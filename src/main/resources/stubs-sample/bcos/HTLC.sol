pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./BAC001.sol";
import "./HTLCStorage.sol";

contract HTLC {

    struct LockContract {
        string secret;
        int timelock;
        bool locked;
        bool unlocked;
        bool timeouted;
    }

    address storageAddress;

    string storageIpath;

    // to lock and unlock other
    string otherHTLCIpath;

    // bac001 asset contract address
    address assetContract;

    HTLCStorage htlcStorage = new HTLCStorage();

    mapping (string => address[]) permissionLists;

    mapping (string => LockContract) contracts;

    modifier contractExists(string _hash) {
        require(haveContract(_hash), "contractExists: the hash should exist");
        _;
    }

    modifier hashMatched(string _hash, string _secret) {
        require(sha256(abi.encodePacked(htlcStorage.stringToBytes32(_secret))) == htlcStorage.stringToBytes32(_hash), "the hash should not exist");
        _;
    }
    modifier unlockable(string _hash) {
        require(contracts[_hash].timeouted == false, "timeoutable: already timeouted");
        require(uint(contracts[_hash].timelock) > now, "unlockable: timelock time must be in the future");
        _;
    }
    modifier timeoutable(string _hash) {
        require(contracts[_hash].unlocked == false, "timeoutable: already unlocked");
        require(uint(contracts[_hash].timelock)  <= now, "timeoutable: timelock not yet passed");
        _;
    }
    modifier getSecretable(string _hash) {
        require(havaPermission(_hash, msg.sender), "getSecretable: have no permission");
        _;
    }

    function initHTLC(
        string _storageAddress,
        string _storageIpath,
        string _otherHTLCIpath,
        string _assetContract
    )
    external
    {
        storageAddress = htlcStorage.stringToAddress(_storageAddress);
        storageIpath = _storageIpath;
        otherHTLCIpath = _otherHTLCIpath;
        htlcStorage = HTLCStorage(storageAddress);
        assetContract = htlcStorage.stringToAddress(_assetContract);
    }

    function setSecret(string _hash, string _secret)
    hashMatched(_hash, _secret)
    external
    returns (string)
    {
        if (haveContract(_hash))
        {
           return contracts[_hash].secret;
        }
        contracts[_hash] = LockContract(
            _secret,
            0,
            false,
            false,
            false
        );
        return _secret;
    }

    function setPermissionList(string _hash, string[] _authorizees)
    public
    returns (uint)
    {
        if (permissionLists[_hash].length != 0)
            revert("Hash exists");

        uint len =  _authorizees.length;
        address[] memory authorizees = new address[](len + 1);
        for(uint i = 0; i < len; i++) {
            authorizees[i] = htlcStorage.stringToAddress(_authorizees[i]);
        }
        authorizees[len] = msg.sender;

        permissionLists[_hash] = authorizees;

        return permissionLists[_hash].length;
    }

    function getStorageIpath()
    external
    view
    returns (string)
    {
        return storageIpath;
    }

    function getOtherHTLCIpath()
    external
    view
    returns (string)
    {
        return otherHTLCIpath;
    }


    function getSecret(string _hash)
    getSecretable(_hash)
    external
    view
    returns (string secret)
    {
        secret = contracts[_hash].secret;
    }

    function lock(string _hash)
    contractExists(_hash)
    external
    returns (int)
    {
        // hava locked
        if (contracts[_hash].locked)
        {
           return htlcStorage.getAmont(_hash);
        }

        int amount = htlcStorage.getAmont(_hash);
        address sender = htlcStorage.getSender(_hash);
        int timelock = htlcStorage.getTimelock(_hash);

        if (BAC001(assetContract).allowance(sender, address(this)) < uint(amount))
        {
            revert("Insufficient authorized assets");
        }

        if(uint(timelock) <= now)
        {
            revert("Illegal timelock");
        }

        // This htlc contract becomes the temporary owner of the assets
        BAC001(assetContract).sendFrom(sender, address(this), uint(amount),"");

        contracts[_hash].timelock = timelock;
        contracts[_hash].locked = true;

        htlcStorage.setSelfLocked(_hash);

        return amount;
    }


    function unlock(string _hash, string _secret)
    external
    contractExists(_hash)
    hashMatched(_hash, _secret)
    unlockable(_hash)
    returns (int)
    {
        // hava unlocked
        if(contracts[_hash].unlocked) {
            return htlcStorage.getAmont(_hash);
        }

        address receiver = htlcStorage.getReceiver(_hash);
        int amount = htlcStorage.getAmont(_hash);

        contracts[_hash].secret = _secret;
        contracts[_hash].unlocked = true;


        htlcStorage.setSelfUnlocked(_hash);

        // transfer from htlc contract to receiver
        BAC001(assetContract).send(receiver, uint(amount),"");
        return amount;
    }


    function timeout(string _hash)
    external
    contractExists(_hash)
    timeoutable(_hash)
    returns (int)
    {
        // hava timeouted
        if(contracts[_hash].timeouted) {
            return htlcStorage.getAmont(_hash);
        }

        address sender = htlcStorage.getSender(_hash);
        int amount = htlcStorage.getAmont(_hash);

        contracts[_hash].timeouted = true;

        // transfer from htlc contract to sender
        BAC001(assetContract).send(sender, uint(amount),"");
        return amount;
    }

    function haveContract(string _hash)
    internal
    view
    returns (bool)
    {
        return (bytes(contracts[_hash].secret).length > 0);
    }

    function havaPermission(string _hash, address _caller)
    internal
    view
    returns (bool)
    {
        address[] storage list = permissionLists[_hash];
        uint len = list.length;
        for(uint i = 0; i < len; i++) {
            if(list[i] == _caller) {
                return true;
            }
        }

        return false;
    }
}
