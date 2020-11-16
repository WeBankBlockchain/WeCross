package com.webank.wecross.routine.xa;

public class XATransactionResponse {
    private XAResponse xaResponse = new XAResponse();
    private XATransaction xaTransaction;

    public XAResponse getXaResponse() {
        return xaResponse;
    }

    public void setXaResponse(XAResponse xaResponse) {
        this.xaResponse = xaResponse;
    }

    public XATransaction getXaTransaction() {
        return xaTransaction;
    }

    public void setXaTransaction(XATransaction xaTransaction) {
        this.xaTransaction = xaTransaction;
    }

    @Override
    public String toString() {
        return "XATransactionResponse{"
                + "xaResponse="
                + xaResponse
                + ", xaTransaction="
                + xaTransaction
                + '}';
    }
}
