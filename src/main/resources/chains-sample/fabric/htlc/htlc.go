package main

import (
	"bytes"
	"crypto/sha256"
	"crypto/x509"
	"encoding/binary"
	"encoding/json"
	"encoding/pem"
	"errors"
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"strconv"
)

const (
	RoleKey                    = "Role-%s"
	IndexKey                   = "Index-%s"
	ProposalsKey               = "HTLCProposals"
	InitiatorKeyPrefix         = "Initiator-%s"
	ParticipantKeyPrefix       = "Participant-%s"
	CounterpartyHtlcAddressKey = "CounterpartyHtlcAddressKey"
	NewProposalTxInfoKeyPrefix = "NewProposalTxHash-%s"
	Size                       = 1024 // capacity of proposal list
	NullFlag                   = "null"
	SplitSymbol                = "##"
)

type HTLC struct {
}

type ProposalData struct {
	Secret     string `json:"secret"`
	Sender     string `json:"sender"`
	Receiver   string `json:"receiver"`
	Amount     uint64 `json:"amount"`
	Timelock   uint64 `json:"timelock"`
	Locked     bool   `json:"locked"`
	Unlocked   bool   `json:"unlocked"`
	Rolledback bool   `json:"rolledback"`
}

type Proposals struct {
	FreeIndexStack [Size]int    `json:"freeIndexStack"`
	ProposalList   [Size]string `json:"proposalList"`
	Depth          int          `json:"depth"`
}

func (h *HTLC) getRole(stub shim.ChaincodeStubInterface, hash string) bool {
	role, err := stub.GetState(getRoleKey(hash))
	checkError(err)

	if role == nil {
		panic(errors.New("proposal not exists"))
	}

	return string(role) == "true"
}

func (h *HTLC) getIndex(stub shim.ChaincodeStubInterface, hash string) int {
	index, err := stub.GetState(getIndexKey(hash))
	checkError(err)

	if index == nil {
		panic(errors.New("proposal not exists"))
	}

	return bytesToInt(index)
}

func (h *HTLC) init(stub shim.ChaincodeStubInterface, counterpartyHtlcAddress string) {
	err := stub.PutState(CounterpartyHtlcAddressKey, []byte(counterpartyHtlcAddress))
	checkError(err)
}

func (h *HTLC) lock(stub shim.ChaincodeStubInterface, hash string) string {
	if !proposalIsExisted(stub, hash) {
		return "proposal not exists"
	}

	var cd ProposalData
	h.getSelfProposalData(stub, hash, &cd)

	if cd.Locked {
		return "done"
	}

	timelock := cd.Timelock
	timeStamp, err := stub.GetTxTimestamp()
	if err != nil {
		return err.Error()
	}
	if cd.Rolledback || timelock <= uint64(timeStamp.Seconds) {
		return "has rolled back"
	}

	return "continue"
}

func (h *HTLC) unlock(stub shim.ChaincodeStubInterface, hash, secret string) string {
	if !proposalIsExisted(stub, hash) {
		return "proposal not exists"
	}

	var cd ProposalData
	h.getSelfProposalData(stub, hash, &cd)

	if cd.Unlocked {
		return "done"
	}

	if !hashMatched(hash, secret) {
		return "hash not matched"
	}

	if !cd.Locked {
		return "can not unlock until lock is done"
	}

	timelock := cd.Timelock
	timeStamp, err := stub.GetTxTimestamp()
	if err != nil {
		return err.Error()
	}
	if cd.Rolledback || timelock <= uint64(timeStamp.Seconds) {
		return "has rolled back"
	}

	return "continue"
}

func (h *HTLC) rollback(stub shim.ChaincodeStubInterface, hash string) string {
	if !proposalIsExisted(stub, hash) {
		return "proposal not exists"
	}

	var cd ProposalData
	h.getSelfProposalData(stub, hash, &cd)

	if cd.Rolledback {
		return "done"
	}

	timelock := cd.Timelock
	timeStamp, err := stub.GetTxTimestamp()
	if err != nil {
		return err.Error()
	}
	if timelock > uint64(timeStamp.Seconds) {
		return "not_yet"
	}

	if !cd.Locked {
		return "no need to rollback unless lock is done"
	}
	if cd.Unlocked {
		return "can not rollback if unlock is done"
	}

	return "continue"
}

func (h *HTLC) getCounterpartyHtlcAddress(stub shim.ChaincodeStubInterface) []byte {
	counterpartyHtlc, err := stub.GetState(CounterpartyHtlcAddressKey)
	checkError(err)

	return counterpartyHtlc
}

