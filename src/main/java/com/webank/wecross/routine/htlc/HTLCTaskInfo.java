package com.webank.wecross.routine.htlc;

public class HTLCTaskInfo {
    String selfPath;
    String selfAccount;
    String counterpartyPath;
    String counterpartyAccount;

    public String getSelfPath() {
        return selfPath;
    }

    public void setSelfPath(String selfPath) {
        this.selfPath = selfPath;
    }

    public String getSelfAccount() {
        return selfAccount;
    }

    public void setSelfAccount(String selfAccount) {
        this.selfAccount = selfAccount;
    }

    public String getCounterpartyPath() {
        return counterpartyPath;
    }

    public void setCounterpartyPath(String counterpartyPath) {
        this.counterpartyPath = counterpartyPath;
    }

    public String getCounterpartyAccount() {
        return counterpartyAccount;
    }

    public void setCounterpartyAccount(String counterpartyAccount) {
        this.counterpartyAccount = counterpartyAccount;
    }
}
