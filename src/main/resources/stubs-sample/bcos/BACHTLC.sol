pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./BAC001.sol";
import "./HTLC.sol";

contract BACHTLC is HTLC {

    // bac001 asset contract address
    address assetContract;

    function init(string[] _ss)
    public
    {
        assetContract = stringToAddress(_ss[0]);
    }

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
            result[0] = "success";
            return;
        }

        uint timelock = getTimelock(_hash);
        if(getRollbackStatus(_hash) || timelock <= (now / 1000))
        {
            result[0] = "has rolled back";
            return;
        }

        address sender = getSender(_hash);
        uint amount = getAmount(_hash);
        if (BAC001(assetContract).allowance(sender, address(this)) < uint(amount))
        {
            result[0] = "insufficient authorized assets";
            return;
        }

        setLockStatus(_hash);

        // This htlc contract becomes the temporary owner of the assets
        BAC001(assetContract).sendFrom(sender, address(this), uint(amount),"");

        result[0] = "success";
    }


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
           result[0] = "success";
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

        setUnlockStatus(_hash);
        setSecret(_hash,_secret);

        // transfer from htlc contract to receiver
        address receiver = getReceiver(_hash);
        uint amount = getAmount(_hash);
        BAC001(assetContract).send(receiver, uint(amount),"");

        result[0] = "success";
    }


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
           result[0] = "success";
           return;
        }

        uint timelock = getTimelock(_hash);
        if(timelock > (now / 1000))
        {
            result[0] = "can not rollback until now > timelock";
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

        setRollbackStatus(_hash);

        // transfer from htlc contract to sender
        address sender = getSender(_hash);
        uint amount = getAmount(_hash);
        BAC001(assetContract).send(sender, uint(amount),"");

        result[0] = "success";
    }

}
