package com.webank.wecross.routine.htlc;

import com.webank.wecross.resource.Resource;

public class HTLCResourcePair {
    HTLC htlc;
    Resource selfHTLCResource;
    Resource counterpartyHTLCResource;

    public HTLCResourcePair(
            HTLC htlc, Resource selfHTLCResource, Resource counterpartyHTLCResource) {
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

    public Resource getSelfHTLCResource() {
        return selfHTLCResource;
    }

    public void setSelfHTLCResource(Resource selfHTLCResource) {
        this.selfHTLCResource = selfHTLCResource;
    }

    public Resource getCounterpartyHTLCResource() {
        return counterpartyHTLCResource;
    }

    public void setCounterpartyHTLCResource(Resource counterpartyHTLCResource) {
        this.counterpartyHTLCResource = counterpartyHTLCResource;
    }
}
