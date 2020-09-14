package com.webank.wecross.network.client;

public class ClientConnection {
    String server;
    int maxTotal;
    int maxPerRoute;
    String sslKey;
    String sslCert;
    String caCert;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxPerRoute() {
        return maxPerRoute;
    }

    public void setMaxPerRoute(int maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
    }

    public String getSSLKey() {
        return sslKey;
    }

    public void setSSLKey(String keyStore) {
        this.sslKey = keyStore;
    }

    public String getSSLCert() {
        return sslCert;
    }

    public void setSSLCert(String trustStore) {
        this.sslCert = trustStore;
    }

    public String getCaCert() {
        return caCert;
    }

    public void setCaCert(String caCert) {
        this.caCert = caCert;
    }

    @Override
    public String toString() {
        return "Connection{"
                + "server='"
                + server
                + '\''
                + ", maxTotal="
                + maxTotal
                + ", maxPerRoute="
                + maxPerRoute
                + ", sslKey='"
                + sslKey
                + '\''
                + ", sslCert='"
                + sslCert
                + '\''
                + ", caCert='"
                + caCert
                + '\''
                + '}';
    }
}
