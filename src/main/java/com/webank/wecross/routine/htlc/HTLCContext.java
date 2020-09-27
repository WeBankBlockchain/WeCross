package com.webank.wecross.routine.htlc;

import com.webank.wecross.account.UniversalAccount;
import com.webank.wecross.stub.Path;

public class HTLCContext {
    private Path selfPath;
    private Path counterpartyPath;
    private UniversalAccount adminUA;

    public HTLCContext() {}

    public HTLCContext(Path selfPath, Path counterpartyPath, UniversalAccount adminUA) {
        this.selfPath = selfPath;
        this.counterpartyPath = counterpartyPath;
        this.adminUA = adminUA;
    }

    public Path getSelfPath() {
        return selfPath;
    }

    public void setSelfPath(Path selfPath) {
        this.selfPath = selfPath;
    }

    public Path getCounterpartyPath() {
        return counterpartyPath;
    }

    public void setCounterpartyPath(Path counterpartyPath) {
        this.counterpartyPath = counterpartyPath;
    }

    public UniversalAccount getAdminUA() {
        return adminUA;
    }

    public void setAdminUA(UniversalAccount adminUA) {
        this.adminUA = adminUA;
    }

    @Override
    public String toString() {
        return "HTLCTaskData{"
                + "selfPath="
                + selfPath
                + ", counterpartyPath="
                + counterpartyPath
                + ", adminUA="
                + adminUA
                + '}';
    }
}
