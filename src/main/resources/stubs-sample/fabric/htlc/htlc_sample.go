package main

import (
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/protos/peer"
)

const (
	LedgerChainCodeNameKey    = "LedgerChainCodeName"
	LedgerChainCodeChannelKey = "LedgerChainCodeChannel"
	SugarAccountKeyPrefix     = "S-%s"
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
			res = shim.Success([]byte(r.Error()))
		}
	}()

	fcn, args := stub.GetFunctionAndParameters()

	switch fcn {
	case "newContract":
		res = a.newContract(stub, args)
	case "setNewContractTxInfo":
		res = a.setNewContractTxInfo(stub, args)
	case "getNewContractTxInfo":
		res = a.getNewContractTxInfo(stub, args)
	case "getContract":
		res = a.getContract(stub, args)
	case "setSecret":
		res = a.setSecret(stub, args)
	case "getSecret":
		res = a.getSecret(stub, args)
	case "lock":
		res = a.lock(stub, args)
	case "unlock":
		res = a.unlock(stub, args)
	case "rollback":
		res = a.rollback(stub, args)
	case "setLockTxInfo":
		res = a.setLockTxInfo(stub, args)
	case "getLockTxInfo":
		res = a.getLockTxInfo(stub, args)
	case "getCounterpartyHtlc":
		res = a.getCounterpartyHtlc(stub, args)
	case "getTask":
		res = a.getTask(stub, args)
	case "deleteTask":
		res = a.deleteTask(stub, args)
	case "getSelfTimelock":
		res = a.getSelfTimelock(stub, args)
	case "getCounterpartyTimelock":
		res = a.getCounterpartyTimelock(stub, args)
	case "getSelfLockStatus":
		res = a.getSelfLockStatus(stub, args)
	case "getCounterpartyLockStatus":
		res = a.getCounterpartyLockStatus(stub, args)
	case "setCounterpartyLockStatus":
		res = a.setCounterpartyLockStatus(stub, args)
	case "getSelfUnlockStatus":
		res = a.getSelfUnlockStatus(stub, args)
	case "getCounterpartyUnlockStatus":
		res = a.getCounterpartyUnlockStatus(stub, args)
	case "setCounterpartyUnlockStatus":
		res = a.setCounterpartyUnlockStatus(stub, args)
	case "getSelfRollbackStatus":
		res = a.getSelfRollbackStatus(stub, args)
	case "getCounterpartyRollbackStatus":
		res = a.getCounterpartyRollbackStatus(stub, args)
	case "setCounterpartyRollbackStatus":
		res = a.setCounterpartyRollbackStatus(stub, args)
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

	return shim.Success([]byte("success"))
}

func (a *HtlcChaincode) newContract(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 10 {
		return shim.Error("invalid arguments")
	}

	hash, role, sender0, receiver0, amount0, timelock0, sender1, receiver1, amount1, timelock1 :=
		args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]
	a.MyHTLC.newContract(stub, hash, role, sender0, receiver0, amount0, timelock0, sender1, receiver1, amount1, timelock1)
	return shim.Success([]byte("success"))
}

func (a *HtlcChaincode) setNewContractTxInfo(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 3 {
		return shim.Error("invalid arguments")
	}

	hash, txHash, blockNum := args[0], args[1], args[2]
	a.MyHTLC.setNewContractTxInfo(stub, hash, txHash, blockNum)
	return shim.Success([]byte("success"))
}

func (a *HtlcChaincode) getNewContractTxInfo(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getNewContractTxInfo(stub, hash))
}

func (a *HtlcChaincode) getContract(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getContract(stub, hash))
}

func (a *HtlcChaincode) setSecret(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 2 {
		return shim.Error("invalid arguments")
	}

	hash, secret := args[0], args[1]
	a.MyHTLC.setSecret(stub, hash, secret)
	return shim.Success([]byte("success"))
}

