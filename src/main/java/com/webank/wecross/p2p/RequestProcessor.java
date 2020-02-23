package com.webank.wecross.p2p;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.common.QueryStatus;
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
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceInfo;
import com.webank.wecross.restserver.Versions;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import com.webank.wecross.zone.ZoneManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(RequestProcessor.class);

    private PeerManager peerManager;
    private ZoneManager zoneManager;
    private P2PMessageEngine p2pEngine;

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

    @Override
    public String name() {
        return "RequestProcessor";
    }

    @Override
    public void process(ChannelHandlerContext ctx, Node node, Message message) {
        try {
            String content = new String(message.getData(), "utf-8");

            logger.info(
                    "  resource request message, host: {}, seq: {}, content: {}",
                    node,
                    message.getSeq(),
                    content);

            P2PMessage<?> p2PMessage =
                    ObjectMapperFactory.getObjectMapper().readValue(content, P2PMessage.class);

            String method = p2PMessage.getMethod();
            String r[] = method.split("/");
            
            Peer peerInfo = peerManager.getPeerInfo(node);

            P2PResponse<Object> p2PResponse = new P2PResponse<>();
            if (r.length == 1) {
                /** method */
                p2PResponse = onStatusMessage(peerInfo, r[0], content);
            } else if (r.length == 4) {
                /** network/stub/resource/method */
                p2PResponse = onResourceMessage(r[0], r[1], r[2], r[3], content);
            } else {
                // invalid paramter method
                p2PResponse.setMessage(" invalid method paramter format");
                p2PResponse.setResult(QueryStatus.INTERNAL_ERROR);
                p2PResponse.setSeq(p2PMessage.getSeq());
                p2PResponse.setVersion(p2PMessage.getVersion());

                logger.error(
                        " invalid method parameter, seq: {}, method: {}", message.getSeq(), method);
            }

            if(p2PResponse.getData() != null) {
            String responseContent =
                    ObjectMapperFactory.getObjectMapper().writeValueAsString(p2PResponse);

	            // send response
	            message.setType(MessageType.RESOURCE_RESPONSE);
	            message.setData(responseContent.getBytes());
	
	            MessageSerializer serializer = new MessageSerializer();
	            ByteBuf byteBuf = ctx.alloc().buffer();
	            serializer.serialize(message, byteBuf);
	            ctx.writeAndFlush(byteBuf);
	
	            logger.info(
	                    " resource request, host: {}, seq: {}, response content: {}",
	                    node,
	                    message.getSeq(),
	                    responseContent);
            }
        } catch (Exception e) {
            logger.error(" invalid format, host: {}, e: {}", node, e);
        }
    }

    public P2PResponse<Object> onStatusMessage(Peer peerInfo, String method, String p2pRequestString) {

        P2PResponse<Object> response = new P2PResponse<Object>();
        response.setVersion(Versions.currentVersion);
        response.setResult(QueryStatus.SUCCESS);
        response.setMessage(QueryStatus.getStatusMessage(QueryStatus.SUCCESS));

        logger.debug("request string: {}", p2pRequestString);

        try {
            switch (method) {
                case "requestPeerInfo":
                    {
                        logger.debug("request method: " + method);
                        P2PMessage<Object> p2pRequest =
                                ObjectMapperFactory.getObjectMapper()
                                        .readValue(
                                                p2pRequestString,
                                                new TypeReference<P2PMessage<Object>>() {});

                        p2pRequest.checkP2PMessage(method);

                        Map<String, ResourceInfo> resources = zoneManager.getAllResourceInfo(true);
                    	
                        Set<ResourceInfo> activeResourceSet = new HashSet<>();
                        for (ResourceInfo activeResource : resources.values()) {
                            activeResourceSet.add(activeResource);
                        }

                        logger.info("Receive request peer info");
                        PeerInfoMessageData data = new PeerInfoMessageData();
                        data.setSeq(peerManager.getSeq());
                        data.setResources(activeResourceSet);

                        response.setResult(QueryStatus.SUCCESS);
                        response.setMessage("request " + method + " success");
                        response.setSeq(p2pRequest.getSeq());
                        response.setData(data);
                        break;
                    }
                case "seq":
                {
                	logger.info("Receive peer seq from peer:{}", peerInfo);
                	 P2PMessage<PeerSeqMessageData> p2pRequest =
                             ObjectMapperFactory.getObjectMapper()
                                     .readValue(
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

                            logger.info("Request peer info, peer:{}, seq:{}", peerInfo, msg.getSeq());
                            p2pEngine.asyncSendMessage(peerInfo, msg, new P2PMessageCallback<PeerInfoMessageData>() {
								@Override
								public void onResponse(int status, String message, P2PMessage<PeerInfoMessageData> msg) {
									logger.info("Receive peer info from {}", peerInfo);

							        PeerInfoMessageData data = (PeerInfoMessageData) msg.getData();
							        if (data != null && msg.getMethod().equals("peerInfo")) {
							            int newSeq = data.getSeq();
							            if (peerManager.hasPeerChanged(peerInfo.getNode(), newSeq)) {
							                // compare and update
							                Set<ResourceInfo> newResources = data.getResources();
							                logger.info(
							                        "Update peerInfo from {}, seq:{}, resource:{}",
							                        peerInfo,
							                        newSeq,
							                        newResources);

							                //update zonemanager
							                zoneManager.removeRemoteResources(peerInfo, peerInfo.getResourceInfos());
							                peerInfo.setResources(newSeq, newResources);
							                peerInfo.setSeq(newSeq);
							                zoneManager.addRemoteResources(peerInfo, newResources);
							            } else {
							                logger.info("Peer info not changed, seq:{}", newSeq);
							            }
							        } else {
							            logger.warn("Receive unrecognized seq message from peer:" + peerInfo);
							        }
								}
                            	
                            });
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
                                ObjectMapperFactory.getObjectMapper()
                                        .readValue(
                                                p2pRequestString,
                                                new TypeReference<P2PMessage<Object>>() {});
                        response.setResult(QueryStatus.METHOD_ERROR);
                        response.setSeq(p2pRequest.getSeq());
                        response.setMessage("Unsupported method: " + method);
                        break;
                    }
            }

        } catch (WeCrossException e) {
            logger.warn("Process request error: {}", e.getMessage());
            response.setResult(QueryStatus.EXCEPTION_FLAG + e.getErrorCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error:", e);

            response.setResult(QueryStatus.INTERNAL_ERROR);
            response.setMessage(e.getMessage());
        }

        logger.trace("Response " + response);
        return response;
    }

    public P2PResponse<Object> onResourceMessage(
            String network, String stub, String resource, String method, String p2pRequestString) {
        Path path = new Path();
        path.setNetwork(network);
        path.setChain(stub);
        path.setResource(resource);

        P2PResponse<Object> p2pResponse = new P2PResponse<Object>();
        p2pResponse.setVersion(Versions.currentVersion);
        p2pResponse.setResult(QueryStatus.SUCCESS);
        p2pResponse.setMessage(QueryStatus.getStatusMessage(QueryStatus.SUCCESS));

        logger.debug("request string: {}", p2pRequestString);

        try {
            Resource resourceObj = zoneManager.getResource(path);
            if (resourceObj == null) {
                logger.warn("Unable to find resource: {}.{}.{}", network, stub, resource);

                throw new Exception("Resource not found");
            }

            switch (method) {
                case "getData":
                    {
                        P2PMessage<GetDataRequest> p2pRequest =
                                ObjectMapperFactory.getObjectMapper()
                                        .readValue(
                                                p2pRequestString,
                                                new TypeReference<P2PMessage<GetDataRequest>>() {});

                        p2pRequest.checkP2PMessage(method);

                        GetDataRequest getDataRequest = p2pRequest.getData();
                        GetDataResponse getDataResponse = resourceObj.getData(getDataRequest);

                        p2pResponse.setData(getDataResponse);
                        p2pResponse.setSeq(p2pRequest.getSeq());
                        break;
                    }
                case "setData":
                    {
                        P2PMessage<SetDataRequest> p2pRequest =
                                ObjectMapperFactory.getObjectMapper()
                                        .readValue(
                                                p2pRequestString,
                                                new TypeReference<P2PMessage<SetDataRequest>>() {});

                        p2pRequest.checkP2PMessage(method);

                        SetDataRequest setDataRequest = (SetDataRequest) p2pRequest.getData();
                        SetDataResponse setDataResponse =
                                (SetDataResponse) resourceObj.setData(setDataRequest);

                        p2pResponse.setData(setDataResponse);
                        p2pResponse.setSeq(p2pRequest.getSeq());
                        break;
                    }
                case "call":
                    {
                        P2PMessage<TransactionRequest> p2pRequest =
                                ObjectMapperFactory.getObjectMapper()
                                        .readValue(
                                                p2pRequestString,
                                                new TypeReference<
                                                        P2PMessage<TransactionRequest>>() {});

                        p2pRequest.checkP2PMessage(method);

                        TransactionRequest transactionRequest =
                                (TransactionRequest) p2pRequest.getData();
                        TransactionResponse transactionResponse =
                                (TransactionResponse) resourceObj.call(transactionRequest);

                        p2pResponse.setData(transactionResponse);
                        p2pResponse.setSeq(p2pRequest.getSeq());
                        break;
                    }
                case "sendTransaction":
                    {
                        P2PMessage<TransactionRequest> p2pRequest =
                                ObjectMapperFactory.getObjectMapper()
                                        .readValue(
                                                p2pRequestString,
                                                new TypeReference<
                                                        P2PMessage<TransactionRequest>>() {});

                        p2pRequest.checkP2PMessage(method);

                        TransactionRequest transactionRequest =
                                (TransactionRequest) p2pRequest.getData();
                        TransactionResponse transactionResponse =
                                (TransactionResponse)
                                        resourceObj.sendTransaction(transactionRequest);

                        p2pResponse.setData(transactionResponse);
                        p2pResponse.setSeq(p2pRequest.getSeq());
                        break;
                    }
                default:
                    {
                        P2PMessage<Object> p2pRequest =
                                ObjectMapperFactory.getObjectMapper()
                                        .readValue(
                                                p2pRequestString,
                                                new TypeReference<P2PMessage<Object>>() {});
                        logger.warn("Unsupported method: {}", method);
                        p2pResponse.setResult(QueryStatus.METHOD_ERROR);
                        p2pResponse.setMessage("Unsupported method: " + method);
                        p2pResponse.setSeq(p2pRequest.getSeq());
                        break;
                    }
            }
        } catch (WeCrossException e) {
            logger.warn("Process request error: {}", e.getMessage());
            p2pResponse.setResult(QueryStatus.EXCEPTION_FLAG + e.getErrorCode());
            p2pResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error:", e);

            p2pResponse.setResult(QueryStatus.INTERNAL_ERROR);
            p2pResponse.setMessage(e.getLocalizedMessage());
        }

        return p2pResponse;
    }
}
