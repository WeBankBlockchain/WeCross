pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./BAC001.sol";
import "./HTLC.sol";

contract BACHTLC is HTLC {

    // bac001 asset contract address
    address assetContract;

    function initHTLCContract(
        string _counterpartyHTLCPath,
        string _counterpartyHTLCAddress,
        string _assetContract
    )
    external
    {
        setCounterpartyHTLCInfo(_counterpartyHTLCPath, _counterpartyHTLCAddress);
        assetContract = stringToAddress(_assetContract);
    }

    function lock(string _hash)
    external
    returns (string[] result)
    {
        result = new string[](1);
        if (!taskIsExisted(_hash))
        {
           result[0] = "task not exists";
        }

        if(getLockStatus(_hash)) {
            result[0] = "success";
        }

        uint timelock = getTimelock(_hash);
        if(getRollbackStatus(_hash) || timelock <= now)
        {
            result[0] = "has rolled back";
        }

        address sender = getSender(_hash);
        uint amount = getAmount(_hash);
        if (BAC001(assetContract).allowance(sender, address(this)) < uint(amount))
        {
            result[0] = "insufficient authorized assets";
        }

        setLockStatus(_hash);

        // This htlc contract becomes the temporary owner of the assets
        BAC001(assetContract).sendFrom(sender, address(this), uint(amount),"");

        result[0] = "success";
    }


    function unlock(string _hash, string _secret)
    external
    returns (string[] result)
    {
        result = new string[](1);
        if (!taskIsExisted(_hash))
        {
           result[0] = "task not exists";
        }

        if (getUnlockStatus(_hash))
        {
           result[0] = "success";
        }

        if(!hashMatched(_hash, _secret))
        {
           result[0] = "hash not matched";
        }

        if (!getLockStatus(_hash))
        {
           result[0] = "can not unlock until lock is done";
        }

        uint timelock = getTimelock(_hash);
        if(getRollbackStatus(_hash) || timelock <= now)
        {
            result[0] = "has rolled back";
        }

        setUnlockStatus(_hash);
        setSecret(_hash,_secret);

        // transfer from htlc contract to receiver
        address receiver = getReceiver(_hash);
        uint amount = getAmount(_hash);
        BAC001(assetContract).send(receiver, uint(amount),"");

        result[0] = "success";
    }


    function rollback(string _hash)
    external
    returns (string[] result)
    {
        result = new string[](1);
        if (!taskIsExisted(_hash))
        {
           result[0] = "task not exists";
        }

        if (getRollbackStatus(_hash))
        {
           result[0] = "success";
        }

        uint timelock = getTimelock(_hash);
        if(timelock > now)
        {
            result[0] = "can not rollback until now > timelock";
        }

        if (!getLockStatus(_hash))
        {
           result[0] = "no need to rollback unless lock is done";
        }

        if (getUnlockStatus(_hash))
        {
           result[0] = "can not rollback if unlock is done";
        }

        setRollbackStatus(_hash);

        // transfer from htlc contract to sender
        address sender = getSender(_hash);
        uint amount = getAmount(_hash);
        BAC001(assetContract).send(sender, uint(amount),"");

        result[0] = "success";
    }

}
