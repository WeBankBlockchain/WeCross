package com.webank.wecross.routine.htlc;

public class HTLCTaskInfo {
    String selfPath;
    String selfAccount;
    String selfAddress;
    String counterpartyPath;
    String counterpartyAccount;
    String counterpartyAddress;

    public HTLCTaskInfo() {}

    public HTLCTaskInfo(
            String selfPath,
            String selfAccount,
            String selfAddress,
            String counterpartyPath,
            String counterpartyAccount,
            String counterpartyAddress) {
        this.selfPath = selfPath;
        this.selfAccount = selfAccount;
        this.selfAddress = selfAddress;
        this.counterpartyPath = counterpartyPath;
        this.counterpartyAccount = counterpartyAccount;
        this.counterpartyAddress = counterpartyAddress;
    }

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

    public String getSelfAddress() {
        return selfAddress;
    }

    public void setSelfAddress(String selfAddress) {
        this.selfAddress = selfAddress;
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

    public String getCounterpartyAddress() {
        return counterpartyAddress;
    }

    public void setCounterpartyAddress(String counterpartyAddress) {
        this.counterpartyAddress = counterpartyAddress;
    }
}
