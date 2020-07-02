pragma solidity >=0.4.22 <0.6.0;
pragma experimental ABIEncoderV2;

import "./LedgerSample.sol";
import "./HTLC.sol";

contract LedgerSampleHTLC is HTLC, LedgerSampleHolder {

    // asset contract address
    address assetContract;

    /*
        @param: assetContract | counterpartyHtlcAddress
    */
    function init(string[] memory _ss) public
    returns (string[] memory result)
    {
        result = new string[](1);
        string[] memory ss = new string[](1);
        ss[0] = _ss[1];
        result = super.init(ss);
        assetContract = stringToAddress(_ss[0]);
    }

    /*
        @param: hash
    */
    function lock(string[] memory _ss) public
    returns (string[] memory result)
    {
        result = new string[](1);
        result = super.lock(_ss);
        if(sameString(result[0], "done")) {
            result[0] = successFlag;
            return result;
        } else if (!sameString(result[0], "continue")) {
            return result;
        }

        string memory _hash = _ss[0];
        address sender = getSender(_hash);
        uint amount = getAmount(_hash);
        if (LedgerSample(assetContract).allowance(sender, address(this)) < uint(amount)) {
            result[0] = "insufficient authorized assets";
            return result;
        }

        // This htlc contract becomes the temporary owner of the assets
        LedgerSample(assetContract).sendFrom(sender, address(this), uint(amount),"");

        setLockState(_hash);
        result[0] = successFlag;
    }

    /*
        @param: hash | secret
    */
    function unlock(string[] memory _ss) public
    returns (string[] memory result)
    {
        result = new string[](1);
        result = super.unlock(_ss);
        if(sameString(result[0], "done")) {
            result[0] = successFlag;
            return result;
        } else if (!sameString(result[0], "continue")) {
            return result;
        }

        string memory _hash = _ss[0];
        // transfer from htlc contract to receiver
        address receiver = getReceiver(_hash);
        uint amount = getAmount(_hash);
        LedgerSample(assetContract).send(receiver, uint(amount),"");

        setUnlockState(_hash);
        setSecret(_ss);
        result[0] = successFlag;
    }

    /*
        @param: hash
    */
    function rollback(string[] memory _ss) public
    returns (string[] memory result)
    {
        result = new string[](1);
        result = super.rollback(_ss);
        if(sameString(result[0], "done")) {
            result[0] = successFlag;
            return result;
        } else if (!sameString(result[0], "continue")) {
            return result;
        }

        string memory _hash = _ss[0];
        // transfer from htlc contract to sender
        address sender = getSender(_hash);
        uint amount = getAmount(_hash);
        LedgerSample(assetContract).send(sender, uint(amount),"");

        setRollbackState(_hash);
        result[0] = successFlag;
    }

    /*
        @param: address
    */
    function balanceOf(string memory account) public view
    returns(string[] memory result)
    {
        result = new string[](1);
        uint b = LedgerSample(assetContract).balance(stringToAddress(account));
        result[0] = uint256ToString(b);
    }
}
