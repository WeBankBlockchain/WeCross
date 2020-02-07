package com.webank.wecross.proposal;

import com.webank.wecross.restserver.request.TransactionRequest;

public abstract class Proposal {
    private int seq;

    public Proposal(int seq) {
        this.seq = seq;
    }

    // Interface
    public abstract byte[] getBytesToSign();

    public abstract void sendSignedPayload(byte[] signBytes) throws Exception;

    public abstract void loadBytes(byte[] proposalBytes) throws Exception;

    public abstract Boolean isEqualsRequest(TransactionRequest request) throws Exception;

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    // Timeout logic
    private static final Long expiresTime = new Long(1000 * 60); // 1min
    private Long deadline = System.currentTimeMillis() + expiresTime;

    public Boolean isTimeout() {
        return System.currentTimeMillis() >= deadline;
    }

    public void refreshDeadline() {
        deadline = System.currentTimeMillis() + expiresTime;
    }

    public void forceDeadline(Long deadline) {
        this.deadline = deadline;
    }
}
