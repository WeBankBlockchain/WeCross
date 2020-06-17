package com.webank.wecross.routine.htlc;

import java.math.BigInteger;

public class HTLCProposal {
    private boolean initiator;
    private String secret;

    private BigInteger selfTimelock;
    private boolean selfLocked;
    private boolean selfUnlocked;
    private boolean selfRolledback;

    private BigInteger counterpartyTimelock;
    private boolean counterpartyLocked;
    private boolean counterpartyUnlocked;
    private boolean counterpartyRolledback;

    public boolean isInitiator() {
        return initiator;
    }

    public void setInitiator(boolean initiator) {
        this.initiator = initiator;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public BigInteger getSelfTimelock() {
        return selfTimelock;
    }

    public void setSelfTimelock(BigInteger selfTimelock) {
        this.selfTimelock = selfTimelock;
    }

    public boolean isSelfLocked() {
        return selfLocked;
    }

    public void setSelfLocked(boolean selfLocked) {
        this.selfLocked = selfLocked;
    }

    public boolean isSelfUnlocked() {
        return selfUnlocked;
    }

    public void setSelfUnlocked(boolean selfUnlocked) {
        this.selfUnlocked = selfUnlocked;
    }

    public boolean isSelfRolledback() {
        return selfRolledback;
    }

    public void setSelfRolledback(boolean selfRolledback) {
        this.selfRolledback = selfRolledback;
    }

    public BigInteger getCounterpartyTimelock() {
        return counterpartyTimelock;
    }

    public void setCounterpartyTimelock(BigInteger counterpartyTimelock) {
        this.counterpartyTimelock = counterpartyTimelock;
    }

    public boolean isCounterpartyLocked() {
        return counterpartyLocked;
    }

    public void setCounterpartyLocked(boolean counterpartyLocked) {
        this.counterpartyLocked = counterpartyLocked;
    }

    public boolean isCounterpartyUnlocked() {
        return counterpartyUnlocked;
    }

    public void setCounterpartyUnlocked(boolean counterpartyUnlocked) {
        this.counterpartyUnlocked = counterpartyUnlocked;
    }

    public boolean isCounterpartyRolledback() {
        return counterpartyRolledback;
    }

    public void setCounterpartyRolledback(boolean counterpartyRolledback) {
        this.counterpartyRolledback = counterpartyRolledback;
    }

    @Override
    public String toString() {
        return "HTLCProposal{"
                + "initiator="
                + initiator
                + ", secret='"
                + secret
                + '\''
                + ", selfTimelock="
                + selfTimelock
                + ", selfLocked="
                + selfLocked
                + ", selfUnlocked="
                + selfUnlocked
                + ", selfRolledback="
                + selfRolledback
                + ", counterpartyTimelock="
                + counterpartyTimelock
                + ", counterpartyLocked="
                + counterpartyLocked
                + ", counterpartyUnlocked="
                + counterpartyUnlocked
                + ", counterpartyRolledback="
                + counterpartyRolledback
                + '}';
    }
}
