package com.webank.wecross.proposal;

public abstract class Proposal {
    private int seq;

    public Proposal(int seq) {
        this.seq = seq;
    }

    // Interface
    public abstract void sendSignedPayload(String signature) throws Exception;

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

    public void forceDeadline(Long deadline) {
        this.deadline = deadline;
    }
}
