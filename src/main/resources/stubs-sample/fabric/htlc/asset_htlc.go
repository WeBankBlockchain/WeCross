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

type AssetHtlcChaincode struct {
	MyHTLC HTLC
}

func (a *AssetHtlcChaincode) Init(stub shim.ChaincodeStubInterface) (res peer.Response) {
	defer func() {
		if r, ok := recover().(error); ok {
			res = shim.Error(r.Error())
		}
	}()

	fn, args := stub.GetFunctionAndParameters()

	switch fn {
	case "init":
		res = a.initHTLCContract(stub, args)
	default:
		res = shim.Success(nil)
	}

	return
}

func (a *AssetHtlcChaincode) Invoke(stub shim.ChaincodeStubInterface) (res peer.Response) {
	defer func() {
		if r, ok := recover().(error); ok {
			res = shim.Error(r.Error())
		}
	}()

	fcn, args := stub.GetFunctionAndParameters()

	switch fcn {
	case "setRole":
		res = a.setRole(stub, args)
	case "addInitiator":
		res = a.addInitiator(stub, args)
	case "addParticipant":
		res = a.addParticipant(stub, args)
	case "setSecret":
		res = a.setSecret(stub, args)
	case "getSecret":
		res = a.getSecret(stub, args)
	case "addTask":
		res = a.addTask(stub, args)
	case "getTask":
		res = a.getTask(stub, args)
	case "deleteTask":
		res = a.deleteTask(stub, args)
	case "lock":
		res = a.lock(stub, args)
	case "unlock":
		res = a.unlock(stub, args)
	case "rollback":
		res = a.rollback(stub, args)
	case "getCounterpartyHTLCPath":
		res = a.getCounterpartyHTLCPath(stub, args)
	case "getCounterpartyHTLCName":
		res = a.getCounterpartyHTLCName(stub, args)
	case "getSelfTimelock":
		res = a.getSelfTimelock(stub, args)
	case "getCounterpartyTimelock":
		res = a.getCounterpartyTimelock(stub, args)
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

func (a *AssetHtlcChaincode) initHTLCContract(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 4 {
		return shim.Error("invalid arguments")
	}

	name, channel, counterpartyHtlcPath, counterpartyHtlcName := args[0], args[1], args[2], args[3]
	a.MyHTLC.setCounterpartyHTLCInfo(stub, counterpartyHtlcPath, counterpartyHtlcName)

	err := stub.PutState(LedgerChainCodeNameKey, []byte(name))
	if err != nil {
		return shim.Error(err.Error())
	}

	err = stub.PutState(LedgerChainCodeChannelKey, []byte(channel))
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success([]byte("success"))
}

func (a *AssetHtlcChaincode) setRole(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 2 {
		return shim.Error("invalid arguments")
	}

	hash, role := args[0], args[1]
	a.MyHTLC.setRole(stub, hash, role)
	return shim.Success([]byte("success"))
}

func (a *AssetHtlcChaincode) addInitiator(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 5 {
		return shim.Error("invalid arguments")
	}

	hash, sender, receiver, amount, timelock := args[0], args[1], args[2], args[3], args[4]
	a.MyHTLC.addInitiator(stub, hash, sender, receiver, amount, timelock)
	return shim.Success([]byte("success"))
}

func (a *AssetHtlcChaincode) addParticipant(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 5 {
		return shim.Error("invalid arguments")
	}

	hash, sender, receiver, amount, timelock := args[0], args[1], args[2], args[3], args[4]
	a.MyHTLC.addParticipant(stub, hash, sender, receiver, amount, timelock)
	return shim.Success([]byte("success"))
}

func (a *AssetHtlcChaincode) setSecret(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 2 {
		return shim.Error("invalid arguments")
	}

	hash, secret := args[0], args[1]
	if !hashMatched(hash, secret) {
		return shim.Error("hash not matched")
	}

	a.MyHTLC.setSecret(stub, hash, secret)
	return shim.Success([]byte("success"))
}

func (a *AssetHtlcChaincode) getSecret(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getSecret(stub, hash))
}

func (a *AssetHtlcChaincode) addTask(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	a.MyHTLC.addTask(stub, hash)
	return shim.Success([]byte("success"))
}

func (a *AssetHtlcChaincode) getTask(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 0 {
		return shim.Error("invalid arguments")
	}

	return shim.Success(a.MyHTLC.getTask(stub))
}

func (a *AssetHtlcChaincode) deleteTask(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	a.MyHTLC.deleteTask(stub, hash)
	return shim.Success([]byte("success"))
}

func (a *AssetHtlcChaincode) lock(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	if !taskIsExisted(stub, hash) {
		return shim.Error("task not exists")
	}

	if a.MyHTLC.getLockStatus(stub, hash) {
		return shim.Success([]byte("success"))
	}

	timelock := a.MyHTLC.getTimelock(stub, hash)
	timeStamp, err := stub.GetTxTimestamp()
	if err != nil {
		return shim.Error(err.Error())
	}
	if a.MyHTLC.getRollbackStatus(stub, hash) || timelock <= uint64(timeStamp.Seconds) {
		return shim.Error("has rolled back")
	}

	amount := a.MyHTLC.getAmount(stub, hash)
	cname, err := stub.GetState(LedgerChainCodeNameKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	channel, err := stub.GetState(LedgerChainCodeChannelKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	trans :=[][]byte{[]byte("createSugarAccount"), []byte(hash), uint64ToBytes(amount)}
	response := stub.InvokeChaincode(string(cname), trans, string(channel))
    if response.Status != 200 {
		return shim.Error(response.Message)
	}

	a.MyHTLC.setLockStatus(stub, hash)
	err = stub.PutState(getSugarAccountKey(hash), response.Payload)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success([]byte("success"))
}

func (a *AssetHtlcChaincode) unlock(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 2 {
		return shim.Error("invalid arguments")
	}

	hash, secret := args[0], args[1]
	if !taskIsExisted(stub, hash) {
		return shim.Error("task not exists")
	}

	if a.MyHTLC.getUnlockStatus(stub, hash) {
		return shim.Success([]byte("success"))
	}

	if !hashMatched(hash, secret) {
		return shim.Error("hash not matched")
	}

	if !a.MyHTLC.getLockStatus(stub, hash) {
		return shim.Error("can not unlock until lock is done")
	}

	timelock := a.MyHTLC.getTimelock(stub, hash)
	timeStamp, err := stub.GetTxTimestamp()
	if err != nil {
		return shim.Error(err.Error())
	}
	if a.MyHTLC.getRollbackStatus(stub, hash) || timelock <= uint64(timeStamp.Seconds) {
		return shim.Error("has rolled back")
	}

	receiver := a.MyHTLC.getReceiver(stub, hash)
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
	trans :=[][]byte{[]byte("withdrawFromSugarAccount"), sa, receiver, []byte(secret)}
	response := stub.InvokeChaincode(string(cname), trans, string(channel))
	if response.Status != 200 {
		return shim.Error(response.Message)
	}

	a.MyHTLC.setUnlockStatus(stub, hash)
	return shim.Success([]byte("success"))
}

func (a *AssetHtlcChaincode) rollback(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	if !taskIsExisted(stub, hash) {
		return shim.Error("task not exists")
	}

	if a.MyHTLC.getRollbackStatus(stub, hash) {
		return shim.Success([]byte("success"))
	}

	timelock := a.MyHTLC.getTimelock(stub, hash)
	timeStamp, err := stub.GetTxTimestamp()
	if err != nil {
		return shim.Error(err.Error())
	}
	if  timelock <= uint64(timeStamp.Seconds) {
		return shim.Error("has rolled back")
	}

	if !a.MyHTLC.getLockStatus(stub, hash) {
		return shim.Error("no need to rollback unless lock is done")
	}
	if a.MyHTLC.getUnlockStatus(stub, hash) {
		return shim.Error("can not rollback if unlock is done")
	}


	sender := a.MyHTLC.getSender(stub, hash)
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
	trans :=[][]byte{[]byte("withdrawFromSugarAccount"), sa, sender}
	response := stub.InvokeChaincode(string(cname), trans, string(channel))
	if response.Status != 200 {
		return shim.Error(response.Message)
	}

	a.MyHTLC.setRollbackStatus(stub, hash)
	return shim.Success([]byte("success"))
}

func (a *AssetHtlcChaincode) getCounterpartyHTLCPath(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 0 {
		return shim.Error("invalid arguments")
	}

	return shim.Success(a.MyHTLC.getCounterpartyHTLCPath(stub))
}

func (a *AssetHtlcChaincode) getCounterpartyHTLCName(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 0 {
		return shim.Error("invalid arguments")
	}

	return shim.Success(a.MyHTLC.getCounterpartyHTLCName(stub))
}

func (a *AssetHtlcChaincode) getSelfTimelock(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getSelfTimelock(stub, hash))
}

func (a *AssetHtlcChaincode) getCounterpartyTimelock(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getCounterpartyTimelock(stub, hash))
}

func (a *AssetHtlcChaincode) getCounterpartyLockStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getCounterpartyLockStatus(stub, hash))
}

