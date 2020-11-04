package com.webank.wecross.network.rpc.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.UserContext;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.network.UriDecoder;
import com.webank.wecross.network.p2p.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionURIHandler implements URIHandler {
    private Logger logger = LoggerFactory.getLogger(ConnectionURIHandler.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    private P2PService p2PService;
    private PeerManager peerManager;
    private ZoneManager zoneManager;

    @Override
    public void handle(
            UserContext userContext, String uri, String method, String content, Callback callback) {
        logger.debug(
                "Handle rpc connection request: {} {} {} {}", userContext, uri, method, content);
        RestResponse<Object> restResponse = new RestResponse();
        Object data;
        try {
            UriDecoder uriDecoder = new UriDecoder(uri);
            String operation = uriDecoder.getMethod();
            switch (operation) {
                case "listChains":
                    data = handleListChains(userContext, uri, method, content);
                    break;
                case "addChain":
                    data = handleAddChain(userContext, uri, method, content);
                    break;
                case "updateChain":
                    data = handleUpdateChain(userContext, uri, method, content);
                    break;
                case "removeChain":
                    data = handleRemoveChain(userContext, uri, method, content);
                    break;
                case "listPeers":
                    data = handleListPeers(userContext, uri, method, content);
                    break;
                case "addPeer":
                    data = handleAddPeer(userContext, uri, method, content);
                    break;
                case "removePeer":
                    data = handleRemovePeer(userContext, uri, method, content);
                    break;
                default:
                    {
                        logger.warn("Unsupported method: {}", method);
                        restResponse.setErrorCode(NetworkQueryStatus.URI_PATH_ERROR);
                        restResponse.setMessage("Unsupported method: " + method);
                        callback.onResponse(restResponse);
                        return;
                    }
            }
        } catch (Exception e) {
            String message = "Handle rpc connection request exception: " + e.getMessage();
            logger.warn(message);
            restResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
            restResponse.setMessage(message);
            callback.onResponse(restResponse);
            return;
        }

        restResponse.setData(data);
        restResponse.setErrorCode(NetworkQueryStatus.SUCCESS);
        restResponse.setMessage("success");
        callback.onResponse(restResponse);
    }

    public static class ChainDetails {
        private String zone;
        private String chain;
        private String type;
        private boolean isLocal;
        private long blockNumber;
        private Map<String, String> properties;
        
        public String getZone() {
            return zone;
        }
        public void setZone(String zone) {
            this.zone = zone;
        }
        public String getChain() {
            return chain;
        }
        public void setChain(String chain) {
            this.chain = chain;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public boolean isLocal() {
            return isLocal;
        }
        public void setLocal(boolean isLocal) {
            this.isLocal = isLocal;
        }
        public long getBlockNumber() {
            return blockNumber;
        }
        public void setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
        }
        public Map<String, String> getProperties() {
            return properties;
        }
        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }
    }

    private Object handleListChains(
            UserContext userContext, String uri, String method, String content) {

        Collection<ChainDetails> chains = new HashSet<>();
        for (Map.Entry<String, Zone> zoneEntry : zoneManager.getZones().entrySet()) {
            String zone = zoneEntry.getKey();
            for (Map.Entry<String, Chain> chainEntry :
                    zoneEntry.getValue().getChains().entrySet()) {
                String chain = chainEntry.getKey();
                String type = chainEntry.getValue().getStubType();
                Map<String, String> properties = chainEntry.getValue().getProperties();
                // Collection<ChainDetails.ResourceInfoDetails> resources = new HashSet<>();
                /*
                for (Resource resource : chainEntry.getValue().getResources().values()) {
                    ResourceInfo resourceInfo = resource.getResourceInfo();

                    ChainDetails.ResourceInfoDetails resourceInfoDetails =
                            new ChainDetails.ResourceInfoDetails();
                    resourceInfoDetails.name = resourceInfo.getName();
                    resourceInfoDetails.stubType = resourceInfo.getStubType();

                    resources.add(resourceInfoDetails);
                }
                */

                ChainDetails chainDetails = new ChainDetails();
                chainDetails.setZone(zone);
                chainDetails.setChain(chain);
                chainDetails.setType(type);
                chainDetails.setLocal(chainEntry.getValue().hasLocalConnection());
                chainEntry.getValue().getBlockManager().asyncGetBlockNumber((exception, number) -> {
                    chainDetails.setBlockNumber(number);
                }); 
                
                chainDetails.chain = chain;
                chainDetails.type = type;
                chainDetails.properties = properties;
                // chainDetails.resources = resources;

                chains.add(chainDetails);
            }
        }

        return chains;
    }

    private Object handleAddChain(
            UserContext userContext, String uri, String method, String content) {
        return null;
    }

    private Object handleUpdateChain(
            UserContext userContext, String uri, String method, String content) {
        return null;
    }

    private Object handleRemoveChain(
            UserContext userContext, String uri, String method, String content) {
        return null;
    }

    private Object handleListPeers(
            UserContext userContext, String uri, String method, String content) {
        return peerManager.getPeerDetails();
    }

    public void setZoneManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    public static class AddressData {
        public String address;
    }

    private Object handleAddPeer(
            UserContext userContext, String uri, String method, String content) {

        try {
            RestRequest<AddressData> restRequest =
                    objectMapper.readValue(
                            content, new TypeReference<RestRequest<AddressData>>() {});
            AddressData data = restRequest.getData();

            p2PService.getNettyService().getInitializer().addConfiguredPeer(data.address);

        } catch (Exception e) {
            return StatusResponse.buildFailedResponse(e.getMessage());
        }

        return StatusResponse.buildSuccessResponse();
    }

    private Object handleRemovePeer(
            UserContext userContext, String uri, String method, String content) {

        try {

            RestRequest<AddressData> restRequest =
                    objectMapper.readValue(
                            content, new TypeReference<RestRequest<AddressData>>() {});
            AddressData data = restRequest.getData();

            p2PService.getNettyService().getInitializer().removeConfiguredPeer(data.address);

        } catch (Exception e) {
            return StatusResponse.buildFailedResponse(e.getMessage());
        }

        return StatusResponse.buildSuccessResponse();
    }

    public void setP2PService(P2PService p2PService) {
        this.p2PService = p2PService;
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }

    public static class StatusResponse {
        public int errorCode;
        public String message;

        public static StatusResponse buildSuccessResponse() {
            StatusResponse response = new StatusResponse();
            response.errorCode = 0;
            response.message = "success";
            return response;
        }

        public static StatusResponse buildFailedResponse(String message) {
            StatusResponse response = new StatusResponse();
            response.errorCode = 1;
            response.message = message;
            return response;
        }
    }
}
