pragma solidity >=0.4.22 <0.6.0;
pragma experimental ABIEncoderV2;

contract HTLC {

    struct ProposalData {
        string secret;
        string sender;
        string receiver;
        string amount;
        string timelock; // UNIX timestamp seconds - locked UNTIL this time
        bool locked;
        bool unlocked;
        bool rolledback;
    }

    string counterpartyHtlcAddress;

    mapping(string => string) newProposalTxInfos;

    // recode if you're the initiator
    mapping(string => bool) htlcRoles;

    mapping(string => uint) proposalIndexs;

    // initiator is the one who initiates the htlc transaction
    mapping(string => ProposalData) initiators;

    // participant is the one who makes the deal with initiator
    mapping(string => ProposalData) participants;

    // record all unfinished proposals
    string[] proposalList;
    uint256 constant size = 1024;     // capacity of proposal list

    uint256[] public freeIndexStack;
    uint256 depth;             // current depth of free index stack [1 - size]

    string constant splitSymbol = "##";
    string constant nullFlag = "null";
    string constant successFlag = "success";

    constructor() public {
        proposalList = new string[](size);
        freeIndexStack = new uint[](size);
        depth = size;
        for(uint256 i = 0; i < size; i++) {
            proposalList[i] = nullFlag;
            freeIndexStack[i] = size - i - 1;
        }
    }

    /*
        @param: counterpartyHtlcAddress
    */
    function init(string[] memory _ss) public
    returns (string[] memory result)
    {
        result = new string[](1);
        counterpartyHtlcAddress = _ss[0];
        result[0] = successFlag;
    }

    /*  please override it
        @param: hash
    */
    function lock(string[] memory _ss) public
    returns (string[] memory result)
    {
        result = new string[](1);

        if (!proposalIsExisted(_ss[0])) {
           result[0] = "proposal not exists";
           return result;
        }

        if(getLockState(_ss[0])) {
            result[0] = "done";
            return result;
        }

        uint256 timelock = getTimelock(_ss[0]);
        if(getRollbackState(_ss[0]) || timelock <= (now / 1000)) {
            result[0] = "has rolled back";
            return result;
        }

        result[0] = "continue";
    }

    /*  please override it
        @param: hash | secret
    */
    function unlock(string[] memory _ss) public
    returns (string[] memory result)
    {
        string memory _hash = _ss[0];
        string memory _secret = _ss[1];
        result = new string[](1);

        if (!proposalIsExisted(_hash)) {
           result[0] = "proposal not exists";
           return result;
        }

        if (getUnlockState(_hash)) {
           result[0] = "done";
           return result;
        }

        if(!hashMatched(_hash, _secret)) {
           result[0] = "hash not matched";
           return result;
        }

        if (!getLockState(_hash)) {
           result[0] = "can not unlock until lock is done";
           return result;
        }

        uint256 timelock = getTimelock(_hash);
        if(getRollbackState(_hash) || timelock <= (now / 1000)) {
            result[0] = "has rolled back";
            return result;
        }

        result[0] = "continue";
    }

    /*  please override it
        @param: hash
    */
    function rollback(string[] memory _ss) public
    returns (string[] memory result)
    {
        result = new string[](1);

        if (!proposalIsExisted(_ss[0])) {
           result[0] = "proposal not exists";
           return result;
        }

        if (getRollbackState(_ss[0])) {
           result[0] = "done";
           return result;
        }

        uint256 timelock = getTimelock(_ss[0]);
        if(timelock > (now / 1000)) {
            result[0] = "not_yet";
            return result;
        }

        if (!getLockState(_ss[0])) {
           result[0] = "no need to rollback unless lock is done";
           return result;
        }

        if (getUnlockState(_ss[0])) {
           result[0] = "can not rollback if unlock is done";
           return result;
        }

        result[0] = "continue";
    }

    function getCounterpartyHtlcAddress() public view
    returns (string[] memory result)
    {
        result = new string[](1);
        result[0] = counterpartyHtlcAddress;
    }

     /*
        @param:
        hash | role |
        sender0 | receiver0 | amount0 | timelock0 |
        sender1 | receiver1 | amount1 | timelock1 |

    */
    function newProposal(string[] memory _ss) public
    returns (string[] memory result)
    {
        result = new string[](1);
        if(depth == 0) {
            result[0] = "the proposal queue is full, one moment please";
            return result;
        }

        if (proposalIsExisted(_ss[0])) {
            result[0] = "proposal existed";
            return result;
        }

        if(!rightTimelock(_ss[5], _ss[9])) {
            result[0] = "illegal timelocks";
            return result;
        }

        if(sameString(_ss[1], "true")) {
            htlcRoles[_ss[0]] = true;
            if(stringToAddress(_ss[2]) != tx.origin) {
                result[0] = "only sender can new a proposal";
                return result;
            }
            // initiator is not listed until secret is written
            preAddProposal(_ss[0]);

        } else {
            htlcRoles[_ss[0]] = false;
            if(stringToAddress(_ss[6]) != tx.origin) {
                result[0] = "only sender can new a proposal";
                return result;
            }
            preAddProposal(_ss[0]);
            addProposal(_ss[0]);
        }

        initiators[_ss[0]] = ProposalData(
            nullFlag,
            _ss[2],
            _ss[3],
            _ss[4],
            _ss[5],
            false,
            false,
            false
        );

        participants[_ss[0]] = ProposalData(
            nullFlag,
            _ss[6],
            _ss[7],
            _ss[8],
            _ss[9],
            false,
            false,
            false
        );
        result[0] = successFlag;
    }

    /*
        @param: hash | tx-hash | blockNum
    */
    function setNewProposalTxInfo(string[] memory _ss) public
    {
        newProposalTxInfos[_ss[0]] = string(abi.encodePacked(
            _ss[1],
            splitSymbol,
            _ss[2])
            );
    }

    /*
        @param: hash
    */
    function getNewProposalTxInfo(string[] memory _ss) public view
    returns (string[] memory result)
    {
        result = new string[](1);
        string memory info = newProposalTxInfos[_ss[0]];
        if(bytes(info).length == 0) {
            result[0] = nullFlag;
        } else {
            result[0] = info;
        }
    }

    /*
        @param: hash
    */
    function getNegotiatedData(string[] memory _ss) public view
    returns (string[] memory result)
    {
        result = new string[](1);

        if (!proposalIsExisted(_ss[0])) {
           result[0] = "proposal not exists";
           return result;
        }

        result[0] = string(abi.encodePacked(
            initiators[_ss[0]].sender,
            "##",
            initiators[_ss[0]].receiver,
            "##",
            initiators[_ss[0]].amount,
            "##",
            initiators[_ss[0]].timelock,
            "##",
            participants[_ss[0]].sender,
            "##",
            participants[_ss[0]].receiver,
            "##",
            participants[_ss[0]].amount,
            "##",
            participants[_ss[0]].timelock)
            );
    }

    /*
        @param: hash
    */
    function getProposalInfo(string[] memory _ss) public view
    returns (string[] memory result)
    {
        result = new string[](1);

        if (!proposalIsExisted(_ss[0])) {
           result[0] = nullFlag;
           return result;
        }

        if(htlcRoles[_ss[0]]) {
            result[0] = string(abi.encodePacked(
                "true##",
                initiators[_ss[0]].secret,
                "##",
                initiators[_ss[0]].timelock,
                "##",
                boolToString(initiators[_ss[0]].locked),
                "##",
                boolToString(initiators[_ss[0]].unlocked),
                "##",
                boolToString(initiators[_ss[0]].rolledback),
                "##",
                participants[_ss[0]].timelock,
                "##",
                boolToString(participants[_ss[0]].locked),
                "##",
                boolToString(participants[_ss[0]].unlocked),
                "##",
                boolToString(participants[_ss[0]].rolledback))
                );
        } else {
            result[0] = string(abi.encodePacked(
                "false##null##",
                participants[_ss[0]].timelock,
                "##",
                boolToString(participants[_ss[0]].locked),
                "##",
                boolToString(participants[_ss[0]].unlocked),
                "##",
                boolToString(participants[_ss[0]].rolledback),
                "##",
                initiators[_ss[0]].timelock,
                "##",
                boolToString(initiators[_ss[0]].locked),
                "##",
                boolToString(initiators[_ss[0]].unlocked),
                "##",
                boolToString(initiators[_ss[0]].rolledback))
                );
        }
    }

    /*
        @param: hash | secret
    */
    function setSecret(string[] memory _ss) public
    returns (string[] memory result)
    {
        result = new string[](1);
        if(!hashMatched(_ss[0], _ss[1])) {
            result[0] = "hash not matched";
            return result;
        }

        if(htlcRoles[_ss[0]]) {
            initiators[_ss[0]].secret = _ss[1];
        } else {
            participants[_ss[0]].secret = _ss[1];
        }

        addProposal(_ss[0]);
        result[0] = successFlag;
    }

    function preAddProposal(string memory _id) internal
    {
        uint256 index = freeIndexStack[depth - 1];
        depth = depth - 1;
        proposalIndexs[_id] = index;

    }

    function addProposal(string memory _id) internal
    {
        uint256 index = proposalIndexs[_id];
        proposalList[index] = _id;
    }

    function getProposalIDs() public view
    returns (string[] memory result)
    {
        result = new string[](1);
        result[0] = proposalList[0];
        for(uint256 i = 1; i < size; i++) {
            result[0] = string(abi.encodePacked(result[0], splitSymbol, proposalList[i]));
        }
    }

    /*
        @param: proposal id
    */
    function deleteProposalID(string[] memory _ss) public
    returns (string[] memory result)
    {
        result = new string[](1);
        uint256 index = proposalIndexs[_ss[0]];

        if(!sameString(proposalList[index], _ss[0])) {
            result[0] = "invalid operation";
            return result;
        }

        proposalList[index] = nullFlag;
        freeIndexStack[depth] = index;
        depth = depth + 1;
        result[0] = successFlag;
    }

    function getIndex(string memory _hash) public view
    returns (uint256, uint256)
    {
        return (proposalIndexs[_hash], depth);
    }

    /*
        @param: hash
    */
    function setCounterpartyLockState(string[] memory _ss) public
    {
        if(!htlcRoles[_ss[0]]) {
            initiators[_ss[0]].locked = true;
        } else {
            participants[_ss[0]].locked = true;
        }
    }

    /*
        @param: hash
    */
    function setCounterpartyUnlockState(string[] memory _ss) public
    {
        if(!htlcRoles[_ss[0]]) {
            initiators[_ss[0]].unlocked = true;
        } else {
            participants[_ss[0]].unlocked = true;
        }
    }

    /*
        @param: hash
    */
    function setCounterpartyRollbackState(string[] memory _ss) public
    {
        if(!htlcRoles[_ss[0]]) {
            initiators[_ss[0]].rolledback = true;
        } else {
            participants[_ss[0]].rolledback = true;
        }
    }

     // the following functions are internal
    function getSender(string memory _hash) internal view
    returns (address)
    {
        if(htlcRoles[_hash]) {
            return stringToAddress(initiators[_hash].sender);
        } else {
            return stringToAddress(participants[_hash].sender);
        }
    }

    function getReceiver(string memory _hash) internal view
    returns (address)
    {
        if(htlcRoles[_hash]) {
            return stringToAddress(initiators[_hash].receiver);
        } else {
            return stringToAddress(participants[_hash].receiver);
        }
    }

    function getAmount(string memory _hash) internal view
    returns (uint)
    {
        if(htlcRoles[_hash]) {
            return stringToUint256(initiators[_hash].amount);
        } else {
            return stringToUint256(participants[_hash].amount);
        }
    }

    function getTimelock(string memory _hash) internal view
    returns (uint)
    {
        if(htlcRoles[_hash]) {
            return stringToUint256(initiators[_hash].timelock);
        } else {
            return stringToUint256(participants[_hash].timelock);
        }
    }

    function getLockState(string memory _hash) internal view
    returns (bool)
    {
        if(htlcRoles[_hash]) {
            return initiators[_hash].locked;
        } else {
            return participants[_hash].locked;
        }
    }

    function getUnlockState(string memory _hash) internal view
    returns (bool)
    {
        if(htlcRoles[_hash]) {
            return initiators[_hash].unlocked;
        } else {
            return participants[_hash].unlocked;
        }
    }

    function getRollbackState(string memory _hash) internal view
    returns (bool)
    {
        if(htlcRoles[_hash]) {
            return initiators[_hash].rolledback;
        } else {
            return participants[_hash].rolledback;
        }
    }

    function setLockState(string memory _hash) internal
    {
        if(htlcRoles[_hash]) {
            initiators[_hash].locked = true;
        } else {
            participants[_hash].locked = true;
        }
    }

    function setUnlockState(string memory _hash) internal
    {
        if(htlcRoles[_hash]) {
            initiators[_hash].unlocked = true;
        } else {
            participants[_hash].unlocked = true;
        }
    }

    function setRollbackState(string memory _hash) internal
    {
        if(htlcRoles[_hash]) {
            initiators[_hash].rolledback = true;
        } else {
            participants[_hash].rolledback = true;
        }
    }

    function hashMatched(string memory _hash, string memory _secret) internal pure
    returns (bool)
    {
        bytes memory a  = abi.encodePacked(sha256(abi.encodePacked(_secret)));
        bytes memory b  = hexStringToBytes(_hash);
        return sha256(a) == sha256(b);
    }

    function proposalIsExisted(string memory _hash) internal view
    returns (bool)
    {
        return (bytes(initiators[_hash].sender).length > 0 &&
                bytes(participants[_hash].sender).length > 0);
    }

    function rightTimelock(string memory _t0, string memory _t1) internal view
    returns (bool)
    {
        uint256 t0 = stringToUint256(_t0);
        uint256 t1 = stringToUint256(_t1);
        return t1 > (t0 + 200) && t0 > (now / 1000 + 200);
    }

    function sameString(string memory _str1, string memory _str2) internal pure
    returns (bool)
    {
        return keccak256(abi.encodePacked(_str1)) == keccak256(abi.encodePacked(_str2));
    }

    // these are utilities
    function stringToAddress(string memory _address) internal pure
    returns (address)
    {
        bytes memory temp = bytes(_address);
        uint160 result = 0;
        uint160 b1;
        uint160 b2;
        for (uint256 i = 2; i < 2 + 2 * 20; i += 2) {
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

    function stringToUint256(string memory _str) internal pure
    returns (uint256)
    {
        bytes memory bts = bytes(_str);
        uint256 result = 0;
        uint256 len = bts.length;
        for (uint256 i = 0; i < len; i++) {
            if (uint8(bts[i]) >= 48 && uint8(bts[i]) <= 57) {
                result = result * 10 + (uint8(bts[i]) - 48);
            }
        }
        return result;
    }

    function hexStringToBytes(string memory _hexStr) internal pure
    returns (bytes memory)
    {
        bytes memory bts = bytes(_hexStr);
        require(bts.length%2 == 0);
        bytes memory result = new bytes(bts.length/2);
        uint256 len = bts.length/2;
        for (uint256 i = 0; i < len; ++i) {
            result[i] = byte(fromHexChar(uint8(bts[2*i])) * 16 +
                fromHexChar(uint8(bts[2*i+1])));
        }
        return result;
    }

    function fromHexChar(uint8 _char) internal pure
    returns (uint8)
    {
        if (byte(_char) >= byte('0') && byte(_char) <= byte('9')) {
            return _char - uint8(byte('0'));
        }
        if (byte(_char) >= byte('a') && byte(_char) <= byte('f')) {
            return 10 + _char - uint8(byte('a'));
        }
        if (byte(_char) >= byte('A') && byte(_char) <= byte('F')) {
            return 10 + _char - uint8(byte('A'));
        }
    }

    function boolToString(bool _flag) internal pure
    returns (string memory)
    {
        if(_flag) {
            return "true";
        } else {
            return "flase";
        }
    }

    function uint256ToString(uint256 _value) internal pure
    returns (string memory)
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

    function bytes32ToString(bytes32 _bts32) internal pure
    returns (string memory)
    {

       bytes memory result = new bytes(_bts32.length);

       uint256 len = _bts32.length;
       for(uint256 i = 0; i < len; i++) {
           result[i] = _bts32[i];
       }

       return string(result);
    }
}