func (h *HTLC) newProposal(stub shim.ChaincodeStubInterface, hash, role, sender0, receiver0, amount0, timelock0, sender1, receiver1, amount1, timelock1 string) {
	var proposals Proposals
	h.getProposals(stub, &proposals)

	if proposals.Depth == 0 {
		panic(errors.New("the proposal queue is full, one moment please"))
	}

	if proposalIsExisted(stub, hash) {
		panic(errors.New("proposal existed"))
	}

	if !rightTimelock(stub, timelock0, timelock1) {
		panic(errors.New("illegal timelocks"))
	}

	err := stub.PutState(getRoleKey(hash), []byte(role))
	checkError(err)

	sender := getTxOrigin(stub)
	if role == "true" {
		if sender != sender0 {
			panic(errors.New("only sender can new a proposal"))
		}
	} else {
		if sender != sender1 {
			panic(errors.New("only sender can new a proposal"))
		}
	}

	var initiator = &ProposalData{
		Secret:     "null",
		Sender:     sender0,
		Receiver:   receiver0,
		Amount:     stringToUint64(amount0),
		Timelock:   stringToUint64(timelock0),
		Locked:     false,
		Unlocked:   false,
		Rolledback: false,
	}

	initiatorInfo, err := json.Marshal(initiator)
	checkError(err)

	err = stub.PutState(getInitiatorKey(hash), initiatorInfo)
	checkError(err)

	var participant = &ProposalData{
		Secret:     "null",
		Sender:     sender1,
		Receiver:   receiver1,
		Amount:     stringToUint64(amount1),
		Timelock:   stringToUint64(timelock1),
		Locked:     false,
		Unlocked:   false,
		Rolledback: false,
	}

	participantInfo, err := json.Marshal(participant)
	checkError(err)

	err = stub.PutState(getParticipantKey(hash), participantInfo)
	checkError(err)

	// pre add proposal
	depth := proposals.Depth
	index := proposals.FreeIndexStack[depth-1]
	proposals.Depth--
	err = stub.PutState(getIndexKey(hash), intToBytes(index))
	checkError(err)

	if role == "false" {
		// add proposal
		proposals.ProposalList[index] = hash
	}

	h.setProposals(stub, &proposals)
}

func (h *HTLC) setNewProposalTxInfo(stub shim.ChaincodeStubInterface, hash, txHash, blockNum string) {
	err := stub.PutState(getNewProposalTxInfoKey(hash), []byte(txHash+SplitSymbol+blockNum))
	checkError(err)

}

func (h *HTLC) getNewProposalTxInfo(stub shim.ChaincodeStubInterface, hash string) []byte {
	txInfo, err := stub.GetState(getNewProposalTxInfoKey(hash))
	checkError(err)

	if txInfo == nil {
		return []byte(NullFlag)
	} else {
		return txInfo
	}
}

func (h *HTLC) getNegotiatedData(stub shim.ChaincodeStubInterface, hash string) []byte {
	pInfo, err := stub.GetState(getInitiatorKey(hash))
	checkError(err)

	if pInfo == nil {
		return []byte(NullFlag)
	}

	var pi ProposalData
	err = json.Unmarshal(pInfo, &pi)
	checkError(err)

	pInfo, err = stub.GetState(getParticipantKey(hash))
	checkError(err)

	var pp ProposalData
	err = json.Unmarshal(pInfo, &pp)
	checkError(err)

	return []byte(pi.Sender + SplitSymbol + pi.Receiver + SplitSymbol + uint64ToString(pi.Amount) + SplitSymbol + uint64ToString(pi.Timelock) + SplitSymbol +
		pp.Sender + SplitSymbol + pp.Receiver + SplitSymbol + uint64ToString(pp.Amount) + SplitSymbol + uint64ToString(pp.Timelock))
}

