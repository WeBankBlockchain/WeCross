package com.webank.wecross.routine.xa;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class XAResponse {
    private int status = 0;
    private List<ChainErrorMessage> chainErrorMessages =
            Collections.synchronizedList(new LinkedList<>());

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<ChainErrorMessage> getChainErrorMessages() {
        return chainErrorMessages;
    }

    public void setChainErrorMessages(List<ChainErrorMessage> chainErrorMessages) {
        this.chainErrorMessages = chainErrorMessages;
    }

    public void addChainErrorMessage(ChainErrorMessage chainErrorMessage) {
        this.chainErrorMessages.add(chainErrorMessage);
    }

    public static class ChainErrorMessage {
        private String chain;
        private String message;

        public ChainErrorMessage(String chain, String message) {
            this.chain = chain;
            this.message = message;
        }

        public String getChain() {
            return chain;
        }

        public void setChain(String chain) {
            this.chain = chain;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "ChainErrorMessage{"
                    + "chain='"
                    + chain
                    + '\''
                    + ", message='"
                    + message
                    + '\''
                    + '}';
        }
    }

    @Override
    public String toString() {
        return "XAResponse{" + "status=" + status + ", chainResponses=" + chainErrorMessages + '}';
    }
}
