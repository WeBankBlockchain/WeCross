package com.webank.wecross.stub.bcos;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;

public class Web3Sdk {

    private Service bcosService;
    private Web3j web3;
    private Credentials credentials;

    public Service getBcosService() {
        return bcosService;
    }

    public void setBcosService(Service bcosService) {
        this.bcosService = bcosService;
    }

    public Web3j getWeb3() {
        return web3;
    }

    public void setWeb3(Web3j web3) {
        this.web3 = web3;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
}
