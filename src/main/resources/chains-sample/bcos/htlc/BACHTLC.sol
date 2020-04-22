pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "./BAC001.sol";
import "./HTLC.sol";

contract BACHTLC is HTLC, BAC001Holder {

    // bac001 asset contract address
    address assetContract;

    function init(string[] _ss)
    public
    returns (string[] result)
    {
        result = new string[](1);
        string[] memory ss = new string[](1);
        ss[0] = _ss[1];
        result = super.init(ss);
        if(!equal(result[0], "success")) {
            return;
        }
        assetContract = stringToAddress(_ss[0]);
    }

    function lock(string[] _ss)
    public
    returns (string[] result)
    {
        result = new string[](1);
        result = super.lock(_ss);
        if(equal(result[0], "done")) {
            result[0] = "success";
            return;
        } else if (!equal(result[0], "continue")) {
            return;
        }

        string memory _hash = _ss[0];
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
        result = new string[](1);
        result = super.unlock(_ss);
        if(equal(result[0], "done")) {
            result[0] = "success";
            return;
        } else if (!equal(result[0], "continue")) {
            return;
        }

        string memory _hash = _ss[0];
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
        result = new string[](1);
        result = super.rollback(_ss);
        if(equal(result[0], "done")) {
            result[0] = "success";
            return;
        } else if (!equal(result[0], "continue")) {
            return;
        }

        string memory _hash = _ss[0];
        // transfer from htlc contract to sender
        address sender = getSender(_hash);
        uint amount = getAmount(_hash);
        BAC001(assetContract).send(sender, uint(amount),"");

        setRollbackStatus(_hash);
        result[0] = "success";
    }

    function balanceOf(string[] _ss)
    public
    view
    returns(string[] result)
    {
        result = new string[](1);
        uint b = BAC001(assetContract).balance(stringToAddress(_ss[0]));
        result[0] = uintToString(b);
    }
}
