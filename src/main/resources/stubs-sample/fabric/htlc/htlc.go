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
	TaskKey                  = "HTLCTask"
	InitiatorKeyPrefix       = "I-%s"
	ParticipantKeyPrefix     = "P-%s"
	CounterpartyHtlcIpathKey = "CounterpartyHtlcIpath"
	CounterpartyHtlcNameKey  = "CounterpartyHtlcName"
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

func (h *HTLC) setCounterpartyHTLCInfo(stub shim.ChaincodeStubInterface, counterpartyHTLCIpath, counterpartyHTLCName string) {
	err := stub.PutState(CounterpartyHtlcIpathKey, []byte(counterpartyHTLCIpath))
	checkError(err)

	err = stub.PutState(CounterpartyHtlcNameKey, []byte(counterpartyHTLCName))
	checkError(err)
}

func (h *HTLC) getCounterpartyHTLCIpath(stub shim.ChaincodeStubInterface) []byte {
	result, err := stub.GetState(CounterpartyHtlcIpathKey)
	checkError(err)

	return result
}

func (h *HTLC) getCounterpartyHTLCName(stub shim.ChaincodeStubInterface) []byte {
	result, err := stub.GetState(CounterpartyHtlcNameKey)
	checkError(err)

	return result
}

func (h *HTLC) setRole(stub shim.ChaincodeStubInterface, hash, role string) {
	if isExisted(stub, hash) {
		panic(errors.New("hash exists"))
	}

	err := stub.PutState(hash, []byte(role))
	checkError(err)
}

func (h *HTLC) getRole(stub shim.ChaincodeStubInterface, hash string) bool {
	role, err := stub.GetState(hash)
	checkError(err)

	if role == nil {
		panic(errors.New("hash not found"))
	}

	if string(role) == "true" {
		return true
	}
	return false
}

func (h *HTLC) addInitiator(stub shim.ChaincodeStubInterface, hash, secret, sender, receiver, amount, timelock string) {
	if isExisted(stub, getInitiatorKey(hash)) {
		panic(errors.New("hash exists"))
	}

	if !hashMatched(hash, secret) {
		panic(errors.New("hash not matched"))
	}

	var cd = &ContractData{
		Secret:     secret,
		Sender:     sender,
		Receiver:   receiver,
		Amount:     stringToUint64(amount),
		Timelock:   stringToUint64(timelock),
		Locked:     false,
		Unlocked:   false,
		Rolledback: false,
	}

	cdInfo, err := json.Marshal(cd)
	checkError(err)

	err = stub.PutState(getInitiatorKey(hash), cdInfo)
	checkError(err)
}

func (h *HTLC) addParticipant(stub shim.ChaincodeStubInterface, hash, sender, receiver, amount, timelock string) {
	if isExisted(stub, getParticipantKey(hash)) {
		panic(errors.New("hash exists"))
	}

	var cd = &ContractData{
		Secret:     "null",
		Sender:     sender,
		Receiver:   receiver,
		Amount:     stringToUint64(amount),
		Timelock:   stringToUint64(timelock),
		Locked:     false,
		Unlocked:   false,
		Rolledback: false,
	}

	cdInfo, err := json.Marshal(cd)
	checkError(err)

	err = stub.PutState(getParticipantKey(hash), cdInfo)
	checkError(err)
}

func (h *HTLC) addTask(stub shim.ChaincodeStubInterface, hash string) {
	if !taskIsExisted(stub, hash) {
		panic(errors.New("illegal task"))
	}

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

func (h *HTLC) getSecret(stub shim.ChaincodeStubInterface, hash string) []byte {
	var cd ContractData
	h.getSelfContractData(stub, hash, &cd)
	return []byte(cd.Secret)
}

func (h *HTLC) setSecret(stub shim.ChaincodeStubInterface, hash, secret string) {
	if !hashMatched(hash, secret) {
		panic(errors.New("hash not matched"))
	}

	if !h.getRole(stub, hash) {
		cdInfo, err := stub.GetState(getParticipantKey(hash))
		checkError(err)

		var cd ContractData
		err = json.Unmarshal(cdInfo, &cd)
		checkError(err)

		cd.Secret = secret
		cdInfo, err = json.Marshal(&cd)
		checkError(err)

		err = stub.PutState(getParticipantKey(hash), cdInfo)
		checkError(err)
	}
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