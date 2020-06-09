package com.webank.wecross.routine.xa;

import com.webank.wecross.stub.Path;

public class XATransactionInfo {
    private String transactionID;
    private int status;
    private int startTimestamp;
    private int commitTimestamp;
    private int rollbackTimestamp;
    private Path[] paths;
    private XATransactionStep[] steps;

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(int startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public int getCommitTimestamp() {
        return commitTimestamp;
    }

    public void setCommitTimestamp(int commitTimestamp) {
        this.commitTimestamp = commitTimestamp;
    }

    public int getRollbackTimestamp() {
        return rollbackTimestamp;
    }

    public void setRollbackTimestamp(int rollbackTimestamp) {
        this.rollbackTimestamp = rollbackTimestamp;
    }

    public Path[] getPaths() {
        return paths;
    }

    public void setPaths(Path[] paths) {
        this.paths = paths;
    }

    public XATransactionStep[] getSteps() {
        return steps;
    }

    public void setSteps(XATransactionStep[] steps) {
        this.steps = steps;
    }
}
