package main

import (
	"bytes"
	"crypto/sha256"
	"crypto/x509"
	"encoding/json"
	"encoding/pem"
	"errors"
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/protos/peer"
	"strconv"
)

const (
	TokenId          = "HTLCoin"
	AccountKeyPrefix = TokenId + "-%s-Balance"
	ApproveKeyPrefix = TokenId + "-%s-Approve-%s"
)

type LedgerChaincode struct {
}

type Token struct {
	Name        string `json:"name"`
	Symbol      string `json:"symbol"`
	TotalSupply uint64 `json:"totalSupply"`
}

type SugarAccount struct {
	Owner string `json:"owner"`
	Sugar string `json:"sugar"`
	Value uint64 `json:"value"`
}

type trap interface {
	matchSugar(string, string) bool
}

func (s *SugarAccount) spendAble(sender string, to string, wrapper string, t trap) bool {
	return s.Owner == sender || s.Owner == to || t.matchSugar(s.Sugar, wrapper)
}

func (l *LedgerChaincode) matchSugar(sugar string, wrapper string) bool {
	return sugar == mySha256([]byte(wrapper))
}

// just for event
type transferEvent struct {
	From  string `json:"from"`
	To    string `json:"to"`
	Value uint64 `json:"value"`
}
type approvalEvent struct {
	Owner   string `json:"owner"`
	Spender string `json:"spender"`
	Value   uint64 `json:"value"`
}

func (l *LedgerChaincode) Init(stub shim.ChaincodeStubInterface) (res peer.Response) {
	defer func() {
		if r, ok := recover().(error); ok {
			res = shim.Error(r.Error())
		}
	}()

	fn, args := stub.GetFunctionAndParameters()

	switch fn {
	case "init":
		res = l.init(stub, args)
	default:
		res = shim.Success(nil)
	}

	return
}

// Invoke function
func (l *LedgerChaincode) Invoke(stub shim.ChaincodeStubInterface) (res peer.Response) {
	defer func() {
		if r, ok := recover().(error); ok {
			res = shim.Error(r.Error())
		}
	}()

	fcn, args := stub.GetFunctionAndParameters()

	switch fcn {
	case "senderInfo":
		res = l.senderInfo(stub, args)
	case "tokenInfo":
		res = l.tokenInfo(stub, args)
	case "totalSupply":
		res = l.totalSupply(stub, args)
	case "balanceOf":
		res = l.balanceOf(stub, args)
	case "balanceOfSugarAccount":
		res = l.balanceOfSugarAccount(stub, args)
	case "transfer":
		res = l.transfer(stub, args)
	case "createSugarAccount":
		res = l.createSugarAccount(stub, args)
	case "withdrawFromSugarAccount":
		res = l.withdrawFromSugarAccount(stub, args)
	case "allowance":
		res = l.allowance(stub, args)
	case "approve":
		res = l.approve(stub, args)
	case "transferFrom":
		res = l.transferFrom(stub, args)
	default:
		res = shim.Error("invalid function name")
	}

	return
}

func (l *LedgerChaincode) init(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 3 {
		return shim.Error("invalid arguments")
	}
	name := args[0]
	symbol := args[1]
	totalSupply := stringToUint64(args[2])
	var token = &Token{
		Name:        name,
		Symbol:      symbol,
		TotalSupply: totalSupply,
	}

	err := checkToken(token)
	if err != nil {
		return shim.Error(err.Error())
	}

	tokenInfo, err := json.Marshal(token)
	if err != nil {
		return shim.Error(err.Error())
	}

	// save genesis info of token
	err = stub.PutState(TokenId, tokenInfo)
	if err != nil {
		return shim.Error(err.Error())
	}

	owner := getMsgSender(stub)
	updateBalance(stub, owner, token.TotalSupply)
	emitEvent(stub, "Transfer", marshalTransferEvent("genesis", owner, token.TotalSupply))

	return shim.Success([]byte("success"))
}

func (l *LedgerChaincode) senderInfo(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 0 {
		return shim.Error("invalid arguments")
	}

	return shim.Success([]byte(getMsgSender(stub)))
}

func (l *LedgerChaincode) tokenInfo(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 0 {
		return shim.Error("invalid arguments")
	}

	token, err := stub.GetState(TokenId)
	if err != nil {
		return shim.Error(err.Error())
	}
	return shim.Success(token)
}

