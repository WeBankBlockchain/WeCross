package main

import (
	"encoding/json"
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/protos/peer"
)

const (
	LedgerChainCodeNameKey    = "LedgerChainCodeName"
	LedgerChainCodeChannelKey = "LedgerChainCodeChannel"
	SugarAccountKeyPrefix     = "S-%s"
	SuccessFlag               = "success"
)

type HtlcChaincode struct {
	MyHTLC HTLC
}

func (a *HtlcChaincode) Init(stub shim.ChaincodeStubInterface) (res peer.Response) {
	defer func() {
		if r, ok := recover().(error); ok {
			res = shim.Error(r.Error())
		}
	}()

	var freeIndexStack [Size] int
	var proposalList [Size] string
	for i := 0; i < Size; i++ {
		proposalList[i] = NullFlag
		freeIndexStack[i] = Size - i - 1
	}
	var proposals = Proposals{
		FreeIndexStack: freeIndexStack,
		ProposalList: proposalList,
		Depth: Size,
	}

	p, err := json.Marshal(&proposals)
	checkError(err)

	err = stub.PutState(ProposalsKey, p)
	checkError(err)

	fn, args := stub.GetFunctionAndParameters()

	switch fn {
	case "init":
		res = a.init(stub, args)
	default:
		res = shim.Success(nil)
	}

	return
}

func (a *HtlcChaincode) Invoke(stub shim.ChaincodeStubInterface) (res peer.Response) {
	defer func() {
		if r, ok := recover().(error); ok {
			// return error message
			res = shim.Error(r.Error())
		}
	}()

	fcn, args := stub.GetFunctionAndParameters()

	switch fcn {
	case "init":
		res = a.init(stub, args)
	case "lock":
		res = a.lock(stub, args)
	case "unlock":
		res = a.unlock(stub, args)
	case "rollback":
		res = a.rollback(stub, args)
	case "getCounterpartyHtlcAddress":
		res = a.getCounterpartyHtlcAddress(stub, args)
	case "newProposal":
		res = a.newProposal(stub, args)
	case "setNewProposalTxInfo":
		res = a.setNewProposalTxInfo(stub, args)
	case "getNewProposalTxInfo":
		res = a.getNewProposalTxInfo(stub, args)
	case "getNegotiatedData":
		res = a.getNegotiatedData(stub, args)
	case "getProposalInfo":
		res = a.getProposalInfo(stub, args)
	case "setSecret":
		res = a.setSecret(stub, args)
	case "getProposalIDs":
		res = a.getProposalIDs(stub, args)
	case "deleteProposalID":
		res = a.deleteProposalID(stub, args)
	case "setCounterpartyLockState":
		res = a.setCounterpartyLockState(stub, args)
	case "setCounterpartyUnlockState":
		res = a.setCounterpartyUnlockState(stub, args)
	case "setCounterpartyRollbackState":
		res = a.setCounterpartyRollbackState(stub, args)
	case "balanceOf":
		res = a.balanceOf(stub, args)
	default:
		res = shim.Error("invalid function name")
	}

	return
}

func (a *HtlcChaincode) init(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 3 {
		return shim.Error("invalid arguments")
	}

	name, channel, counterpartyHtlc := args[0], args[1], args[2]

	err := stub.PutState(LedgerChainCodeNameKey, []byte(name))
	if err != nil {
		return shim.Error(err.Error())
	}

	err = stub.PutState(LedgerChainCodeChannelKey, []byte(channel))
	if err != nil {
		return shim.Error(err.Error())
	}

	a.MyHTLC.init(stub, counterpartyHtlc)

	return shim.Success([]byte(SuccessFlag))
}

func (a *HtlcChaincode) newProposal(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 10 {
		return shim.Error("invalid arguments")
	}

	hash, role, sender0, receiver0, amount0, timelock0, sender1, receiver1, amount1, timelock1 :=
		args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]
	a.MyHTLC.newProposal(stub, hash, role, sender0, receiver0, amount0, timelock0, sender1, receiver1, amount1, timelock1)
	return shim.Success([]byte(SuccessFlag))
}

func (a *HtlcChaincode) setNewProposalTxInfo(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 3 {
		return shim.Error("invalid arguments")
	}

	hash, txHash, blockNum := args[0], args[1], args[2]
	a.MyHTLC.setNewProposalTxInfo(stub, hash, txHash, blockNum)
	return shim.Success([]byte(SuccessFlag))
}

func (a *HtlcChaincode) getNewProposalTxInfo(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getNewProposalTxInfo(stub, hash))
}

func (a *HtlcChaincode) getNegotiatedData(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getNegotiatedData(stub, hash))
}

func (a *HtlcChaincode) getProposalInfo(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getProposalInfo(stub, hash))
}

func (a *HtlcChaincode) setSecret(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 2 {
		return shim.Error("invalid arguments")
	}

	hash, secret := args[0], args[1]
	a.MyHTLC.setSecret(stub, hash, secret)
	return shim.Success([]byte(SuccessFlag))
}

