package com.webank.wecross.p2p.config;

import com.webank.wecross.p2p.netty.common.Host;
import com.webank.wecross.p2p.netty.common.Utils;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class P2PConfig {

    private static Logger logger = LoggerFactory.getLogger(P2PConfig.class);

    private String listenIP;
    private Integer listenPort;

    private Resource caCert;
    private Resource sslCert;
    private Resource sslKey;

    private List<String> peers;

    public List<String> getPeers() {
        return peers;
    }

    public void setPeers(List<String> peers) {
        this.peers = peers;
    }

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

    public Integer getListenPort() {
        return listenPort;
    }

    public void setListenPort(Integer listenPort) {
        this.listenPort = listenPort;
    }

    @Override
    public String toString() {
        return "P2PConfig{"
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
                + ", peers="
                + peers
                + '}';
    }

    /**
     * parser string host format like: "127.0.0.1:1111" to Host
     *
     * @param host
     * @return invalid host format return null otherwise return Host object
     * @throws InvalidParameterException
     */
    public Host toHost(String host) throws InvalidParameterException {

        try {
            // "127.0.0.1:1111"
            String[] r = host.split(":");
            String IP = r[0];
            Integer port = Integer.parseInt(r[1]);

            if (!Utils.validIP(IP) || !Utils.validPort(port)) {
                throw new IllegalArgumentException(" invalid host format : " + host);
            }

            return new Host(IP, port);

        } catch (Exception e) {
            throw new IllegalArgumentException(" invalid host format : " + host);
        }
    }

    public Set<Host> getConnectPeers() {
        Set<Host> hostSet = new HashSet<Host>();
        if (null == peers) {
            return hostSet;
        }

        for (String peer : peers) {
            Host host = toHost(peer);
            logger.info(" => connect peer ip : {}", host);
            hostSet.add(host);
        }

        return hostSet;
    }

    /** check if P2PConfig valid */
    public void validConfig() {
        if (!Utils.validIP(getListenIP())) {
            throw new IllegalArgumentException(" invalid listen ip, listenIp: " + getListenIP());
        }

        if (!Utils.validPort(getListenPort())) {
            throw new IllegalArgumentException(
                    " invalid listen port, listenPort: " + getListenPort());
        }

        if (caCert == null) {
            throw new IllegalArgumentException(" caCert is null ");
        }

        if (sslCert == null) {
            throw new IllegalArgumentException(" sslCert is null ");
        }

        if (sslKey == null) {
            throw new IllegalArgumentException(" sslKey is null ");
        }

        if (peers != null && !peers.isEmpty()) {
            for (String peer : peers) {
                toHost(peer);
            }
        }
    }
}