func (l *LedgerChaincode) totalSupply(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 0 {
		return shim.Error("invalid arguments")
	}

	tokenInfo, err := stub.GetState(TokenId)
	if err != nil {
		return shim.Error(err.Error())
	}

	var token Token
	err = json.Unmarshal(tokenInfo, &token)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(uint64ToBytes(token.TotalSupply))
}

func (l *LedgerChaincode) balanceOf(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	owner := args[0]
	balance := getBalance(stub, owner)

	return shim.Success(uint64ToBytes(balance))
}

func (l *LedgerChaincode) balanceOfSugarAccount(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	key := args[0]
	saInfo, err := stub.GetState(key)
	if err != nil {
		return shim.Error(err.Error())
	}
	if saInfo == nil {
		return shim.Success([]byte("account not found"))
	}

	var sa SugarAccount
	err = json.Unmarshal(saInfo, &sa)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(uint64ToBytes(sa.Value))
}

func (l *LedgerChaincode) transfer(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 2 {
		return shim.Error("invalid arguments")
	}

	sender := getMsgSender(stub)
	to := args[0]
	value := stringToUint64(args[1])

	updateBalance(stub, sender, safeSub(getBalance(stub, sender), value))
	updateBalance(stub, to, safeAdd(getBalance(stub, to), value))
	emitEvent(stub, "Transfer", marshalTransferEvent(sender, to, value))

	return shim.Success([]byte("success"))
}

// owner create a Sugar account
func (l *LedgerChaincode) createSugarAccount(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 2 {
		return shim.Error("invalid arguments")
	}

	owner := getMsgSender(stub)
	sugar := args[0]
	value := stringToUint64(args[1])
	updateBalance(stub, owner, safeSub(getBalance(stub, owner), value))

	var sa = &SugarAccount{
		Owner: owner,
		Sugar: sugar,
		Value: value,
	}
	saInfo, err := json.Marshal(sa)
	if err != nil {
		return shim.Error(err.Error())
	}

	// get an unique key
	timestamp, err := stub.GetTxTimestamp()
	if err != nil {
		return shim.Error(err.Error())
	}
	currentTime := uint64ToBytes(uint64(timestamp.Seconds))
	key := mySha256(append(saInfo, currentTime...))

	err = stub.PutState(key, saInfo)
	emitEvent(stub, "Transfer", marshalTransferEvent(owner, key, value))

	return shim.Success([]byte(key))
}

// spend the Sugar account
func (l *LedgerChaincode) withdrawFromSugarAccount(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	var wrapper string
	if len(args) == 2 {
		wrapper = ""
	} else if len(args) == 3 {
		wrapper = args[2]
	} else {
		return shim.Error("invalid arguments")
	}

	sender := getMsgSender(stub)
	key := args[0]
	to := args[1]

	saInfo, err := stub.GetState(key)
	if err != nil {
		return shim.Error(err.Error())
	}
	if saInfo == nil {
		return shim.Error("account not found")
	}

	var sa SugarAccount
	err = json.Unmarshal(saInfo, &sa)
	if err != nil {
		return shim.Error(err.Error())
	}

	if !sa.spendAble(sender, to, wrapper, l) {
		return shim.Error("could not spend")
	}

	updateBalance(stub, to, safeAdd(getBalance(stub, to), sa.Value))
	err = stub.DelState(key)
	emitEvent(stub, "Transfer", marshalTransferEvent(key, to, sa.Value))

	return shim.Success([]byte("success"))
}

func (l *LedgerChaincode) allowance(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 2 {
		return shim.Error("invalid arguments")
	}

	owner := args[0]
	spender := args[1]

	allowance := getAllowance(stub, owner, spender)

	return shim.Success(uint64ToBytes(allowance))
}

func (l *LedgerChaincode) approve(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 2 {
		return shim.Error("invalid arguments")
	}
	sender := getMsgSender(stub)
	spender := args[0]
	value := stringToUint64(args[1])

	updateAllowance(stub, sender, spender, value)
	emitEvent(stub, "Approval", marshalApprovalEvent(sender, spender, value))

	return shim.Success([]byte("success"))
}

