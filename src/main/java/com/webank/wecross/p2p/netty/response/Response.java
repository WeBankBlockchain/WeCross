package com.webank.wecross.p2p.netty.response;

public class Response {
    private int errorCode;
    private String errorMessage;
    private String content;
    private String messageID;

    public static Response build(int errorCode, String errorMsg, String messageID, String content) {

        Response response = new Response();
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMsg);
        response.setMessageID(messageID);
        response.setContent(content);

        return response;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    @Override
    public String toString() {
        return "Response{"
                + "errorCode="
                + errorCode
                + ", errorMessage='"
                + errorMessage
                + '\''
                + ", content='"
                + content
                + '\''
                + ", messageID='"
                + messageID
                + '\''
                + '}';
    }
}
