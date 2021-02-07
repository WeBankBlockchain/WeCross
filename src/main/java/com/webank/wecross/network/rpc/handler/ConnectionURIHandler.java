package com.webank.wecross.network.rpc.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.UserContext;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.network.UriDecoder;
import com.webank.wecross.network.p2p.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.peer.PeerManager.PeerDetails;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionURIHandler implements URIHandler {
    private Logger logger = LoggerFactory.getLogger(ConnectionURIHandler.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    private P2PService p2PService;
    private PeerManager peerManager;
    private ZoneManager zoneManager;

    private static interface HandleCallback {
        public void onResponse(Exception e, Object response);
    }

    public class ListData {
        private long size;
        private Object data;

        ListData(long size, Object data) {
            this.size = size;
            this.data = data;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }

    @Override
    public void handle(
            UserContext userContext, String uri, String method, String content, Callback callback) {
        logger.debug(
                "Handle rpc connection request: {} {} {} {}", userContext, uri, method, content);
        try {
            UriDecoder uriDecoder = new UriDecoder(uri);
            String operation = uriDecoder.getMethod();

            HandleCallback handleCallback =
                    new HandleCallback() {
                        @Override
                        public void onResponse(Exception e, Object response) {
                            RestResponse<Object> restResponse = new RestResponse<Object>();

                            if (e == null) {
                                restResponse.setData(response);
                                restResponse.setErrorCode(NetworkQueryStatus.SUCCESS);
                                restResponse.setMessage(
                                        NetworkQueryStatus.getStatusMessage(
                                                NetworkQueryStatus.SUCCESS));
                                callback.onResponse(restResponse);
                            } else {
                                String message =
                                        "Handle rpc connection request exception: "
                                                + e.getMessage();
                                logger.warn("Error", e);

                                restResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
                                restResponse.setMessage(message);
                                callback.onResponse(restResponse);
                                return;
                            }
                        }
                    };

            switch (operation) {
                case "listChains":
                    handleListChains(userContext, uri, method, content, handleCallback);
                    break;
                case "listZones":
                    handleListZones(userContext, uri, method, content, handleCallback);
                    break;
                    /*
                     * case "addChain": data = handleAddChain(userContext, uri, method, content);
                     * break; case "updateChain": data = handleUpdateChain(userContext, uri, method,
                     * content); break; case "removeChain": data = handleRemoveChain(userContext,
                     * uri, method, content); break;
                     */
                case "listPeers":
                    handleListPeers(userContext, uri, method, content, handleCallback);
                    break;
                case "addPeer":
                    handleAddPeer(userContext, uri, method, content, handleCallback);
                    break;
                case "removePeer":
                    handleRemovePeer(userContext, uri, method, content, handleCallback);
                    break;
                default:
                    {
                        RestResponse<Object> restResponse = new RestResponse<Object>();

                        logger.warn("Unsupported method: {}", method);
                        restResponse.setErrorCode(NetworkQueryStatus.URI_PATH_ERROR);
                        restResponse.setMessage("Unsupported method: " + method);
                        callback.onResponse(restResponse);
                        return;
                    }
            }
        } catch (Exception e) {
            RestResponse<Object> restResponse = new RestResponse<Object>();

            String message = "Handle rpc connection request exception: " + e.getMessage();
            logger.warn("ERROR", e);
            restResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
            restResponse.setMessage(message);
            callback.onResponse(restResponse);
            return;
        }
    }

    public class ChainDetail {
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

    private void handleListChains(
            UserContext userContext,
            String uri,
            String method,
            String content,
            HandleCallback callback) {
        UriDecoder uriDecoder = new UriDecoder(uri);
        String zone = "";
        int offset = 0;
        int size = 0;
        try {
            zone = uriDecoder.getQueryBykey("zone");
            offset = Integer.valueOf(uriDecoder.getQueryBykey("offset"));
            size = Integer.valueOf(uriDecoder.getQueryBykey("size"));
        } catch (Exception e) {
            // can't get offset and size, query all
        }

        List<ChainDetail> chains = new LinkedList<ChainDetail>();

        Zone zoneObj = zoneManager.getZone(zone);
        if (zoneObj == null) {
            callback.onResponse(null, new ListData(0, chains));
            return;
        }

        long total = zoneManager.getZone(zone).getChains().size();
        if (offset > total) {
            callback.onResponse(null, new ListData(0, chains));
            return;
        }

        if (total > offset + size) {
            total = offset + size;
        }

        int i = 0;
        AtomicLong current = new AtomicLong(0);

        if (zoneManager.getZone(zone).getChains().isEmpty()) {
            callback.onResponse(null, new ListData(0, chains));
        } else {
            for (Map.Entry<String, Chain> chainEntry :
                    zoneManager.getZone(zone).getChains().entrySet()) {
                if ((offset == 0 && size == 0) || (i >= offset && i < total)) {
                    String chain = chainEntry.getKey();
                    String type = chainEntry.getValue().getStubType();
                    Map<String, String> properties = chainEntry.getValue().getProperties();

                    ChainDetail chainDetails = new ChainDetail();
                    chains.add(chainDetails);

                    chainDetails.setZone(zone);
                    chainDetails.setChain(chain);
                    chainDetails.setType(type);
                    chainDetails.setLocal(chainEntry.getValue().hasLocalConnection());
                    chainDetails.setProperties(properties);

                    long totalEnd = total;
                    final long totalSize = zoneManager.getZone(zone).getChains().size();
                    if (offset == 0 && size == 0) {
                        totalEnd = totalSize;
                    }
                    final long totalEnd2 = totalEnd;
                    chainEntry
                            .getValue()
                            .getBlockManager()
                            .asyncGetBlockNumber(
                                    (exception, number) -> {
                                        chainDetails.setBlockNumber(number);

                                        long finish = current.addAndGet(1);
                                        if (finish == totalEnd2) {
                                            callback.onResponse(
                                                    null, new ListData(totalSize, chains));
                                        }
                                    });
                } else if (i >= total) {
                    break;
                } else {
                    current.addAndGet(1);
                }
                ++i;
            }
        }
    }

    private void handleListZones(
            UserContext userContext,
            String uri,
            String method,
            String content,
            HandleCallback callback) {
        UriDecoder uriDecoder = new UriDecoder(uri);
        int offset = 0;
        int size = 0;
        try {
            offset = Integer.valueOf(uriDecoder.getQueryBykey("offset"));
            size = Integer.valueOf(uriDecoder.getQueryBykey("size"));
        } catch (Exception e) {
            // can't get offset and size, query all
        }

        List<String> zones = new LinkedList<String>();

        long i = 0;
        for (Map.Entry<String, Zone> zoneEntry : zoneManager.getZones().entrySet()) {
            if ((offset == 0 && size == 0) || (i >= offset && i < offset + size)) {
                zones.add(zoneEntry.getKey());
            } else if (i >= offset + size) {
                break;
            }

            ++i;
        }

        callback.onResponse(null, new ListData(zoneManager.getZones().size(), zones));
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

    private void handleListPeers(
            UserContext userContext,
            String uri,
            String method,
            String content,
            HandleCallback callback) {
        UriDecoder uriDecoder = new UriDecoder(uri);
        int offset = 0;
        int size = 0;
        try {
            offset = Integer.parseInt(uriDecoder.getQueryBykey("offset"));
            size = Integer.parseInt(uriDecoder.getQueryBykey("size"));
        } catch (Exception e) {
            // can't get offset and size, query all
        }

        Collection<PeerDetails> peers = peerManager.getPeerDetails();
        PeerDetails localRouter = getLocalDetails();
        peers.add(localRouter);

        Iterator<PeerDetails> iterator = peers.iterator();
        Collection<PeerDetails> result = new LinkedList<>();

        if (offset > peers.size()) {
            callback.onResponse(null, new ListData(peers.size(), result));
            return;
        }

        if (offset == 0 && size == 0) {
            callback.onResponse(null, new ListData(peers.size(), peers));
            return;
        }

        int i = 0;
        while (iterator.hasNext()) {
            PeerDetails peer = iterator.next();
            if (i >= offset && i < offset + size) {
                result.add(peer);
            }

            if (i >= offset + size) {
                break;
            }

            ++i;
        }

        callback.onResponse(null, new ListData(peers.size(), peers));
    }

    private PeerDetails getLocalDetails() {
        PeerDetails localRouter = peerManager.new PeerDetails();
        localRouter.nodeID = "Local";
        localRouter.address =
                p2PService.getNettyService().getInitializer().getConfig().getListenIP()
                        + ":"
                        + p2PService.getNettyService().getInitializer().getConfig().getListenPort();
        localRouter.chainInfos = new HashSet<>();
        for (Zone zone : zoneManager.getZones().values()) {
            Map<String, Chain> zoneChains = zone.getChains();
            for (Map.Entry<String, Chain> chainEntry : zoneChains.entrySet()) {
                if (chainEntry.getValue().getLocalConnection() != null) {
                    PeerManager.ChainInfoDetails chainInfoDetails =
                            peerManager.new ChainInfoDetails();
                    chainInfoDetails.path = chainEntry.getKey();
                    chainInfoDetails.stubType = chainEntry.getValue().getStubType();
                    localRouter.chainInfos.add(chainInfoDetails);
                }
            }
        }
        return localRouter;
    }

    public void setZoneManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    public static class AddressData {
        public String address;
    }

    private void handleAddPeer(
            UserContext userContext,
            String uri,
            String method,
            String content,
            HandleCallback callback) {

        try {
            RestRequest<AddressData> restRequest =
                    objectMapper.readValue(
                            content, new TypeReference<RestRequest<AddressData>>() {});
            AddressData data = restRequest.getData();

            p2PService.getNettyService().getInitializer().addConfiguredPeer(data.address);

        } catch (Exception e) {
            callback.onResponse(null, StatusResponse.buildFailedResponse(e.getMessage()));
        }

        callback.onResponse(null, StatusResponse.buildSuccessResponse());
    }

    private void handleRemovePeer(
            UserContext userContext,
            String uri,
            String method,
            String content,
            HandleCallback callback) {

        try {

            RestRequest<AddressData> restRequest =
                    objectMapper.readValue(
                            content, new TypeReference<RestRequest<AddressData>>() {});
            AddressData data = restRequest.getData();

            p2PService.getNettyService().getInitializer().removeConfiguredPeer(data.address);

        } catch (Exception e) {
            callback.onResponse(e, StatusResponse.buildFailedResponse(e.getMessage()));
        }

        callback.onResponse(null, StatusResponse.buildSuccessResponse());
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
            response.message = "Success";
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