func (l *LedgerChaincode) transferFrom(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 3 {
		return shim.Error("invalid arguments")
	}

	sender := getMsgSender(stub)
	from := args[0]
	to := args[1]
	value := stringToUint64(args[2])

	updateAllowance(stub, from, sender, safeSub(getAllowance(stub, from, sender), value))
	updateBalance(stub, from, safeSub(getBalance(stub, from), value))
	updateBalance(stub, to, safeAdd(getBalance(stub, to), value))
	emitEvent(stub, "Transfer", marshalTransferEvent(from, to, value))

	return shim.Success([]byte("success"))
}

func getMsgSender(stub shim.ChaincodeStubInterface) string {
	creatorInfo, err := stub.GetCreator()
	checkError(err)

	certStart := bytes.IndexAny(creatorInfo, "-----BEGIN")
	if certStart == -1 {
		panic(errors.New("no certificate found"))
	}

	certText := creatorInfo[certStart:]
	asn1Data, _ := pem.Decode(certText)
	if asn1Data == nil {
		panic(errors.New("could not decode the PEM structure"))
	}

	cert, err := x509.ParseCertificate(asn1Data.Bytes)
	if err != nil {
		panic(err)
	}

	return cert.Subject.CommonName
}

func getBalance(stub shim.ChaincodeStubInterface, owner string) uint64 {
	key := fmt.Sprintf(AccountKeyPrefix, owner)
	balance, err := stub.GetState(key)

	checkError(err)

	if balance == nil {
		return 0
	}
	return bytesToUint64(balance)
}

func updateBalance(stub shim.ChaincodeStubInterface, owner string, balance uint64) {
	key := fmt.Sprintf(AccountKeyPrefix, owner)
	err := stub.PutState(key, uint64ToBytes(balance))
	checkError(err)
}

func getAllowance(stub shim.ChaincodeStubInterface, owner string, spender string) uint64 {
	key := fmt.Sprintf(ApproveKeyPrefix, owner, spender)
	allowance, err := stub.GetState(key)
	checkError(err)

	if allowance == nil {
		return 0
	}
	return bytesToUint64(allowance)
}

func updateAllowance(stub shim.ChaincodeStubInterface, owner string, spender string, allowance uint64) {
	key := fmt.Sprintf(ApproveKeyPrefix, owner, spender)
	err := stub.PutState(key, uint64ToBytes(allowance))
	checkError(err)
}

func marshalTransferEvent(from string, to string, value uint64) []byte {
	transfer, err := json.Marshal(&transferEvent{from, to, value})
	checkError(err)

	return transfer
}

func marshalApprovalEvent(owner string, spender string, value uint64) []byte {
	approval, err := json.Marshal(&approvalEvent{owner, spender, value})
	checkError(err)

	return approval
}

func emitEvent(stub shim.ChaincodeStubInterface, name string, payload []byte) {
	err := stub.SetEvent(name, payload)
	checkError(err)
}

func checkToken(token *Token) error {
	if token.Name == "" {
		return fmt.Errorf("name cannot be null")
	}
	if token.Symbol == "" {
		return fmt.Errorf("symbol cannot be null")
	}
	if token.TotalSupply <= 0 {
		return fmt.Errorf("totalSupply must be greater than 0")
	}
	return nil
}

func checkError(err error) {
	if err != nil {
		panic(err)
	}
}

func stringToUint64(s string) uint64 {
	u, err := strconv.ParseUint(s, 10, 64)
	checkError(err)

	return u
}

func uint64ToString(u uint64) string {
	return strconv.FormatUint(u, 10)
}

func bytesToUint64(b []byte) uint64 {
	return stringToUint64(string(b))
}

func uint64ToBytes(u uint64) []byte {
	return []byte(uint64ToString(u))
}

func safeAdd(a uint64, b uint64) uint64 {
	c := a + b
	if c < a {
		panic(errors.New("invalid addition"))
	}
	return c
}

func safeSub(a uint64, b uint64) uint64 {
	if b > a {
		panic(errors.New("invalid subtraction"))
	}
	return a - b
}

func mySha256(input []byte) string {
	hash := sha256.New()
	hash.Write(input)
	return fmt.Sprintf("%x", hash.Sum(nil))
}

func main() {
	err := shim.Start(new(LedgerChaincode))
	if err != nil {
		fmt.Printf("Error: %s", err)
	}
}
