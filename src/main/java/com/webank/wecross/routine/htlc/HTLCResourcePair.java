package com.webank.wecross.routine.htlc;

public class HTLCResourcePair {
    HTLC htlc;
    HTLCResource selfHTLCResource;
    HTLCResource counterpartyHTLCResource;

    public HTLCResourcePair(
            HTLC htlc, HTLCResource selfHTLCResource, HTLCResource counterpartyHTLCResource) {
        this.htlc = htlc;
        this.selfHTLCResource = selfHTLCResource;
        this.counterpartyHTLCResource = counterpartyHTLCResource;
    }

    public HTLC getHtlc() {
        return htlc;
    }

    public void setHtlc(HTLC htlc) {
        this.htlc = htlc;
    }

    public HTLCResource getSelfHTLCResource() {
        return selfHTLCResource;
    }

    public void setSelfHTLCResource(HTLCResource selfHTLCResource) {
        this.selfHTLCResource = selfHTLCResource;
    }

    public HTLCResource getCounterpartyHTLCResource() {
        return counterpartyHTLCResource;
    }

    public void setCounterpartyHTLCResource(HTLCResource counterpartyHTLCResource) {
        this.counterpartyHTLCResource = counterpartyHTLCResource;
    }

    @Override
    public String toString() {
        return "HTLCResourcePair{"
                + "selfHTLCResource="
                + selfHTLCResource.toString()
                + ", counterpartyHTLCResource="
                + counterpartyHTLCResource.toString()
                + '}';
    }
}
