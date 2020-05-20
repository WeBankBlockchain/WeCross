package com.webank.wecross.network.rpc.netty;

import org.springframework.core.io.Resource;

public class RPCConfig {

    private String listenIP;
    private int listenPort;

    private Resource caCert;
    private Resource sslCert;
    private Resource sslKey;

    public Resource getCaCert() {
        return caCert;
    }

    public void setCaCert(Resource caCert) {
        this.caCert = caCert;
    }

    public Resource getSslCert() {
        return sslCert;
    }

    public void setSslCert(Resource sslCert) {
        this.sslCert = sslCert;
    }

    public Resource getSslKey() {
        return sslKey;
    }

    public void setSslKey(Resource sslKey) {
        this.sslKey = sslKey;
    }

    public String getListenIP() {
        return listenIP;
    }

    public void setListenIP(String listenIP) {
        this.listenIP = listenIP;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    @Override
    public String toString() {
        return "RPCConfig{"
                + "listenIP='"
                + listenIP
                + '\''
                + ", listenPort="
                + listenPort
                + ", caCert="
                + caCert
                + ", sslCert="
                + sslCert
                + ", sslKey="
                + sslKey
                + '}';
    }
}
