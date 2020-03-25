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
        string[] memory ss = new string[](1);
        ss[0] = _ss[1];
        super.init(ss);
    }

    function lock(string[] _ss)
    public
    returns (string[] result)
    {
        string memory _hash = _ss[0];
        result = new string[](1);
        if(getLockStatus(_hash)) {
            result[0] = "success";
            return;
        }

        result = super.lock(_ss);
        if(!equal(result[0], "success")) {
            return;
        }

        address sender = getSender(_hash);
        uint amount = getAmount(_hash);
        if (BAC001(assetContract).allowance(sender, address(this)) < uint(amount))
        {
            result[0] = "insufficient authorized assets";
            return;
        }

        // This htlc contract becomes the temporary owner of the assets
        BAC001(assetContract).sendFrom(sender, address(this), uint(amount),"");

        setLockStatus(_hash);
        result[0] = "success";
    }


    function unlock(string[] _ss)
    public
    returns (string[] result)
    {
        string memory _hash = _ss[0];
        result = new string[](1);
        if (getUnlockStatus(_hash))
        {
           result[0] = "success";
           return;
        }

        result = super.unlock(_ss);
        if(!equal(result[0], "success")) {
            return;
        }

        // transfer from htlc contract to receiver
        address receiver = getReceiver(_hash);
        uint amount = getAmount(_hash);
        BAC001(assetContract).send(receiver, uint(amount),"");

        setUnlockStatus(_hash);
        setSecret(_ss);
        result[0] = "success";
    }

    function rollback(string[] _ss)
    public
    returns (string[] result)
    {
        string memory _hash = _ss[0];
        result = new string[](1);
        if (getRollbackStatus(_hash))
        {
           result[0] = "success";
           return;
        }

        result = super.rollback(_ss);
        if(!equal(result[0], "success")) {
            return;
        }

        // transfer from htlc contract to sender
        address sender = getSender(_hash);
        uint amount = getAmount(_hash);
        BAC001(assetContract).send(sender, uint(amount),"");

        setRollbackStatus(_hash);
        result[0] = "success";
    }

}
