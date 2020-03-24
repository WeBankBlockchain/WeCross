package main

import (
	"crypto/sha256"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"strconv"
)

const (
	TaskKey              = "HTLCTask"
	InitiatorKeyPrefix   = "I-%s"
	ParticipantKeyPrefix = "P-%s"
)

type HTLC struct {
}

type ContractData struct {
	Secret     string `json:"secret"`
	Sender     string `json:"sender"`
	Receiver   string `json:"receiver"`
	Amount     uint64 `json:"amount"`
	Timelock   uint64 `json:"timelock"`
	Locked     bool   `json:"locked"`
	Unlocked   bool   `json:"unlocked"`
	Rolledback bool   `json:"rolledback"`
}

type Tasks struct {
	Num       int      `json:"num"`
	TaskQueue []string `json:"taskQueue"`
}

func (h *HTLC) getRole(stub shim.ChaincodeStubInterface, hash string) bool {
	role, err := stub.GetState(hash)
	checkError(err)

	if role == nil {
		panic(errors.New("hash not found"))
	}

	return string(role) == "true"
}

func (h *HTLC) newContract(stub shim.ChaincodeStubInterface, hash, role, secret, sender0, receiver0, amount0, timelock0, sender1, receiver1, amount1, timelock1 string) {
	if isExisted(stub, hash) {
		panic(errors.New("task exists"))
	}

	if !rightTimelock(stub, timelock0, timelock1) {
		panic(errors.New("illegal timelocks"))
	}

	err := stub.PutState(hash, []byte(role))
	checkError(err)

	if string(role) == "true" {
		if !hashMatched(hash, secret) {
			panic(errors.New("hash not matched"))
		}
	}

	var initiator = &ContractData{
		Secret:     secret,
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

	var participant = &ContractData{
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

	h.addTask(stub, hash)
}

func (h *HTLC) setSecret(stub shim.ChaincodeStubInterface, hash, secret string) {
	if h.getRole(stub, hash) {
		cdInfo, err := stub.GetState(getInitiatorKey(hash))
		checkError(err)

		var cd ContractData
		err = json.Unmarshal(cdInfo, &cd)
		checkError(err)

		cd.Secret = secret
		// only unlock would set secret
		cd.Unlocked = true
		cdInfo, err = json.Marshal(&cd)
		checkError(err)

		err = stub.PutState(getInitiatorKey(hash), cdInfo)
		checkError(err)
	} else {
		cdInfo, err := stub.GetState(getParticipantKey(hash))
		checkError(err)

		var cd ContractData
		err = json.Unmarshal(cdInfo, &cd)
		checkError(err)

		cd.Secret = secret
		cd.Unlocked = true
		cdInfo, err = json.Marshal(&cd)
		checkError(err)

		err = stub.PutState(getParticipantKey(hash), cdInfo)
		checkError(err)
	}
}

func (h *HTLC) getSecret(stub shim.ChaincodeStubInterface, hash string) []byte {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	return []byte(cd.Secret)
}

func (h *HTLC) addTask(stub shim.ChaincodeStubInterface, hash string) {

	t, err := stub.GetState(TaskKey)
	checkError(err)

	var tasks Tasks
	if t == nil {
		tasks = Tasks{
			Num:       1,
			TaskQueue: []string{hash},
		}
	} else {
		err = json.Unmarshal(t, &tasks)
		tasks.Num++
		tasks.TaskQueue = append(tasks.TaskQueue, hash)
	}

	t, err = json.Marshal(&tasks)
	checkError(err)

	err = stub.PutState(TaskKey, t)
	checkError(err)
}

func (h *HTLC) getTask(stub shim.ChaincodeStubInterface) []byte {
	t, err := stub.GetState(TaskKey)
	checkError(err)

	if t == nil {
		return []byte("null")
	}

	var tasks Tasks
	err = json.Unmarshal(t, &tasks)
	checkError(err)

	if tasks.Num == 0 {
		return []byte("null")
	}

	return []byte(tasks.TaskQueue[0])
}

func (h *HTLC) deleteTask(stub shim.ChaincodeStubInterface, hash string) {
	t, err := stub.GetState(TaskKey)
	checkError(err)

	if t == nil {
		panic(errors.New("there is no task"))
	}

	var tasks Tasks
	err = json.Unmarshal(t, &tasks)
	checkError(err)

	if tasks.Num == 0 {
		panic(errors.New("there is no task"))
	}

	if tasks.TaskQueue[0] != hash {
		panic(errors.New("invalid operation"))
	}

	if tasks.Num == 1 {
		tasks.TaskQueue = []string{}
	} else {
		tasks.TaskQueue = tasks.TaskQueue[1:]
	}
	tasks.Num--
	t, err = json.Marshal(&tasks)
	checkError(err)

	err = stub.PutState(TaskKey, t)
	checkError(err)
}

func (h *HTLC) getSender(stub shim.ChaincodeStubInterface, hash string) []byte {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	return []byte(cd.Sender)
}

func (h *HTLC) getReceiver(stub shim.ChaincodeStubInterface, hash string) []byte {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	return []byte(cd.Receiver)
}

func (h *HTLC) getAmount(stub shim.ChaincodeStubInterface, hash string) uint64 {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	return cd.Amount
}

func (h *HTLC) getTimelock(stub shim.ChaincodeStubInterface, hash string) uint64 {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	return cd.Timelock
}

func (h *HTLC) getLockStatus(stub shim.ChaincodeStubInterface, hash string) bool {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	return cd.Locked
}

func (h *HTLC) getUnlockStatus(stub shim.ChaincodeStubInterface, hash string) bool {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	return cd.Unlocked
}

func (h *HTLC) getRollbackStatus(stub shim.ChaincodeStubInterface, hash string) bool {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	return cd.Rolledback
}

func (h *HTLC) setLockStatus(stub shim.ChaincodeStubInterface, hash string) {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	cd.Locked = true
	h.setSelfContractData(stub, hash, &cd)
}

func (h *HTLC) setUnlockStatus(stub shim.ChaincodeStubInterface, hash string) {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	cd.Unlocked = true
	h.setSelfContractData(stub, hash, &cd)
}

func (h *HTLC) setRollbackStatus(stub shim.ChaincodeStubInterface, hash string) {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	cd.Rolledback = true
	h.setSelfContractData(stub, hash, &cd)
}

func (h *HTLC) getSelfTimelock(stub shim.ChaincodeStubInterface, hash string) []byte {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	return uint64ToBytes(cd.Timelock)
}

func (h *HTLC) getCounterpartyTimelock(stub shim.ChaincodeStubInterface, hash string) []byte {
	var cd ContractData
	h.getCounterpartyContractData(stub, hash, &cd)
	return uint64ToBytes(cd.Timelock)
}

func (h *HTLC) getSelfLockStatus(stub shim.ChaincodeStubInterface, hash string) []byte {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	return boolToBytes(cd.Locked)
}

func (h *HTLC) getCounterpartyLockStatus(stub shim.ChaincodeStubInterface, hash string) []byte {
	var cd ContractData
	h.getCounterpartyContractData(stub, hash, &cd)
	return boolToBytes(cd.Locked)
}

func (h *HTLC) setCounterpartyLockStatus(stub shim.ChaincodeStubInterface, hash string) {
	var cd ContractData
	h.getCounterpartyContractData(stub, hash, &cd)
	cd.Locked = true
	h.setCounterpartyContractData(stub, hash, &cd)
}

func (h *HTLC) getSelfUnlockStatus(stub shim.ChaincodeStubInterface, hash string) []byte {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	return boolToBytes(cd.Unlocked)
}

func (h *HTLC) getCounterpartyUnlockStatus(stub shim.ChaincodeStubInterface, hash string) []byte {
	var cd ContractData
	h.getCounterpartyContractData(stub, hash, &cd)
	return boolToBytes(cd.Unlocked)
}

func (h *HTLC) setCounterpartyUnlockStatus(stub shim.ChaincodeStubInterface, hash string) {
	var cd ContractData
	h.getCounterpartyContractData(stub, hash, &cd)
	cd.Unlocked = true
	h.setCounterpartyContractData(stub, hash, &cd)
}

func (h *HTLC) getSelfRollbackStatus(stub shim.ChaincodeStubInterface, hash string) []byte {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	return boolToBytes(cd.Rolledback)
}

func (h *HTLC) getCounterpartyRollbackStatus(stub shim.ChaincodeStubInterface, hash string) []byte {
	var cd ContractData
	h.getCounterpartyContractData(stub, hash, &cd)
	return boolToBytes(cd.Rolledback)
}

func (h *HTLC) setCounterpartyRollbackStatus(stub shim.ChaincodeStubInterface, hash string) {
	var cd ContractData
	h.getCounterpartyContractData(stub, hash, &cd)
	cd.Rolledback = true
	h.setCounterpartyContractData(stub, hash, &cd)
}

func (h *HTLC) getSelfContractData(stub shim.ChaincodeStubInterface, hash string, cd *ContractData) {
	var cdInfo []byte
	var err error
	if h.getRole(stub, hash) {
		cdInfo, err = stub.GetState(getInitiatorKey(hash))
	} else {
		cdInfo, err = stub.GetState(getParticipantKey(hash))
	}
	checkError(err)
	err = json.Unmarshal(cdInfo, &cd)
	checkError(err)
}

func (h *HTLC) setSelfContractData(stub shim.ChaincodeStubInterface, hash string, cd *ContractData) {
	cdInfo, err := json.Marshal(cd)
	if h.getRole(stub, hash) {
		err = stub.PutState(getInitiatorKey(hash), cdInfo)
	} else {
		err = stub.PutState(getParticipantKey(hash), cdInfo)
	}
	checkError(err)
}

func (h *HTLC) getCounterpartyContractData(stub shim.ChaincodeStubInterface, hash string, cd *ContractData) {
	var cdInfo []byte
	var err error
	if !h.getRole(stub, hash) {
		cdInfo, err = stub.GetState(getInitiatorKey(hash))
	} else {
		cdInfo, err = stub.GetState(getParticipantKey(hash))
	}
	checkError(err)
	err = json.Unmarshal(cdInfo, &cd)
	checkError(err)
}

func (h *HTLC) setCounterpartyContractData(stub shim.ChaincodeStubInterface, hash string, cd *ContractData) {
	cdInfo, err := json.Marshal(cd)
	if !h.getRole(stub, hash) {
		err = stub.PutState(getInitiatorKey(hash), cdInfo)
	} else {
		err = stub.PutState(getParticipantKey(hash), cdInfo)
	}
	checkError(err)
}

func getInitiatorKey(hash string) string {
	return fmt.Sprintf(InitiatorKeyPrefix, hash)
}

func getParticipantKey(hash string) string {
	return fmt.Sprintf(ParticipantKeyPrefix, hash)
}

func taskIsExisted(stub shim.ChaincodeStubInterface, hash string) bool {
	return isExisted(stub, hash) &&
		isExisted(stub, getInitiatorKey(hash)) &&
		isExisted(stub, getParticipantKey(hash))
}

func isExisted(stub shim.ChaincodeStubInterface, key string) bool {
	value, err := stub.GetState(key)
	checkError(err)

	return value != nil
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
	return t0 > (t1+3600) && t1 > (uint64(timeStamp.Seconds)+3600)
}
