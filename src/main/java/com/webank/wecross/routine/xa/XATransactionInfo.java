package com.webank.wecross.routine.xa;

import com.webank.wecross.stub.Path;
import java.util.List;

public class XATransactionInfo {
    private String transactionID;
    private int status;
    private String startTimestamp;
    private String commitTimestamp;
    private String rollbackTimestamp;
    private List<String> allPaths;
    private List<Path> paths;
    private List<XATransactionStep> transactionSteps;

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

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(String startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getCommitTimestamp() {
        return commitTimestamp;
    }

    public void setCommitTimestamp(String commitTimestamp) {
        this.commitTimestamp = commitTimestamp;
    }

    public String getRollbackTimestamp() {
        return rollbackTimestamp;
    }

    public void setRollbackTimestamp(String rollbackTimestamp) {
        this.rollbackTimestamp = rollbackTimestamp;
    }

    public List<String> getAllPaths() {
        return allPaths;
    }

    public void setAllPaths(List<String> allPaths) {
        this.allPaths = allPaths;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public void setPaths(List<Path> paths) {
        this.paths = paths;
    }

    public List<XATransactionStep> getTransactionSteps() {
        return transactionSteps;
    }

    public void setTransactionSteps(List<XATransactionStep> transactionSteps) {
        this.transactionSteps = transactionSteps;
    }

    @Override
    public String toString() {
        return "XATransactionInfo{"
                + "transactionID='"
                + transactionID
                + '\''
                + ", status="
                + status
                + ", startTimestamp="
                + startTimestamp
                + ", commitTimestamp="
                + commitTimestamp
                + ", rollbackTimestamp="
                + rollbackTimestamp
                + ", paths="
                + paths
                + ", transactionSteps="
                + transactionSteps
                + '}';
    }
}