func (a *HtlcChaincode) lock(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	result := a.MyHTLC.lock(stub, hash)
	if result == "done" {
		return shim.Success([]byte("success"))
	} else if result != "continue" {
		return shim.Success([]byte(result))
	}

	var pd ProposalData
	a.MyHTLC.getSelfProposalData(stub, hash, &pd)
	cname, err := stub.GetState(LedgerChainCodeNameKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	channel, err := stub.GetState(LedgerChainCodeChannelKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	trans := [][]byte{[]byte("escrowToSugar"), []byte(pd.Sender), []byte(hash), uint64ToBytes(pd.Amount)}
	response := stub.InvokeChaincode(string(cname), trans, string(channel))
	if response.Status != shim.OK {
		return shim.Success([]byte(response.Message))
	}

	err = stub.PutState(getSugarAccountKey(hash), response.Payload)
	if err != nil {
		return shim.Error(err.Error())
	}

	pd.Locked = true
	a.MyHTLC.setSelfProposalData(stub, hash, &pd)
	return shim.Success([]byte(SuccessFlag))
}

func (a *HtlcChaincode) unlock(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) < 2 {
		return shim.Error("invalid arguments")
	}

	hash, secret := args[0], args[1]
	result := a.MyHTLC.unlock(stub, hash, secret)
	if result == "done" {
		return shim.Success([]byte("success"))
	} else if result != "continue" {
		return shim.Success([]byte(result))
	}

	var pd ProposalData
	a.MyHTLC.getSelfProposalData(stub, hash, &pd)
	sa, err := stub.GetState(getSugarAccountKey(hash))
	if err != nil {
		return shim.Error(err.Error())
	}
	cname, err := stub.GetState(LedgerChainCodeNameKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	channel, err := stub.GetState(LedgerChainCodeChannelKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	trans := [][]byte{[]byte("withdrawFromSugarAccount"), sa, []byte(pd.Receiver), []byte(secret)}
	response := stub.InvokeChaincode(string(cname), trans, string(channel))
	if response.Status != shim.OK {
		return shim.Success([]byte(response.Message))
	}

	pd.Unlocked = true
	pd.Secret = secret
	a.MyHTLC.setSelfProposalData(stub, hash, &pd)
	return shim.Success([]byte(SuccessFlag))
}

func (a *HtlcChaincode) rollback(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	result := a.MyHTLC.rollback(stub, hash)
	if result == "done" {
		return shim.Success([]byte("success"))
	} else if result != "continue" {
		return shim.Success([]byte(result))
	}

	var pd ProposalData
	a.MyHTLC.getSelfProposalData(stub, hash, &pd)
	sa, err := stub.GetState(getSugarAccountKey(hash))
	if err != nil {
		return shim.Error(err.Error())
	}
	cname, err := stub.GetState(LedgerChainCodeNameKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	channel, err := stub.GetState(LedgerChainCodeChannelKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	trans := [][]byte{[]byte("withdrawFromSugarAccount"), sa, []byte(pd.Sender)}
	response := stub.InvokeChaincode(string(cname), trans, string(channel))
	if response.Status != shim.OK {
		return shim.Success([]byte(response.Message))
	}

	pd.Rolledback = true
	a.MyHTLC.setSelfProposalData(stub, hash, &pd)
	return shim.Success([]byte(SuccessFlag))
}

func (a *HtlcChaincode) getCounterpartyHtlcAddress(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	return shim.Success(a.MyHTLC.getCounterpartyHtlcAddress(stub))
}

func (a *HtlcChaincode) getProposalIDs(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	return shim.Success(a.MyHTLC.getProposalIDs(stub))
}

func (a *HtlcChaincode) deleteProposalID(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	a.MyHTLC.deleteProposalID(stub, hash)
	return shim.Success([]byte(SuccessFlag))
}

func (a *HtlcChaincode) setCounterpartyLockState(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	a.MyHTLC.setCounterpartyLockState(stub, hash)
	return shim.Success([]byte(SuccessFlag))
}


func (a *HtlcChaincode) setCounterpartyUnlockState(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	a.MyHTLC.setCounterpartyUnlockState(stub, hash)
	return shim.Success([]byte(SuccessFlag))
}

func (a *HtlcChaincode) setCounterpartyRollbackState(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	a.MyHTLC.setCounterpartyRollbackState(stub, hash)
	return shim.Success([]byte(SuccessFlag))
}

func (a *HtlcChaincode) balanceOf(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	cname, err := stub.GetState(LedgerChainCodeNameKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	channel, err := stub.GetState(LedgerChainCodeChannelKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	trans := [][]byte{[]byte("balanceOf"), []byte(args[0])}
	response := stub.InvokeChaincode(string(cname), trans, string(channel))
	if response.Status != 200 {
		return shim.Success([]byte(response.Message))
	}
	return shim.Success(response.Payload)
}

func getSugarAccountKey(hash string) string {
	return fmt.Sprintf(SugarAccountKeyPrefix, hash)
}

func main() {
	err := shim.Start(new(HtlcChaincode))
	if err != nil {
		fmt.Printf("Error: %s", err)
	}
}
