pragma solidity ^0.4.25;

contract HTLCStorage {

    struct ConsensusInfo {
        bool isInitiator;
        address sender;
        address receiver;
        int amount;
        int exchange;
        int timelock0; // UNIX timestamp seconds - locked UNTIL this time
        int timelock1;
        bool locked0;
        bool locked1;
        bool unlocked0;
        bool unlocked1;
    }

    mapping(string => ConsensusInfo) consensusInfos;

    int head = 0;        // point to the current task to be performed
    int tail = 0;        // point to the next position for added task
    string[] taskQueue;  // record all unfinished tasks

    modifier hashMatched(string _hash, string _secret) {
        require(sha256(abi.encodePacked(stringToBytes32(_secret))) ==stringToBytes32(_hash), "hashMatched: the hash should not exist");
        _;
    }

    modifier hashExists(string _hash) {
        require(haveHash(_hash), "hashExists: the hash should exist");
        _;
    }

    modifier notNegative(int _num) {
        require(_num >= 0, "notNegative: can not be negative number");
        _;
    }

    modifier rightTimelock(int _t0, int _t1) {
        require(_t0 > _t1 && uint(_t1) > now, "rightTimelock: t0 must be bigger than t1");
        _;
    }

    modifier deleteQueueable(string _task) {
        require( head != tail && sameString(taskQueue[uint(head)], _task), "deleteQueueable: illegal operation");
        _;
    }

    function getContractAddress()
    internal
    view
    returns (string)
    {
       return addressToString(address(this));
    }

    function initInfo(
        int _isInitiator,
        string _hash,
        string _receiver,
        int _amount,
        int _exchange,
        int _timelock0,
        int _timelock1
    )
    external
    notNegative(_amount)
    notNegative(_exchange)
    rightTimelock(_timelock0, _timelock1)
    {
        if (haveHash(_hash))
            revert("Hash exists");

        bool isInitiator = true;
        if(_isInitiator == 0) {
            isInitiator = false;
        }

        consensusInfos[_hash] = ConsensusInfo(
            isInitiator,
            msg.sender,
            stringToAddress(_receiver),
            _amount,
            _exchange,
            _timelock0,
            _timelock1,
            false,
            false,
            false,
            false
        );

        // add task
        this.addTask(_hash);
    }

    function getIsInitiator(string _hash)
    external
    view
    hashExists(_hash)
    returns (int isInitiator)
    {
        if(consensusInfos[_hash].isInitiator)  {
            isInitiator = 1;
        }
        isInitiator  = 0;
    }

    function getSender(string _hash)
    external
    view
    hashExists(_hash)
    returns (address sender)
    {
        sender = consensusInfos[_hash].sender;
    }

    function getReceiver(string _hash)
    external
    view
    hashExists(_hash)
    returns (address receiver)
    {
        receiver = consensusInfos[_hash].receiver;
    }


    function getAmont(string _hash)
    external
    view
    hashExists(_hash)
    returns (int amount)
    {
        amount = consensusInfos[_hash].amount;
    }

    function getExchange(string _hash)
    external
    view
    hashExists(_hash)
    returns (int amount)
    {
        amount = consensusInfos[_hash].exchange;
    }

    function getTimelock(string _hash)
    external
    view
    hashExists(_hash)
    returns (int timelock)
    {
        if(consensusInfos[_hash].isInitiator)  {
            timelock = consensusInfos[_hash].timelock0;
        }
        else {
            timelock = consensusInfos[_hash].timelock1;
        }
    }

    function getSelfLocked(string _hash)
    external
    view
    hashExists(_hash)
    returns (int status)
    {
        if(consensusInfos[_hash].isInitiator)  {
            if(consensusInfos[_hash].locked0)
            {
               return 1;
            }
            return 0;


        } else {
            if(consensusInfos[_hash].locked1)
            {
                return 1;
            }
            return 0;
        }
    }

    function getSelfUnlocked(string _hash)
    external
    view
    hashExists(_hash)
    returns (int status)
    {
        if(consensusInfos[_hash].isInitiator)  {
            if(consensusInfos[_hash].unlocked0)
            {
               return 1;
            }
            return 0;


        } else {
            if(consensusInfos[_hash].unlocked1)
            {
                return 1;
            }
            return 0;
        }
    }

    function getOtherUnlocked(string _hash)
    external
    view
    hashExists(_hash)
    returns (int status)
    {
        if(consensusInfos[_hash].isInitiator)  {
            if(consensusInfos[_hash].unlocked1)
            {
               return 1;
            }
            return 0;


        } else {
            if(consensusInfos[_hash].unlocked0)
            {
                return 1;
            }
            return 0;
        }
    }

    // call by htlc
    function setSelfLocked(string _hash)
    external
    hashExists(_hash)
    {
        if(consensusInfos[_hash].isInitiator) {
            consensusInfos[_hash].locked0 = true;
        }
        else {
            consensusInfos[_hash].locked1 = true;
        }

    }

    // call by htlc
    function setSelfUnlocked(string _hash)
    external
    hashExists(_hash)
    {
        if(consensusInfos[_hash].isInitiator) {
            consensusInfos[_hash].unlocked0 = true;
        }
        else {
            consensusInfos[_hash].unlocked1 = true;
        }
    }

    // call by htlc and router
    function setOtherLocked(string _hash, int _status)
    external
    hashExists(_hash)
    {
        bool status = false;
        if(_status == 1) {
            status = true;
        }

        if(consensusInfos[_hash].isInitiator) {
            consensusInfos[_hash].locked1 = status;
        }
        else {
            consensusInfos[_hash].locked0 = status;
        }
    }

    // call by router
    function setOtherUnlocked(string _hash, int _status)
    external
    hashExists(_hash)
    {
        bool status = false;
        if(_status == 1) {
            status = true;
        }

        if(consensusInfos[_hash].isInitiator) {
            consensusInfos[_hash].unlocked1 = status;
        }
        else {
            consensusInfos[_hash].unlocked0 = status;
        }
    }

    function getTaskIndex()
    external
    view
    returns (int, int)
    {
        return (head, tail);
    }

    function getTask()
    external
    view
    returns (string)
    {
        if(head == tail) {
            return ("no task");
        }
        else {
            return (taskQueue[uint(head)]);
        }
    }

    function addTask(string _task)
    external
    returns (int)
    {
        tail = tail + 1;
        taskQueue.push(_task);
        return tail;
    }

    function deleteTask(string _task)
    external
    deleteQueueable(_task)
    returns (int)
    {
        head = head + 1;
        return head;
    }

    function haveHash(string _hash)
    internal
    view
    returns (bool)
    {
        return (consensusInfos[_hash].sender != address(0));

    }

    function sameString(string a, string b)
    internal
    pure
    returns (bool)
    {
        return keccak256(abi.encodePacked(a)) == keccak256(abi.encodePacked(b));
    }

    function stringToBytes32(string _source)
    public
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

    function byte32ToString(bytes32 _source)
    public
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
    public
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

    function addressToString(address _address)
    public
    pure
    returns (string)
    {
       bytes memory b = new bytes(20);
       for (uint i = 0; i < 20; i++)
           b[i] = byte(uint8(uint(_address) / (2**(8*(19 - i)))));
       return string(b);
    }
}
