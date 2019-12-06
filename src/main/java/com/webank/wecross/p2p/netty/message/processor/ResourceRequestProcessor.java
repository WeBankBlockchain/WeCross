package com.webank.wecross.p2p.netty.message.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.engine.P2PResponse;
import com.webank.wecross.p2p.netty.common.Host;
import com.webank.wecross.p2p.netty.common.Utils;
import com.webank.wecross.p2p.netty.message.MessageType;
import com.webank.wecross.p2p.netty.message.proto.Message;
import com.webank.wecross.p2p.netty.message.serialize.MessageSerializer;
import com.webank.wecross.peer.PeerInfoMessageData;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.peer.PeerSeqMessageData;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResourceRequestProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(ResourceRequestProcessor.class);

    private PeerManager peerManager;
    private NetworkManager networkManager;

    public PeerManager getPeerManager() {
        return peerManager;
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public String name() {
        return "SourceRequest";
    }

    @Override
    public void process(ChannelHandlerContext ctx, Message message) {
        Host host = Utils.channelContextPeerHost(ctx);
        try {
            String content = new String(message.getData(), "utf-8");

            logger.info(
                    "  resource request message, host: {}, seq: {}, content: {}",
                    host,
                    message.getSeq(),
                    content);

            P2PMessage p2PMessage =
                    ObjectMapperFactory.getObjectMapper().readValue(content, P2PMessage.class);

            String method = p2PMessage.getMethod();
            String r[] = method.split("/");

            P2PResponse<Object> p2PResponse = new P2PResponse<>();
            if (r.length == 1) {
                /** method */
                p2PResponse = handlePeer(r[0], content);
            } else if (r.length == 4) {
                /** network/stub/resource/method */
                p2PResponse = handleRemote(r[0], r[1], r[2], r[3], content);
            } else {
                // invalid paramter method
                p2PResponse.setMessage(" invalid method paramter format");
                p2PResponse.setResult(Status.INTERNAL_ERROR);
                p2PResponse.setSeq(p2PMessage.getSeq());
                p2PResponse.setVersion(p2PMessage.getVersion());

                logger.error(
                        " invalid method parameter, seq: {}, method: {}", message.getSeq(), method);
            }

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
                    host,
                    message.getSeq(),
                    responseContent);
        } catch (Exception e) {
            logger.error(" invalid format, host: {}, e: {}", host, e);
        }
    }

    public P2PResponse<Object> handlePeer(String method, String p2pRequestString) {

        P2PResponse<Object> response = new P2PResponse<Object>();
        response.setVersion("0.2");
        response.setResult(Status.SUCCESS);

        logger.debug("request string: {}", p2pRequestString);

        try {
            switch (method) {
                case "requestSeq":
                    {
                        logger.debug("request method: " + method);
                        P2PMessage<Object> p2pRequest =
                                ObjectMapperFactory.getObjectMapper()
                                        .readValue(
                                                p2pRequestString,
                                                new TypeReference<P2PMessage<Object>>() {});

                        p2pRequest.checkP2PMessage(method);
                        PeerSeqMessageData data =
                                (PeerSeqMessageData)
                                        peerManager.onRestfulPeerMessage(method, p2pRequest);

                        response.setResult(Status.SUCCESS);
                        response.setMessage("request " + method + " method success");
                        response.setSeq(p2pRequest.getSeq());
                        response.setData(data);
                        break;
                    }
                case "requestPeerInfo":
                    {
                        logger.debug("request method: " + method);
                        P2PMessage<Object> p2pRequest =
                                ObjectMapperFactory.getObjectMapper()
                                        .readValue(
                                                p2pRequestString,
                                                new TypeReference<P2PMessage<Object>>() {});

                        p2pRequest.checkP2PMessage(method);

                        PeerInfoMessageData data =
                                (PeerInfoMessageData)
                                        peerManager.onRestfulPeerMessage(method, p2pRequest);

                        response.setResult(Status.SUCCESS);
                        response.setMessage("request " + method + " method success");
                        response.setSeq(p2pRequest.getSeq());
                        response.setData(data);
                        break;
                    }
                case "seq":
                    {
                        logger.debug("request method: " + method);
                        P2PMessage<PeerSeqMessageData> p2pRequest =
                                ObjectMapperFactory.getObjectMapper()
                                        .readValue(
                                                p2pRequestString,
                                                new TypeReference<
                                                        P2PMessage<PeerSeqMessageData>>() {});

                        p2pRequest.checkP2PMessage(method);

                        peerManager.onRestfulPeerMessage(method, p2pRequest);

                        response.setResult(Status.SUCCESS);
                        response.setMessage("request " + method + " method success");
                        response.setSeq(p2pRequest.getSeq());
                        response.setData(null);
                        break;
                    }
                case "peerInfo":
                    {
                        logger.debug("request method: " + method);
                        P2PMessage<PeerInfoMessageData> p2pRequest =
                                ObjectMapperFactory.getObjectMapper()
                                        .readValue(
                                                p2pRequestString,
                                                new TypeReference<
                                                        P2PMessage<PeerInfoMessageData>>() {});

                        p2pRequest.checkP2PMessage(method);

                        peerManager.onRestfulPeerMessage(method, p2pRequest);

                        response.setResult(Status.SUCCESS);
                        response.setMessage("request " + method + " method success");
                        response.setSeq(p2pRequest.getSeq());
                        response.setData(null);
                        break;
                    }
                case "requestChainState":
                    {
                        logger.debug("request method: " + method);
                        response.setMessage("request " + method + " method success");
                        break;
                    }

                case "chainState":
                    {
                        logger.debug("request method: " + method);
                        response.setMessage("request " + method + " method success");
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
                        response.setResult(Status.METHOD_ERROR);
                        response.setSeq(p2pRequest.getSeq());
                        response.setMessage("Unsupported method: " + method);
                        break;
                    }
            }

        } catch (WeCrossException e) {
            logger.warn("Process request error: {}", e.getMessage());
            response.setResult(e.getErrorCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error:", e);

            response.setResult(Status.INTERNAL_ERROR);
            response.setMessage(e.getMessage());
        }

        logger.trace("Response " + response);
        return response;
    }

    public P2PResponse<Object> handleRemote(
            String network, String stub, String resource, String method, String p2pRequestString) {
        Path path = new Path();
        path.setNetwork(network);
        path.setChain(stub);
        path.setResource(resource);

        P2PResponse<Object> p2pResponse = new P2PResponse<Object>();
        p2pResponse.setVersion("0.2");
        p2pResponse.setResult(Status.SUCCESS);

        logger.debug("request string: {}", p2pRequestString);

        try {
            Resource resourceObj = networkManager.getResource(path);
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
                        p2pResponse.setResult(Status.METHOD_ERROR);
                        p2pResponse.setMessage("Unsupported method: " + method);
                        p2pResponse.setSeq(p2pRequest.getSeq());
                        break;
                    }
            }
        } catch (WeCrossException e) {
            logger.warn("Process request error: {}", e.getMessage());
            p2pResponse.setResult(e.getErrorCode());
            p2pResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error:", e);

            p2pResponse.setResult(Status.INTERNAL_ERROR);
            p2pResponse.setMessage(e.getLocalizedMessage());
        }

        return p2pResponse;
    }
}
