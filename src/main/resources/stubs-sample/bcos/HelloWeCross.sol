pragma experimental ABIEncoderV2;

contract HelloWeCross{
    int number;
    string message;
    int[] numbers=[1,2,3,4,5];
    string[] messages=["Talk is cheap","Show me the code"];

    function HelloWeCross(){
        (number, message) = (2019, "Hello WeCross");
    }

    function getNumber()constant returns(int){
        return number;
    }

    function setNumber(int num) returns(int){
        number = num;
        return number;
    }

    function getNumbers()constant returns(int[]){
        return numbers;
    }

    function setNumbers(int[] nums) returns(int[]){
        uint len = nums.length;
        numbers = new int[](len);
        for(uint i = 0; i < len; i++) {
            numbers[i] = nums[i];
        }
        return numbers;
    }

    function getMessage()constant returns(string){
        return message;
    }

    function setMessage(string msg) returns(string){
        message = msg;
        return message;
    }

    function getMessages()constant returns(string[]){
        return messages;
    }

    function setMessages(string[] msgs) returns(string[]){
        uint len = msgs.length;
        messages = new string[](len);
        for(uint i = 0; i < len; i++) {
            messages[i] = msgs[i];
        }
        return messages;
    }

    function getNumAndMsg()constant returns(int, string){
        return (number, message);
    }

    function setNumAndMsg(int num, string msg) returns(int,string){
        (number, message) = (num, msg);
        return (number, message);
    }

    function getAll()constant returns(int,int[],string,string[]){
        return (number,numbers,message,messages);
    }

    function setAll(int num, int[] nums, string msg, string[] msgs)constant returns(int,int[],string,string[]){
        (number, message) = (num, msg);
        setNumbers(nums);
        setMessages(msgs);
        return (number,numbers,message,messages);
    }

    function getInputs(int num, int[] nums, string msg, string[] msgs)constant returns(int,int[],string,string[]){
        return (num, nums, msg, msgs);
    }
}
