package com.webank.wecross.account.uaproof;

public class UAProofInfo {

    public UAProofInfo() {}

    private String chainAccountID; // caID
    private String uaID;
    private String type;
    private String uaProof;

    public String getChainAccountID() {
        return chainAccountID;
    }

    public void setChainAccountID(String chainAccountID) {
        this.chainAccountID = chainAccountID;
    }

    public String getUaID() {
        return uaID;
    }

    public void setUaID(String uaID) {
        this.uaID = uaID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUaProof() {
        return uaProof;
    }

    public void setUaProof(String uaProof) {
        this.uaProof = uaProof;
    }

    public static UAProofInfoBuilder builder() {
        return new UAProofInfoBuilder();
    }

    @Override
    public String toString() {
        return "UAProofInfo{"
                + "caID='"
                + chainAccountID
                + '\''
                + ", uaID='"
                + uaID
                + '\''
                + ", type='"
                + type
                + '\''
                + ", uaProof='"
                + uaProof
                + '\''
                + '}';
    }

    public static final class UAProofInfoBuilder {
        private String chainAccountID;
        private String uaID;
        private String type;
        private String uaProof;

        private UAProofInfoBuilder() {}

        public static UAProofInfoBuilder anUAProofInfo() {
            return new UAProofInfoBuilder();
        }

        public UAProofInfoBuilder chainAccountID(String chainAccountID) {
            this.chainAccountID = chainAccountID;
            return this;
        }

        public UAProofInfoBuilder uaID(String uaID) {
            this.uaID = uaID;
            return this;
        }

        public UAProofInfoBuilder type(String type) {
            this.type = type;
            return this;
        }

        public UAProofInfoBuilder uaProof(String uaProof) {
            this.uaProof = uaProof;
            return this;
        }

        public UAProofInfo build() {
            UAProofInfo uAProofInfo = new UAProofInfo();
            uAProofInfo.setChainAccountID(chainAccountID);
            uAProofInfo.setUaID(uaID);
            uAProofInfo.setType(type);
            uAProofInfo.setUaProof(uaProof);
            return uAProofInfo;
        }
    }
}
