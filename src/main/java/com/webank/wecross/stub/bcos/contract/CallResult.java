package com.webank.wecross.stub.bcos.contract;

import java.util.Objects;

public class CallResult {
    private String status;
    private String message;
    private String output;

    public CallResult() {}

    public CallResult(String status, String message, String output) {
        this.status = status;
        this.message = message;
        this.output = output;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof org.fisco.bcos.channel.client.CallResult)) {
            return false;
        }
        org.fisco.bcos.channel.client.CallResult that =
                (org.fisco.bcos.channel.client.CallResult) o;
        return Objects.equals(getStatus(), that.getStatus())
                && Objects.equals(getMessage(), that.getMessage())
                && Objects.equals(getOutput(), that.getOutput());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStatus(), getMessage(), getOutput());
    }

    @Override
    public String toString() {
        return "CallResult{"
                + "status='"
                + status
                + '\''
                + ", message='"
                + message
                + '\''
                + ", output='"
                + output
                + '\''
                + '}';
    }
}