func (a *HtlcChaincode) getSecret(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getSecret(stub, hash))
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

	var cd ContractData
	a.MyHTLC.getSelfContractData(stub, hash, &cd)
	cname, err := stub.GetState(LedgerChainCodeNameKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	channel, err := stub.GetState(LedgerChainCodeChannelKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	trans := [][]byte{[]byte("escrowToSugar"), []byte(cd.Sender), []byte(hash), uint64ToBytes(cd.Amount)}
	response := stub.InvokeChaincode(string(cname), trans, string(channel))
	if response.Status != 200 {
		return shim.Error(response.Message)
	}

	err = stub.PutState(getSugarAccountKey(hash), response.Payload)
	if err != nil {
		return shim.Error(err.Error())
	}

	cd.Locked = true
	a.MyHTLC.setSelfContractData(stub, hash, &cd)
	return shim.Success([]byte("success"))
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

	var cd ContractData
	a.MyHTLC.getSelfContractData(stub, hash, &cd)
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
	trans := [][]byte{[]byte("withdrawFromSugarAccount"), sa, []byte(cd.Receiver), []byte(secret)}
	response := stub.InvokeChaincode(string(cname), trans, string(channel))
	if response.Status != 200 {
		return shim.Error(response.Message)
	}

	cd.Unlocked = true
	cd.Secret = secret
	a.MyHTLC.setSelfContractData(stub, hash, &cd)
	return shim.Success([]byte("success"))
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

	var cd ContractData
	a.MyHTLC.getSelfContractData(stub, hash, &cd)
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
	trans := [][]byte{[]byte("withdrawFromSugarAccount"), sa, []byte(cd.Sender)}
	response := stub.InvokeChaincode(string(cname), trans, string(channel))
	if response.Status != 200 {
		return shim.Error(response.Message)
	}

	cd.Rolledback = true
	a.MyHTLC.setSelfContractData(stub, hash, &cd)
	return shim.Success([]byte("success"))
}

func (a *HtlcChaincode) setLockTxInfo(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 3 {
		return shim.Error("invalid arguments")
	}

	hash, txHash, blockNum := args[0], args[1], args[2]
	a.MyHTLC.setLockTxInfo(stub, hash, txHash, blockNum)
	return shim.Success([]byte("success"))
}

func (a *HtlcChaincode) getLockTxInfo(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getLockTxInfo(stub, hash))
}

func (a *HtlcChaincode) getCounterpartyHtlc(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	return shim.Success(a.MyHTLC.getCounterpartyHtlc(stub))
}

func (a *HtlcChaincode) getTask(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	return shim.Success(a.MyHTLC.getTask(stub))
}

func (a *HtlcChaincode) deleteTask(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	a.MyHTLC.deleteTask(stub, hash)
	return shim.Success([]byte("success"))
}

func (a *HtlcChaincode) getSelfTimelock(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getSelfTimelock(stub, hash))
}

func (a *HtlcChaincode) getCounterpartyTimelock(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getCounterpartyTimelock(stub, hash))
}

func (a *HtlcChaincode) getSelfLockStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getSelfLockStatus(stub, hash))
}

func (a *HtlcChaincode) getCounterpartyLockStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getCounterpartyLockStatus(stub, hash))
}

func (a *HtlcChaincode) setCounterpartyLockStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	a.MyHTLC.setCounterpartyLockStatus(stub, hash)
	return shim.Success([]byte("success"))
}

func (a *HtlcChaincode) getSelfUnlockStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getSelfUnlockStatus(stub, hash))
}

func (a *HtlcChaincode) getCounterpartyUnlockStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getCounterpartyUnlockStatus(stub, hash))
}

func (a *HtlcChaincode) setCounterpartyUnlockStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	a.MyHTLC.setCounterpartyUnlockStatus(stub, hash)
	return shim.Success([]byte("success"))
}

func (a *HtlcChaincode) getSelfRollbackStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getSelfRollbackStatus(stub, hash))
}

func (a *HtlcChaincode) getCounterpartyRollbackStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getCounterpartyRollbackStatus(stub, hash))
}

func (a *HtlcChaincode) setCounterpartyRollbackStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	a.MyHTLC.setCounterpartyRollbackStatus(stub, hash)
	return shim.Success([]byte("success"))
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
