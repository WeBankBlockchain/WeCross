package com.webank.wecross.host.config;

import com.webank.wecross.host.Peer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "peer-manager")
public class PeerManagerConfig {

    private Logger logger = LoggerFactory.getLogger(PeerManagerConfig.class);

    private List<String> peers;

    private Peer parsePeerString(String peerString) throws Exception {
        try {
            String[] infos = peerString.split("@");
            return new Peer(infos[1], infos[0]);
        } catch (Exception e) {
            throw new Exception(
                    "Unrecognized peer configuration: " + peerString + " exception: " + e);
        }
    }

    @Bean
    public Map<String, Peer> initPeers() {
        Map<String, Peer> ret = new HashMap<>();
        if (peers == null) {
            logger.info("no peer configuration found");
            return ret;
        }
        for (String peerString : peers) {
            try {
                Peer peer = parsePeerString(peerString);
                logger.info("Load peer name:{} url:{}", peer.getName(), peer.getUrl());
                ret.put(peer.getUrl(), peer);
            } catch (Exception e) {
                logger.error("Ignore unrecognized peer configuration: " + peerString);
                continue;
            }
        }
        return ret;
    }

    public void setPeers(List<String> peers) {
        this.peers = peers;
    }
}