func (h *HTLC) getProposalInfo(stub shim.ChaincodeStubInterface, hash string) []byte {
	pInfo, err := stub.GetState(getInitiatorKey(hash))
	checkError(err)

	if pInfo == nil {
		return []byte(NullFlag)
	}

	var ip ProposalData
	err = json.Unmarshal(pInfo, &ip)
	checkError(err)

	pInfo, err = stub.GetState(getParticipantKey(hash))
	checkError(err)

	var pp ProposalData
	err = json.Unmarshal(pInfo, &pp)
	checkError(err)

	if h.getRole(stub, hash) {
		return []byte("true" + SplitSymbol + ip.Secret + SplitSymbol + uint64ToString(ip.Timelock) + SplitSymbol + boolToString(ip.Locked) + SplitSymbol + boolToString(ip.Unlocked) + SplitSymbol + boolToString(ip.Rolledback) +
			SplitSymbol + uint64ToString(pp.Timelock) + SplitSymbol + boolToString(pp.Locked) + SplitSymbol + boolToString(pp.Unlocked) + SplitSymbol + boolToString(pp.Rolledback))
	} else {
		return []byte("false" + SplitSymbol + NullFlag + SplitSymbol + uint64ToString(pp.Timelock) + SplitSymbol + boolToString(pp.Locked) + SplitSymbol + boolToString(pp.Unlocked) + SplitSymbol + boolToString(pp.Rolledback) +
			SplitSymbol + uint64ToString(ip.Timelock) + SplitSymbol + boolToString(ip.Locked) + SplitSymbol + boolToString(ip.Unlocked) + SplitSymbol + boolToString(ip.Rolledback))
	}
}

func (h *HTLC) setSecret(stub shim.ChaincodeStubInterface, hash, secret string) {
	if !hashMatched(hash, secret) {
		panic(errors.New("hash not matched"))
	}

	if h.getRole(stub, hash) {
		cdInfo, err := stub.GetState(getInitiatorKey(hash))
		checkError(err)

		var cd ProposalData
		err = json.Unmarshal(cdInfo, &cd)
		checkError(err)

		cd.Secret = secret
		cdInfo, err = json.Marshal(&cd)
		checkError(err)

		err = stub.PutState(getInitiatorKey(hash), cdInfo)
		checkError(err)

		// add proposal
		var proposals Proposals
		h.getProposals(stub, &proposals)

		index := h.getIndex(stub, hash)
		proposals.ProposalList[index] = hash
		h.setProposals(stub, &proposals)

	} else {
		cdInfo, err := stub.GetState(getParticipantKey(hash))
		checkError(err)

		var cd ProposalData
		err = json.Unmarshal(cdInfo, &cd)
		checkError(err)

		cd.Secret = secret
		cdInfo, err = json.Marshal(&cd)
		checkError(err)

		err = stub.PutState(getParticipantKey(hash), cdInfo)
		checkError(err)
	}
}

func (h *HTLC) getProposalIDs(stub shim.ChaincodeStubInterface) []byte {
	var proposals Proposals
	h.getProposals(stub, &proposals)
	result := proposals.ProposalList[0]
	for i := 1; i < Size; i++ {
		result = result + SplitSymbol + proposals.ProposalList[i]
	}

	return []byte(result)
}

func (h *HTLC) deleteProposalID(stub shim.ChaincodeStubInterface, hash string) {
	index := h.getIndex(stub, hash)

	var proposals Proposals
	h.getProposals(stub, &proposals)

	if hash != proposals.ProposalList[index] {
		panic(errors.New("invalid operation"))
	}

	proposals.ProposalList[index] = NullFlag
	proposals.FreeIndexStack[proposals.Depth] = index
	proposals.Depth++

	h.setProposals(stub, &proposals)
}

func (h *HTLC) setCounterpartyLockState(stub shim.ChaincodeStubInterface, hash string) {
	var cd ProposalData
	h.getCounterpartyProposalData(stub, hash, &cd)
	cd.Locked = true
	h.setCounterpartyProposalData(stub, hash, &cd)
}

func (h *HTLC) setCounterpartyUnlockState(stub shim.ChaincodeStubInterface, hash string) {
	var cd ProposalData
	h.getCounterpartyProposalData(stub, hash, &cd)
	cd.Unlocked = true
	h.setCounterpartyProposalData(stub, hash, &cd)
}

func (h *HTLC) setCounterpartyRollbackState(stub shim.ChaincodeStubInterface, hash string) {
	var cd ProposalData
	h.getCounterpartyProposalData(stub, hash, &cd)
	cd.Rolledback = true
	h.setCounterpartyProposalData(stub, hash, &cd)
}

func (h *HTLC) getSelfProposalData(stub shim.ChaincodeStubInterface, hash string, pd *ProposalData) {
	var cdInfo []byte
	var err error
	if h.getRole(stub, hash) {
		cdInfo, err = stub.GetState(getInitiatorKey(hash))
	} else {
		cdInfo, err = stub.GetState(getParticipantKey(hash))
	}
	checkError(err)
	err = json.Unmarshal(cdInfo, pd)
	checkError(err)
}

