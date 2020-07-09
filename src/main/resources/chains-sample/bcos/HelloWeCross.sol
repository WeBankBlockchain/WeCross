pragma solidity ^0.5.1;
pragma experimental ABIEncoderV2;

contract HelloWeCross {
    string[] ss = ["Talk is cheap", "Show me the code"];

    function set(string memory _s) public returns (string[] memory) {
        ss = [_s];
        return ss;
    }

    function setArray(string[] memory _ss) public returns (string[] memory) {
        ss = _ss;
        return ss;
    }

    function getAndClear() public returns(string[] memory) {
        string[] memory _ss = ss;
        ss.length = 0;
        return _ss;
    }

    function get() public view returns(string[] memory) {
        return ss;
    }
}