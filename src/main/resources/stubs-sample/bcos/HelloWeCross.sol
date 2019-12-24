pragma experimental ABIEncoderV2;

contract HelloWeCross{
    int number;
    string message;
    int[] numbers=[1,2,3,4,5];
    string[] messages=["Talk is cheap","Show me the code"];

    function HelloWeCross(){
        number = 2019;
        message="Hello WeCross";
    }

    function getNumber()constant returns(int){
        return number;
    }

    function getMessage()constant returns(string){
        return message;
    }

    function getNumbers()constant returns(int[]){
        return numbers;
    }

    function getMessages()constant returns(string[]){
        return messages;
    }

    function getAll()constant returns(int,int[],string, string[]){
        return (number,numbers,message,messages);
    }


    function setNumber(int num) returns(int){
        number = num;
        return number;
    }

    function setMessage(string msg) returns(string){
        message = msg;
        return message;
    }

    function setNumAndMsg(int num, string msg) returns(int, string){
        number = num;
        message = msg;
        return (number, message);
    }

    function getNumAndMsg() returns(int, string){
        return (number, message);
    }
}