func (h *HTLC) setSelfProposalData(stub shim.ChaincodeStubInterface, hash string, pd *ProposalData) {
	cdInfo, err := json.Marshal(pd)
	if h.getRole(stub, hash) {
		err = stub.PutState(getInitiatorKey(hash), cdInfo)
	} else {
		err = stub.PutState(getParticipantKey(hash), cdInfo)
	}
	checkError(err)
}

func (h *HTLC) getProposals(stub shim.ChaincodeStubInterface, ps *Proposals) {
	psInfo, err := stub.GetState(ProposalsKey)
	checkError(err)

	err = json.Unmarshal(psInfo, ps)
	checkError(err)
}

func (h *HTLC) setProposals(stub shim.ChaincodeStubInterface, ps *Proposals) {
	psInfo, err := json.Marshal(ps)

	err = stub.PutState(ProposalsKey, psInfo)
	checkError(err)
}

func (h *HTLC) getCounterpartyProposalData(stub shim.ChaincodeStubInterface, hash string, pd *ProposalData) {
	var cdInfo []byte
	var err error
	if !h.getRole(stub, hash) {
		cdInfo, err = stub.GetState(getInitiatorKey(hash))
	} else {
		cdInfo, err = stub.GetState(getParticipantKey(hash))
	}
	checkError(err)
	err = json.Unmarshal(cdInfo, pd)
	checkError(err)
}

func (h *HTLC) setCounterpartyProposalData(stub shim.ChaincodeStubInterface, hash string, pd *ProposalData) {
	cdInfo, err := json.Marshal(pd)
	if !h.getRole(stub, hash) {
		err = stub.PutState(getInitiatorKey(hash), cdInfo)
	} else {
		err = stub.PutState(getParticipantKey(hash), cdInfo)
	}
	checkError(err)
}

func getRoleKey(hash string) string {
	return fmt.Sprintf(RoleKey, hash)
}

func getIndexKey(hash string) string {
	return fmt.Sprintf(IndexKey, hash)
}

func getInitiatorKey(hash string) string {
	return fmt.Sprintf(InitiatorKeyPrefix, hash)
}

func getParticipantKey(hash string) string {
	return fmt.Sprintf(ParticipantKeyPrefix, hash)
}

func getNewProposalTxInfoKey(hash string) string {
	return fmt.Sprintf(NewProposalTxInfoKeyPrefix, hash)
}

func proposalIsExisted(stub shim.ChaincodeStubInterface, hash string) bool {
	return isExisted(stub, getInitiatorKey(hash)) &&
		isExisted(stub, getParticipantKey(hash))
}

func isExisted(stub shim.ChaincodeStubInterface, key string) bool {
	value, err := stub.GetState(key)
	checkError(err)

	return value != nil
}

func getTxOrigin(stub shim.ChaincodeStubInterface) string {
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

func boolToString(b bool) string {
	if b {
		return "true"
	} else {
		return "false"
	}
}

func intToBytes(n int) []byte {
	x := int32(n)
	bytesBuffer := bytes.NewBuffer([]byte{})
	err := binary.Write(bytesBuffer, binary.BigEndian, x)
	checkError(err)

	return bytesBuffer.Bytes()
}

func bytesToInt(b []byte) int {
	bytesBuffer := bytes.NewBuffer(b)
	var x int32
	err := binary.Read(bytesBuffer, binary.BigEndian, &x)
	checkError(err)

	return int(x)
}

func stringToUint64(s string) uint64 {
	u, err := strconv.ParseUint(s, 10, 64)
	checkError(err)

	return u
}

func uint64ToString(u uint64) string {
	return strconv.FormatUint(u, 10)
}

func boolToBytes(b bool) []byte {
	if b {
		return []byte("true")
	}
	return []byte("false")
}

func uint64ToBytes(u uint64) []byte {
	return []byte(uint64ToString(u))
}

func checkError(err error) {
	if err != nil {
		panic(err)
	}
}

func hashMatched(hash, secret string) bool {
	return hash == string(mySha256([]byte(secret)))
}

func mySha256(input []byte) string {
	hash := sha256.New()
	hash.Write(input)
	return fmt.Sprintf("%x", hash.Sum(nil))
}

func rightTimelock(stub shim.ChaincodeStubInterface, timelock0, timelock1 string) bool {
	t0 := stringToUint64(timelock0)
	t1 := stringToUint64(timelock1)
	timeStamp, err := stub.GetTxTimestamp()
	checkError(err)
	return t1 > (t0+200) && t0 > (uint64(timeStamp.Seconds)+200)
}
