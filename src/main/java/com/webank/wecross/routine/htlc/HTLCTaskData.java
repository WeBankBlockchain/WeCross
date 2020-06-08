package com.webank.wecross.routine.htlc;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Path;

public class HTLCTaskData {
    Path selfPath;
    Account account1;
    Path counterpartyPath;
    Account account2;
    String counterpartyAddress;

    public Path getSelfPath() {
        return selfPath;
    }

    public void setSelfPath(Path selfPath) {
        this.selfPath = selfPath;
    }

    public Account getAccount1() {
        return account1;
    }

    public void setAccount1(Account account1) {
        this.account1 = account1;
    }

    public Path getCounterpartyPath() {
        return counterpartyPath;
    }

    public void setCounterpartyPath(Path counterpartyPath) {
        this.counterpartyPath = counterpartyPath;
    }

    public Account getAccount2() {
        return account2;
    }

    public void setAccount2(Account account2) {
        this.account2 = account2;
    }

    public String getCounterpartyAddress() {
        return counterpartyAddress;
    }

    public void setCounterpartyAddress(String counterpartyAddress) {
        this.counterpartyAddress = counterpartyAddress;
    }

    @Override
    public String toString() {
        return "HTLCTaskData{"
                + "selfPath="
                + selfPath
                + ", account1="
                + account1
                + ", counterpartyPath="
                + counterpartyPath
                + ", account2="
                + account2
                + ", counterpartyAddress='"
                + counterpartyAddress
                + '\''
                + '}';
    }
}
