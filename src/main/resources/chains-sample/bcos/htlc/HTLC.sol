pragma experimental ABIEncoderV2;

contract HTLC {

    struct ContractData {
        string secret;
        string sender;
        string receiver;
        string amount;
        string timelock; // UNIX timestamp seconds - locked UNTIL this time
        bool locked;
        bool unlocked;
        bool rolledback;
    }

    string counterpartyHtlc;

    mapping(string => string) lockTxInfos;

    mapping(string => string) newContractTxInfos;

    // recode if you're the initiator
    mapping(string => bool) htlcRoles;

    // initiator is the one who initiates the htlc transaction
    mapping(string => ContractData) initiators;

    // participant is the one who makes the deal with initiator
    mapping(string => ContractData) participants;

    // record all unfinished tasks
    uint head = 0;        // point to the current task to be performed
    uint tail = 0;        // point to the next position for added task
    string[] taskQueue;

    bool isInited = false;

    function init(string[] _ss)
    public
    returns (string[] result)
    {
        result = new string[](1);
        if(isInited) {
            result[0] = "cannot init repeatedly";
            return;
        }
        counterpartyHtlc = _ss[0];
        isInited = true;
        result[0] = "success";
    }

    function newContract(string[] _ss)
    public
    returns (string[] result)
    {
        result = new string[](1);
        if (taskIsExisted(_ss[0])) {
            result[0] = "hash as contract-id existed";
            return;
        }

        if(!rightTimelock(_ss[5], _ss[9])) {
            result[0] = "illegal timelocks";
            return;
        }

        if(equal(_ss[1], "true")) {
           htlcRoles[_ss[0]] = true;
          if(stringToAddress(_ss[2]) != tx.origin) {
                result[0] = "only sender can new a contract";
                return;
          }

        } else {
           htlcRoles[_ss[0]] = false;
            if(stringToAddress(_ss[6]) != tx.origin) {
                result[0] = "only sender can new a contract";
                return;
          }
        }

        initiators[_ss[0]] = ContractData(
            "null",
            _ss[2],
            _ss[3],
            _ss[4],
            _ss[5],
            false,
            false,
            false
        );

        participants[_ss[0]] = ContractData(
            "null",
            _ss[6],
            _ss[7],
            _ss[8],
            _ss[9],
            false,
            false,
            false
        );

        addTask(_ss[0]);
        result[0] = "success";
    }

    function setNewContractTxInfo(string[] _ss)
    public
    {
        newContractTxInfos[_ss[0]] = string(abi.encodePacked(
            _ss[1],
            " ",
            _ss[2])
            );
    }

    function getNewContractTxInfo(string[] _ss)
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        string memory info = newContractTxInfos[_ss[0]];
        if(bytes(info).length == 0) {
            result[0] = "null";
        } else {
            result[0] = info;
        }
    }

    function getContract(string[] _ss)
    public
    view
    returns (string[] result)
    {
        string memory _hash = _ss[0];
        result = new string[](1);

        if (!taskIsExisted(_hash))
        {
           result[0] = "contract not exists";
           return;
        }

        result[0] = string(abi.encodePacked(
            initiators[_hash].sender,
            " ",
            initiators[_hash].receiver,
            " ",
            initiators[_hash].amount,
            " ",
            initiators[_hash].timelock,
            " ",
            participants[_hash].sender,
            " ",
            participants[_hash].receiver,
            " ",
            participants[_hash].amount,
            " ",
            participants[_hash].timelock)
            );
    }

    function setSecret(string[] _ss)
    public
    returns (string[] result)
    {
        result = new string[](1);
        if(!hashMatched(_ss[0], _ss[1])) {
            result[0] = "hash not matched";
            return;
        }

        if(htlcRoles[_ss[0]]) {
            initiators[_ss[0]].secret = _ss[1];
        } else {
            participants[_ss[0]].secret = _ss[1];
        }
        result[0] = "success";
    }

    function getSecret(string[] _ss)
    public
    returns (string[] result)
    {
        result = new string[](1);
        if(htlcRoles[_ss[0]]) {
            result[0] = initiators[_ss[0]].secret;
        } else {
            result[0] = participants[_ss[0]].secret;
        }
    }

    // please override it
    function lock(string[] _ss)
    public
    returns (string[] result)
    {
        string memory _hash = _ss[0];
        result = new string[](1);

        if (!taskIsExisted(_hash))
        {
           result[0] = "task not exists";
           return;
        }

        if(getLockStatus(_hash)) {
            result[0] = "done";
            return;
        }

        uint timelock = getTimelock(_hash);
        if(getRollbackStatus(_hash) || timelock <= (now / 1000))
        {
            result[0] = "has rolled back";
            return;
        }

        result[0] = "continue";
    }

    // please override it
    function unlock(string[] _ss)
    public
    returns (string[] result)
    {
        string memory _hash = _ss[0];
        string memory _secret = _ss[1];
        result = new string[](1);

        if (!taskIsExisted(_hash))
        {
           result[0] = "task not exists";
           return;
        }

        if (getUnlockStatus(_hash))
        {
           result[0] = "done";
           return;
        }

        if(!hashMatched(_hash, _secret))
        {
           result[0] = "hash not matched";
           return;
        }

        if (!getLockStatus(_hash))
        {
           result[0] = "can not unlock until lock is done";
           return;
        }

        uint timelock = getTimelock(_hash);
        if(getRollbackStatus(_hash) || timelock <= (now / 1000))
        {
            result[0] = "has rolled back";
            return;
        }

        result[0] = "continue";
    }

    // please override it
    function rollback(string[] _ss)
    public
    returns (string[] result)
    {
        string memory _hash = _ss[0];
        result = new string[](1);

        if (!taskIsExisted(_hash))
        {
           result[0] = "task not exists";
           return;
        }

        if (getRollbackStatus(_hash))
        {
           result[0] = "done";
           return;
        }

        uint timelock = getTimelock(_hash);
        if(timelock > (now / 1000))
        {
            result[0] = "not_yet";
            return;
        }

        if (!getLockStatus(_hash))
        {
           result[0] = "no need to rollback unless lock is done";
           return;
        }

        if (getUnlockStatus(_hash))
        {
           result[0] = "can not rollback if unlock is done";
           return;
        }

        result[0] = "continue";
    }


    function setLockTxInfo(string[] _ss)
    public
    {
        lockTxInfos[_ss[0]] = string(abi.encodePacked(
            _ss[1],
            " ",
            _ss[2])
            );
    }

    function getLockTxInfo(string[] _ss)
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        string memory info = lockTxInfos[_ss[0]];
        if(bytes(info).length == 0) {
            result[0] = "null";
        } else {
            result[0] = info;
        }
    }

    function getCounterpartyHtlc()
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        result[0] = counterpartyHtlc;
    }

    function addTask(string _task)
    internal
    {

        tail = tail + 1;
        taskQueue.push(_task);
    }

    function getTask()
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        if(head == tail) {
            result[0] = ("null");
        } else {
            result[0] = (taskQueue[uint(head)]);
        }
    }

    function deleteTask(string[] _ss)
    public
    returns (string[] result)
    {
        result = new string[](1);
        if(head == tail || !equal(taskQueue[head], _ss[0])) {
            result[0] = "invalid operation";
            return;
        }
        head = head + 1;
        result[0] = "success";
    }

    function getTaskIndex()
    external
    view
    returns (uint, uint)
    {
        return (head, tail);
    }

    function getSender(string _hash)
    internal
    view
    returns (address)
    {
        if(htlcRoles[_hash]) {
            return stringToAddress(initiators[_hash].sender);
        } else {
            return stringToAddress(participants[_hash].sender);
        }
    }

    function getReceiver(string _hash)
    internal
    view
    returns (address)
    {
        if(htlcRoles[_hash]) {
            return stringToAddress(initiators[_hash].receiver);
        } else {
            return stringToAddress(participants[_hash].receiver);
        }
    }

    function getAmount(string _hash)
    internal
    view
    returns (uint)
    {
        if(htlcRoles[_hash]) {
            return stringToUint(initiators[_hash].amount);
        } else {
            return stringToUint(participants[_hash].amount);
        }
    }

    function getTimelock(string _hash)
    internal
    view
    returns (uint)
    {
        if(htlcRoles[_hash]) {
            return stringToUint(initiators[_hash].timelock);
        } else {
            return stringToUint(participants[_hash].timelock);
        }
    }

    function getLockStatus(string _hash)
    internal
    view
    returns (bool)
    {
        if(htlcRoles[_hash]) {
            return initiators[_hash].locked;
        } else {
            return participants[_hash].locked;
        }
    }

    function getUnlockStatus(string _hash)
    internal
    view
    returns (bool)
    {
        if(htlcRoles[_hash]) {
            return initiators[_hash].unlocked;
        } else {
            return participants[_hash].unlocked;
        }
    }

    function getRollbackStatus(string _hash)
    internal
    view
    returns (bool)
    {
        if(htlcRoles[_hash]) {
            return initiators[_hash].rolledback;
        } else {
            return participants[_hash].rolledback;
        }
    }

    function setLockStatus(string _hash)
    internal
    {
        if(htlcRoles[_hash]) {
            initiators[_hash].locked = true;
        } else {
            participants[_hash].locked = true;
        }
    }

    function setUnlockStatus(string _hash)
    internal
    {
        if(htlcRoles[_hash]) {
            initiators[_hash].unlocked = true;
        } else {
            participants[_hash].unlocked = true;
        }
    }

    function setRollbackStatus(string _hash)
    internal
    {
        if(htlcRoles[_hash]) {
            initiators[_hash].rolledback = true;
        } else {
            participants[_hash].rolledback = true;
        }
    }

     // these following functions are just for HTLC scheduler
    function getSelfTimelock(string[] _ss)
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        if(htlcRoles[_ss[0]]) {
            result[0] = initiators[_ss[0]].timelock;
        } else {
            result[0] = participants[_ss[0]].timelock;
        }
    }

    function getCounterpartyTimelock(string[] _ss)
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        if(!htlcRoles[_ss[0]]) {
            result[0] = initiators[_ss[0]].timelock;
        } else {
            result[0] = participants[_ss[0]].timelock;
        }
    }

    function getSelfLockStatus(string[] _ss)
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        if(htlcRoles[_ss[0]]) {
            if(initiators[_ss[0]].locked) {
                result[0] = "true";
            } else {
                result[0] = "false";
            }
        } else {
            if(participants[_ss[0]].locked) {
                result[0] = "true";
            } else {
                result[0] = "false";
            }
        }
    }

    function getCounterpartyLockStatus(string[] _ss)
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        if(!htlcRoles[_ss[0]]) {
            if(initiators[_ss[0]].locked) {
                result[0] = "true";
            } else {
                result[0] = "false";
            }
        } else {
            if(participants[_ss[0]].locked) {
                result[0] = "true";
            } else {
                result[0] = "false";
            }
        }
    }

    function setCounterpartyLockStatus(string[] _ss)
    public
    {
        if(!htlcRoles[_ss[0]]) {
            initiators[_ss[0]].locked = true;
        } else {
            participants[_ss[0]].locked = true;
        }
    }

    function getSelfUnlockStatus(string[] _ss)
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        if(htlcRoles[_ss[0]]) {
            if(initiators[_ss[0]].unlocked) {
                result[0] = "true";
            } else {
                result[0] = "false";
            }
        } else {
            if(participants[_ss[0]].unlocked) {
                result[0] = "true";
            } else {
                result[0] = "false";
            }
        }
    }

    function getCounterpartyUnlockStatus(string[] _ss)
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        if(!htlcRoles[_ss[0]]) {
            if(initiators[_ss[0]].unlocked) {
                result[0] = "true";
            } else {
                result[0] = "false";
            }
        } else {
            if(participants[_ss[0]].unlocked) {
                result[0] = "true";
            } else {
                result[0] = "false";
            }
        }
    }

    function setCounterpartyUnlockStatus(string[] _ss)
    public
    {
        if(!htlcRoles[_ss[0]]) {
            initiators[_ss[0]].unlocked = true;
        } else {
            participants[_ss[0]].unlocked = true;
        }
    }

    function getSelfRollbackStatus(string[] _ss)
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        if(htlcRoles[_ss[0]]) {
            if(initiators[_ss[0]].rolledback) {
                result[0] = "true";
            } else {
                result[0] = "false";
            }
        } else {
            if(participants[_ss[0]].rolledback) {
                result[0] = "true";
            } else {
                result[0] = "false";
            }
        }
    }

    function getCounterpartyRollbackStatus(string[] _ss)
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        if(!htlcRoles[_ss[0]]) {
            if(initiators[_ss[0]].rolledback) {
                result[0] = "true";
            } else {
                result[0] = "false";
            }
        } else {
            if(participants[_ss[0]].rolledback) {
                result[0] = "true";
            } else {
                result[0] = "false";
            }
        }
    }

    function setCounterpartyRollbackStatus(string[] _ss)
    public
    {
        if(!htlcRoles[_ss[0]]) {
            initiators[_ss[0]].rolledback = true;
        } else {
            participants[_ss[0]].rolledback = true;
        }
    }

    // these are utilities
    function taskIsExisted(string _hash)
    internal
    view
    returns (bool)
    {
        return (bytes(initiators[_hash].sender).length > 0 &&
                bytes(participants[_hash].sender).length > 0);
    }

    function rightTimelock(string _t0, string _t1)
    internal
    view
    returns (bool)
    {
        uint t0 = stringToUint(_t0);
        uint t1 = stringToUint(_t1);
        return t0 > (t1 + 200) && t1 > (now / 1000 + 200);
    }

    function equal(string a, string b)
    internal
    pure
    returns (bool)
    {
        return keccak256(abi.encodePacked(a)) == keccak256(abi.encodePacked(b));
    }

    function bytes32ToString(bytes32 _source)
    internal
    pure
    returns (string)
    {

       bytes memory result = new bytes(_source.length);

       uint len = _source.length;
       for(uint i = 0; i < len; i++) {

           result[i] = _source[i];
       }

       return string(result);
    }

    function stringToAddress(string _address)
    internal
    pure
    returns (address)
    {
        bytes memory temp = bytes(_address);
        uint160 result = 0;
        uint160 b1;
        uint160 b2;
        for (uint i = 2; i < 2 + 2 * 20; i += 2) {
            result *= 256;
            b1 = uint160(uint8(temp[i]));
            b2 = uint160(uint8(temp[i + 1]));
            if ((b1 >= 97) && (b1 <= 102)) {
                b1 -= 87;
            } else if ((b1 >= 65) && (b1 <= 70)) {
                b1 -= 55;
            } else if ((b1 >= 48) && (b1 <= 57)) {
            b1 -= 48;
            }

            if ((b2 >= 97) && (b2 <= 102)) {
                b2 -= 87;
            } else if ((b2 >= 65) && (b2 <= 70)) {
                b2 -= 55;
            } else if ((b2 >= 48) && (b2 <= 57)) {
                b2 -= 48;
            }
            result += (b1 * 16 + b2);
        }
        return address(result);
    }

    // function addressToString(address _address)
    // internal
    // pure
    // returns (string)
    // {
    //     bytes memory result = new bytes(40);
    //     for (uint i = 0; i < 20; i++) {
    //         byte temp = byte(uint8(uint(_address) / (2 ** (8 * (19 - i)))));
    //         byte b1 = byte(uint8(temp) / 16);
    //         byte b2 = byte(uint8(temp) - 16 * uint8(b1));
    //         result[2 * i] = convert(b1);
    //         result[2 * i + 1] = convert(b2);
    //     }
    //     return string(abi.encodePacked("0x", string(result)));
    // }

    function convert(byte _byte)
    private
    pure
    returns (byte)
    {
        if (_byte < 10) {
            return byte(uint8(_byte) + 0x30);
        } else {
            return byte(uint8(_byte) + 0x57);
        }
    }

    function uintToString(uint _value)
    internal
    pure
    returns (string)
    {
        bytes32 result;
        if (_value == 0) {
            return "0";
        } else {
            while (_value > 0) {
                result = bytes32(uint(result) / (2 ** 8));
                result |= bytes32(((_value % 10) + 48) * 2 ** (8 * 31));
                _value /= 10;
            }
        }
        return bytes32ToString(result);
    }

    function stringToUint(string _s)
    internal
    pure
    returns (uint)
    {
        bytes memory b = bytes(_s);
        uint result = 0;
        uint len = b.length;
        for (uint i = 0; i < len; i++) {
            if (b[i] >= 48 && b[i] <= 57) {
                result = result * 10 + (uint(b[i]) - 48);
            }
        }
        return result;
    }

    function hashMatched(string _hash, string _secret)
    public
    pure
    returns (bool)
    {
        bytes memory a  = abi.encodePacked(sha256(abi.encodePacked(_secret)));
        bytes memory b  = hexStringToBytes(_hash);
        return sha256(a) == sha256(b);
    }

    function hexStringToBytes(string s)
    public
    pure
    returns (bytes)
    {
        bytes memory ss = bytes(s);
        require(ss.length%2 == 0);
        bytes memory r = new bytes(ss.length/2);
        uint len = ss.length/2;
        for (uint i = 0; i < len; ++i) {
            r[i] = byte(fromHexChar(uint(ss[2*i])) * 16 +
                        fromHexChar(uint(ss[2*i+1])));
        }
        return r;
    }

    function fromHexChar(uint c)
    public
    pure
    returns (uint)
    {
        if (byte(c) >= byte('0') && byte(c) <= byte('9')) {
            return c - uint(byte('0'));
        }
        if (byte(c) >= byte('a') && byte(c) <= byte('f')) {
            return 10 + c - uint(byte('a'));
        }
        if (byte(c) >= byte('A') && byte(c) <= byte('F')) {
            return 10 + c - uint(byte('A'));
        }
    }
}
