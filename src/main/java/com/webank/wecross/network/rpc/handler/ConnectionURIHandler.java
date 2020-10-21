package com.webank.wecross.network.rpc.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.UserContext;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.p2p.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.stub.ResourceInfo;
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
            String[] splits = uri.substring(1).split("/");

            if (splits.length != 2) {
                throw new Exception("Unsupported uri: " + uri);
            }

            String operation = splits[1];

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
                    data = handleDefault(userContext, uri, method, content);
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
        public String zone;
        public String chain;
        public String type;
        public Collection<ResourceInfo> resources;
        public Map<String, String> properties;
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
                Collection<ResourceInfo> resources = new HashSet<>();

                for (Resource resource : chainEntry.getValue().getResources().values()) {
                    resources.add(resource.getResourceInfo());
                }

                ChainDetails chainDetails = new ChainDetails();
                chainDetails.zone = zone;
                chainDetails.chain = chain;
                chainDetails.type = type;
                chainDetails.properties = properties;
                chainDetails.resources = resources;

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

    private Object handleDefault(UserContext userContext, String uri, String method, String content)
            throws WeCrossException {
        throw new WeCrossException(
                WeCrossException.ErrorCode.METHOD_ERROR, "Unsupported method: " + method);
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
