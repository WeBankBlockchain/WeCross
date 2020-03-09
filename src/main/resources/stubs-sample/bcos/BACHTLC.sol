pragma solidity ^0.4.25;

import "./BAC001.sol";
import "./HTLC.sol";

contract BACHTLC is HTLC {

    // bac001 asset contract address
    address assetContract;

    function initBACHTLC(
        string _counterpartyHTLCIpath,
        string _counterpartyHTLCAddress,
        string _assetContract
    )
    external
    {
        setCounterpartyHTLCInfo(_counterpartyHTLCIpath, _counterpartyHTLCAddress);
        assetContract = stringToAddress(_assetContract);
    }

    function lock(string _hash)
    external
    returns (string)
    {
        if (!hasContract(_hash))
        {
           return "contract not exists";
        }

        if(getLockStatus(_hash)) {
            return "success";
        }

        uint timelock = getTimelock(_hash);
        if(getRollbackStatus(_hash) || timelock <= now)
        {
            return "has rolled back";
        }

        address sender = getSender(_hash);
        uint amount = getAmount(_hash);
        if (BAC001(assetContract).allowance(sender, address(this)) < uint(amount))
        {
            return "insufficient authorized assets";
        }

        setLockStatus(_hash);

        // This htlc contract becomes the temporary owner of the assets
        BAC001(assetContract).sendFrom(sender, address(this), uint(amount),"");

        return "success";
    }


    function unlock(string _hash, string _secret)
    external
    returns (string)
    {
        if (!hasContract(_hash))
        {
           return "contract not exists";
        }

        if (getUnlockStatus(_hash))
        {
           return "success";
        }

        if(!hashMatched(_hash, _secret))
        {
           return "hash not matched";
        }

        if (!getLockStatus(_hash))
        {
           return "can not unlock until lock is done";
        }

        uint timelock = getTimelock(_hash);
        if(getRollbackStatus(_hash) || timelock <= now)
        {
            return "has rolled back";
        }

        setUnlockStatus(_hash);
        setSecret(_hash,_secret);

        // transfer from htlc contract to receiver
        address receiver = getReceiver(_hash);
        uint amount = getAmount(_hash);
        BAC001(assetContract).send(receiver, uint(amount),"");

        return "success";
    }


    function rollback(string _hash)
    external
    returns (string)
    {
        if (!hasContract(_hash))
        {
           return "contract not exists";
        }

        if (getRollbackStatus(_hash))
        {
           return "success";
        }

        uint timelock = getTimelock(_hash);
        if(timelock > now)
        {
            return "can not rollback until now > timelock";
        }

        if (!getLockStatus(_hash))
        {
           return "not need rollback unless lock is done";
        }

        if (getUnlockStatus(_hash))
        {
           return "can not rollback if unlock is done";
        }

        setRollbackStatus(_hash);

        // transfer from htlc contract to sender
        address sender = getSender(_hash);
        uint amount = getAmount(_hash);
        BAC001(assetContract).send(sender, uint(amount),"");

        return "success";
    }

}
