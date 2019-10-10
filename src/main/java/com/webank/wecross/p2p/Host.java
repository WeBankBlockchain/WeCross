package com.webank.wecross.p2p;

import com.webank.wecross.core.StateResponse;
import com.webank.wecross.restserver.RestResponse;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class Host {
    private RestTemplate restTemplate = new RestTemplate();
    private Map<String, PeerState> peers;
    private Logger logger = LoggerFactory.getLogger(Host.class);

    public void syncAllState() {
        for (Entry<String, PeerState> entry : peers.entrySet()) {
            ResponseEntity<RestResponse<StateResponse>> response =
                    restTemplate.exchange(
                            entry.getKey() + "/state",
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<RestResponse<StateResponse>>() {});

            StateResponse stateResponse = response.getBody().getData();
            entry.getValue().setSeq(stateResponse.getSeq());

            logger.info("Get seq: {} from peer: {}", stateResponse.getSeq(), entry.getKey());
        }
    }

    public Map<String, PeerState> getPeers() {
        return peers;
    }

    public void setPeers(Map<String, PeerState> peers) {
        this.peers = peers;
    }
}