func (a *AssetHtlcChaincode) setCounterpartyLockStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	a.MyHTLC.setCounterpartyLockStatus(stub, hash)
	return shim.Success([]byte("success"))
}

func (a *AssetHtlcChaincode) getSelfUnlockStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getSelfUnlockStatus(stub, hash))
}

func (a *AssetHtlcChaincode) getCounterpartyUnlockStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getCounterpartyUnlockStatus(stub, hash))
}

func (a *AssetHtlcChaincode) setCounterpartyUnlockStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	a.MyHTLC.setCounterpartyUnlockStatus(stub, hash)
	return shim.Success([]byte("success"))
}

func (a *AssetHtlcChaincode) getSelfRollbackStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getSelfRollbackStatus(stub, hash))
}

func (a *AssetHtlcChaincode) getCounterpartyRollbackStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("invalid arguments")
	}

	hash := args[0]
	return shim.Success(a.MyHTLC.getCounterpartyRollbackStatus(stub, hash))
}

func (a *AssetHtlcChaincode) setCounterpartyRollbackStatus(stub shim.ChaincodeStubInterface, args []string) peer.Response {
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
	err := shim.Start(new(AssetHtlcChaincode))
	if err != nil {
		fmt.Printf("Error: %s", err)
	}
}
