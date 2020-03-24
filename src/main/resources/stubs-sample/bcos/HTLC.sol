pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

contract HTLC {

    struct ContractData {
        string secret;
        string sender;
        string receiver;
        uint amount;
        uint timelock; // UNIX timestamp seconds - locked UNTIL this time
        bool locked;
        bool unlocked;
        bool rolledback;
    }

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

    /* to be defined*/
    // function lock(string _hash) external returns (string);
    // function unlock(string _hash) external returns (string);
    // function rollback(string _hash) external returns (string);

    function newContract(string[] _ss)
    public
    returns (string[] result)
    {
        result = new string[](1);
        if (taskIsExisted(_ss[0])) {
            result[0] = "task exists";
            return;
        }

        if(!rightTimelock(_ss[6], _ss[10])) {
            result[0] = "illegal timelocks";
            return;
        }

        if(equal(_ss[1], "true")) {
           htlcRoles[_ss[0]] = true;
        } else {
           htlcRoles[_ss[0]] = false;
        }

        if(htlcRoles[_ss[0]]) {
            if(!hashMatched(_ss[0], _ss[2])) {
                result[0] = "hash not matched";
                return;
            }
        }

        initiators[_ss[0]] = ContractData(
            _ss[2],
            _ss[3],
            _ss[4],
            stringToUint(_ss[5]),
            stringToUint(_ss[6]),
            false,
            false,
            false
        );

        participants[_ss[0]] = ContractData(
            "null",
            _ss[7],
            _ss[8],
            stringToUint(_ss[9]),
            stringToUint(_ss[10]),
            false,
            false,
            false
        );

        addTask(_ss[0]);
        result[0] = "success";
    }

    function setSecret(string _hash, string _secret)
    internal
    {
        if(!htlcRoles[_hash]) {
            participants[_hash].secret = _secret;
        }
    }

    function getSecret(string[] _ss)
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        if(htlcRoles[_ss[0]]) {
            result[0] = initiators[_ss[0]].secret;
        } else {
            result[0] = participants[_ss[0]].secret;
        }
    }

    function addTask(string _task)
    internal
    {

        tail = tail + 1;
        taskQueue.push(_task);
    }

    function getTask(string[] _ss)
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
            return initiators[_hash].amount;
        } else {
            return participants[_hash].amount;
        }
    }

    function getTimelock(string _hash)
    internal
    view
    returns (uint)
    {
        if(htlcRoles[_hash]) {
            return initiators[_hash].timelock;
        } else {
            return participants[_hash].timelock;
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
            result[0] = uintToString(initiators[_ss[0]].timelock);
        } else {
            result[0] = uintToString(participants[_ss[0]].timelock);
        }
    }

    function getCounterpartyTimelock(string[] _ss)
    public
    view
    returns (string[] result)
    {
        result = new string[](1);
        if(!htlcRoles[_ss[0]]) {
            result[0] = uintToString(initiators[_ss[0]].timelock);
        } else {
            result[0] = uintToString(participants[_ss[0]].timelock);
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
        return (initiators[_hash].amount > 0 &&
                participants[_hash].amount > 0);
    }

    function hasInitiator(string _hash)
    internal
    view
    returns (bool)
    {
        return (initiators[_hash].amount > 0);
    }

    function hasParticipant(string _hash)
    internal
    view
    returns (bool)
    {
        return (participants[_hash].amount > 0);
    }

    function rightTimelock(string _t0, string _t1)
    internal
    view
    returns (bool)
    {
        uint t0 = stringToUint(_t0);
        uint t1 = stringToUint(_t1);
        return t0 > (t1 + 3600) && t1 > (now / 1000 + 3600);
    }

    function equal(string a, string b)
    internal
    pure
    returns (bool)
    {
        return keccak256(abi.encodePacked(a)) == keccak256(abi.encodePacked(b));
    }

    function stringToBytes32(string _source)
    internal
    pure
    returns (bytes32 result)
    {
        bytes memory tempEmptyString = bytes(_source);
        if (tempEmptyString.length == 0) {
            return 0x0;
        }

        assembly {
            result := mload(add(_source, 32))
        }
    }

    function bytes32ToString(bytes32 _source)
    internal
    pure
    returns (string)
    {

       bytes memory result = new bytes(_source.length);

       for(uint i = 0; i < _source.length; i++) {

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
        for (uint i = 0; i < b.length; i++) {
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
        for (uint i=0; i<ss.length/2; ++i) {
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
