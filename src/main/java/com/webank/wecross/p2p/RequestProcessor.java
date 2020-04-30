package com.webank.wecross.p2p;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.p2p.engine.P2PResponse;
import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.p2p.netty.message.processor.Processor;
import com.webank.wecross.p2p.netty.message.proto.Message;
import com.webank.wecross.p2p.netty.message.serialize.MessageSerializer;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.peer.PeerInfoMessageData;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.peer.PeerSeqMessageData;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.Versions;
import com.webank.wecross.routine.RoutineManager;
import com.webank.wecross.routine.htlc.HTLCManager;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.zone.ZoneManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(RequestProcessor.class);

    private PeerManager peerManager;
    private ZoneManager zoneManager;
    private P2PMessageEngine p2pEngine;
    private RoutineManager routineManager;
    private ObjectMapper objectMapper = new ObjectMapper();

    public PeerManager getPeerManager() {
        return peerManager;
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public void setZoneManager(ZoneManager networkManager) {
        this.zoneManager = networkManager;
    }

    public P2PMessageEngine getP2pEngine() {
        return p2pEngine;
    }

    public void setP2pEngine(P2PMessageEngine p2pEngine) {
        this.p2pEngine = p2pEngine;
    }

    public RoutineManager getRoutineManager() {
        return routineManager;
    }

    public void setRoutineManager(RoutineManager routineManager) {
        this.routineManager = routineManager;
    }

    @Override
    public String name() {
        return "RequestProcessor";
    }

    @Override
    public void process(ChannelHandlerContext ctx, Node node, Message message) {
        try {
            String content = new String(message.getData(), "utf-8");

            logger.debug(
                    "  resource request message, host: {}, seq: {}, content: {}",
                    node,
                    message.getSeq(),
                    content);

            P2PMessage<?> p2PMessage = objectMapper.readValue(content, P2PMessage.class);

            String method = p2PMessage.getMethod();
            String r[] = method.split("/");

            Peer peerInfo = peerManager.getPeerInfo(node);

            P2PResponse<Object> p2PResponse = new P2PResponse<>();
            if (r.length == 1) {
                /** method */
                p2PResponse = onStatusMessage(peerInfo, r[0], content);
            } else if (r.length == 4) {
                /** network/stub/resource/method */
                p2PResponse = onTransactionMessage(r[0], r[1], r[2], r[3], content);
            } else {
                // invalid paramter method
                p2PResponse.setMessage(" invalid method paramter format");
                p2PResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
                p2PResponse.setSeq(p2PMessage.getSeq());
                p2PResponse.setVersion(p2PMessage.getVersion());

                logger.error(
                        " invalid method parameter, seq: {}, method: {}", message.getSeq(), method);
            }

            if (p2PResponse.getData() != null) {
                String responseContent = objectMapper.writeValueAsString(p2PResponse);

                // send response
                message.setType(MessageType.RESOURCE_RESPONSE);
                message.setData(responseContent.getBytes());

                MessageSerializer serializer = new MessageSerializer();
                ByteBuf byteBuf = ctx.alloc().buffer();
                serializer.serialize(message, byteBuf);
                ctx.writeAndFlush(byteBuf);

                logger.debug(
                        " resource request, host: {}, seq: {}, response content: {}",
                        node,
                        message.getSeq(),
                        responseContent);
            }
        } catch (Exception e) {
            logger.error(" invalid format, host: {}, e: {}", node, e);
        }
    }

    public P2PResponse<Object> onStatusMessage(
            Peer peerInfo, String method, String p2pRequestString) {

        P2PResponse<Object> response = new P2PResponse<Object>();
        response.setVersion(Versions.currentVersion);
        response.setErrorCode(NetworkQueryStatus.SUCCESS);
        response.setMessage(NetworkQueryStatus.getStatusMessage(NetworkQueryStatus.SUCCESS));

        logger.debug("request string: {}", p2pRequestString);

        try {
            switch (method) {
                case "requestPeerInfo":
                    {
                        logger.debug("Receive requestPeerInfo from peer {}", method, peerInfo);
                        P2PMessage<Object> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<Object>>() {});

                        p2pRequest.checkP2PMessage(method);

                        Map<String, ResourceInfo> resources = zoneManager.getAllResourcesInfo(true);

                        PeerInfoMessageData data = new PeerInfoMessageData();
                        data.setSeq(zoneManager.getSeq());
                        data.setResources(resources);

                        response.setErrorCode(NetworkQueryStatus.SUCCESS);
                        response.setMessage("request " + method + " success");
                        response.setSeq(p2pRequest.getSeq());
                        response.setData(data);
                        break;
                    }
                case "seq":
                    {
                        logger.debug("Receive seq from peer:{}", peerInfo);
                        P2PMessage<PeerSeqMessageData> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<PeerSeqMessageData>>() {});

                        PeerSeqMessageData data = (PeerSeqMessageData) p2pRequest.getData();
                        if (data != null && p2pRequest.getMethod().equals("seq")) {
                            int currentSeq = data.getSeq();
                            if (peerManager.hasPeerChanged(peerInfo.getNode(), currentSeq)) {
                                P2PMessage<Object> msg = new P2PMessage<>();
                                msg.newSeq();

                                msg.setData(null);
                                msg.setVersion(Versions.currentVersion);
                                msg.setMethod("requestPeerInfo");

                                logger.debug(
                                        "Request peerInfo to peer:{}, seq:{}",
                                        peerInfo,
                                        msg.getSeq());

                                P2PMessageCallback<PeerInfoMessageData> callback =
                                        new P2PMessageCallback<PeerInfoMessageData>() {
                                            @Override
                                            public void onResponse(
                                                    int status,
                                                    String message,
                                                    P2PResponse<PeerInfoMessageData> responseMsg) {
                                                logger.trace("Receive peerInfo:{}", peerInfo);
                                                try {
                                                    if (responseMsg != null
                                                            && responseMsg.getData() != null) {

                                                        PeerInfoMessageData data =
                                                                (PeerInfoMessageData)
                                                                        responseMsg.getData();
                                                        int newSeq = data.getSeq();
                                                        if (peerManager.hasPeerChanged(
                                                                peerInfo.getNode(), newSeq)) {
                                                            // compare and update
                                                            Map<String, ResourceInfo> newResources =
                                                                    data.getResources();
                                                            logger.debug(
                                                                    "Update peerInfo from {}, seq:{}, resource:{}",
                                                                    peerInfo,
                                                                    newSeq,
                                                                    newResources);

                                                            // update zonemanager
                                                            zoneManager.removeRemoteResources(
                                                                    peerInfo,
                                                                    peerInfo.getResourceInfos());
                                                            zoneManager.addRemoteResources(
                                                                    peerInfo, newResources);
                                                            peerInfo.setResources(
                                                                    newSeq, newResources);
                                                        } else {
                                                            logger.debug(
                                                                    "Peer info not changed, seq:{}",
                                                                    newSeq);
                                                        }
                                                    } else {
                                                        logger.warn(
                                                                "Receive unrecognized seq message("
                                                                        + responseMsg
                                                                        + ") from peer:"
                                                                        + peerInfo);
                                                    }
                                                } catch (WeCrossException e) {
                                                    logger.error(
                                                            "Update peerInfo error({}): {}",
                                                            e.getErrorCode(),
                                                            e);
                                                } catch (Exception e) {
                                                    logger.error(
                                                            "Update peerInfo error(Internal error): {}",
                                                            e);
                                                }
                                            }
                                        };
                                callback.setTypeReference(
                                        new TypeReference<P2PResponse<PeerInfoMessageData>>() {});

                                p2pEngine.asyncSendMessage(peerInfo, msg, callback);
                            }
                        } else {
                            logger.warn("Receive unrecognized seq message from peer:" + peerInfo);
                        }

                        break;
                    }
                default:
                    {
                        logger.debug("request method: " + method);
                        P2PMessage<Object> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<Object>>() {});
                        response.setErrorCode(NetworkQueryStatus.METHOD_ERROR);
                        response.setSeq(p2pRequest.getSeq());
                        response.setMessage("Unsupported method: " + method);
                        break;
                    }
            }

        } catch (WeCrossException e) {
            logger.warn("Process request error: {}", e.getMessage());
            response.setErrorCode(NetworkQueryStatus.EXCEPTION_FLAG + e.getErrorCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error:", e);

            response.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
            response.setMessage(e.getMessage());
        }

        logger.trace("Response " + response);
        return response;
    }

    public P2PResponse<Object> onTransactionMessage(
            String network, String chain, String resource, String method, String p2pRequestString) {
        Path path = new Path();
        path.setNetwork(network);
        path.setChain(chain);
        path.setResource(resource);

        P2PResponse<Object> p2pResponse = new P2PResponse<Object>();
        p2pResponse.setVersion(Versions.currentVersion);
        p2pResponse.setErrorCode(NetworkQueryStatus.SUCCESS);
        p2pResponse.setMessage(NetworkQueryStatus.getStatusMessage(NetworkQueryStatus.SUCCESS));

        logger.debug("request string: {}", p2pRequestString);

        try {
            Resource resourceObj = zoneManager.getResource(path);
            if (resourceObj == null) {
                logger.warn("Unable to find resource: {}.{}.{}", network, chain, resource);
                throw new Exception("Resource not found");
            } else {
                HTLCManager htlcManager = routineManager.getHtlcManager();
                resourceObj = htlcManager.filterHTLCResource(zoneManager, path, resourceObj);
            }

            switch (method) {
                case "transaction":
                    {
                        logger.debug("On remote transaction request");
                        P2PMessage<Request> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<Request>>() {});

                        p2pRequest.checkP2PMessage(method);

                        P2PMessage<Request> request =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<Request>>() {});

                        Response response =
                                (Response) resourceObj.onRemoteTransaction(request.getData());

                        p2pResponse.setData(response);
                        p2pResponse.setSeq(p2pRequest.getSeq());
                        break;
                    }
                default:
                    {
                        P2PMessage<Object> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<Object>>() {});
                        logger.warn("Unsupported method: {}", method);
                        p2pResponse.setErrorCode(NetworkQueryStatus.METHOD_ERROR);
                        p2pResponse.setMessage("Unsupported method: " + method);
                        p2pResponse.setSeq(p2pRequest.getSeq());
                        break;
                    }
            }
        } catch (WeCrossException e) {
            logger.warn("Process request error: {}", e.getMessage());
            p2pResponse.setErrorCode(NetworkQueryStatus.EXCEPTION_FLAG + e.getErrorCode());
            p2pResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error:", e);

            p2pResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
            p2pResponse.setMessage(e.getLocalizedMessage());
        }

        return p2pResponse;
    }
}
