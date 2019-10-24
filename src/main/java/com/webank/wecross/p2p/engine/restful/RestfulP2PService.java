package com.webank.wecross.p2p.engine.restful;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.peer.PeerInfoMessageData;
import com.webank.wecross.p2p.peer.PeerRequestPeerInfoMessageData;
import com.webank.wecross.p2p.peer.PeerRequestSeqMessageData;
import com.webank.wecross.p2p.peer.PeerSeqMessageData;
import com.webank.wecross.resource.*;
import com.webank.wecross.resource.request.GetDataRequest;
import com.webank.wecross.resource.request.SetDataRequest;
import com.webank.wecross.resource.request.TransactionRequest;
import com.webank.wecross.resource.response.GetDataResponse;
import com.webank.wecross.resource.response.SetDataResponse;
import com.webank.wecross.resource.response.TransactionResponse;
import com.webank.wecross.stub.remote.GetDataRequestMessageData;
import com.webank.wecross.stub.remote.SetDataRequestMessageData;
import com.webank.wecross.stub.remote.TransactionRequestMessageData;
import javax.servlet.http.HttpServletRequest;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("p2p")
public class RestfulP2PService {
    @javax.annotation.Resource private WeCrossHost host;

    private Logger logger = LoggerFactory.getLogger(RestfulP2PService.class);
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    @RequestMapping(value = "/peer/{method}", method = RequestMethod.POST)
    public P2PHttpResponse<Object> handlePeer(
            @PathVariable("method") String method,
            @RequestBody String p2pRequestString,
            HttpServletRequest request) {

        P2PHttpResponse<Object> response = new P2PHttpResponse<Object>();
        response.setVersion("0.1");
        response.setResult(0);

        logger.debug("request string: {}", p2pRequestString);

        try {

            switch (method) {
                case "requestSeq":
                    {
                        logger.debug("request method: peer/" + method);
                        P2PMessage<PeerRequestSeqMessageData> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<
                                                P2PMessage<PeerRequestSeqMessageData>>() {});

                        PeerSeqMessageData data =
                                (PeerSeqMessageData) host.onRestfulPeerMessage(method, p2pRequest);

                        response.setResult(0);
                        response.setMessage("request peer/" + method + " method success");
                        response.setSeq(p2pRequest.getSeq());
                        response.setData(data);
                        break;
                    }
                case "requestPeerInfo":
                    {
                        logger.debug("request method: peer/" + method);
                        P2PMessage<PeerRequestPeerInfoMessageData> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<
                                                P2PMessage<PeerRequestPeerInfoMessageData>>() {});

                        PeerInfoMessageData data =
                                (PeerInfoMessageData) host.onRestfulPeerMessage(method, p2pRequest);

                        response.setResult(0);
                        response.setMessage("request peer/" + method + " method success");
                        response.setSeq(p2pRequest.getSeq());
                        response.setData(data);
                        break;
                    }
                case "seq":
                    {
                        logger.debug("request method: peer/" + method);
                        P2PMessage<PeerSeqMessageData> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<PeerSeqMessageData>>() {});

                        host.onRestfulPeerMessage(method, p2pRequest);

                        response.setResult(0);
                        response.setMessage("request peer/" + method + " method success");
                        response.setSeq(p2pRequest.getSeq());
                        response.setData(null);
                        break;
                    }
                case "peerInfo":
                    {
                        logger.debug("request method: peer/" + method);
                        P2PMessage<PeerInfoMessageData> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<PeerInfoMessageData>>() {});

                        host.onRestfulPeerMessage(method, p2pRequest);

                        response.setResult(0);
                        response.setMessage("request peer/" + method + " method success");
                        response.setSeq(p2pRequest.getSeq());
                        response.setData(null);
                        break;
                    }
                default:
                    {
                        logger.debug("request method: peer/" + method);
                        P2PMessage<Object> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<Object>>() {});
                        response.setResult(-1);
                        response.setSeq(p2pRequest.getSeq());
                        response.setMessage("Unsupport method: peer/" + method);
                        break;
                    }
            }

        } catch (Exception e) {
            logger.warn("Process request error:", e);

            response.setResult(-1);
            response.setMessage(e.getLocalizedMessage());
        }

        logger.trace("Response " + response);
        return response;
    }

    @RequestMapping(value = "/stub/{method}", method = RequestMethod.POST)
    public P2PHttpResponse<Object> handleStub(
            @PathVariable("method") String method, @RequestBody String p2pRequestString) {

        P2PHttpResponse<Object> response = new P2PHttpResponse<Object>();
        response.setVersion("0.1");
        response.setResult(0);

        logger.debug("request string: {}", p2pRequestString);

        try {

            switch (method) {
                case "requestChainState":
                    {
                        logger.debug("request method: stub/" + method);
                        response.setMessage("request stub/" + method + " method success");
                        break;
                    }

                case "chainState":
                    {
                        logger.debug("request method: stub/" + method);
                        response.setMessage("request stub/" + method + " method success");
                        break;
                    }

                default:
                    {
                        response.setResult(-1);
                        response.setMessage("Unsupport method: stub/" + method);
                        break;
                    }
            }

        } catch (Exception e) {
            logger.warn("Process request error:", e);

            response.setResult(-1);
            response.setMessage(e.getLocalizedMessage());
        }

        return response;
    }

    @RequestMapping(
            value = {"/remote/{network}/{stub}/{resource}/{method}"},
            method = RequestMethod.POST)
    public P2PHttpResponse<Object> handleRemote(
            @PathVariable("network") String network,
            @PathVariable("stub") String stub,
            @PathVariable("resource") String resource,
            @PathVariable("method") String method,
            @RequestBody String p2pRequestString) {
        Path path = new Path();
        path.setNetwork(network);
        path.setChain(stub);
        path.setResource(resource);

        P2PHttpResponse<Object> p2pResponse = new P2PHttpResponse<Object>();
        p2pResponse.setVersion("0.1");
        p2pResponse.setResult(0);

        logger.debug("request string: {}", p2pRequestString);

        try {
            Resource resourceObj = host.getResource(path);
            if (resourceObj == null) {
                logger.warn("Unable to find resource: {}.{}.{}", network, stub, resource);

                throw new Exception("Resource not found");
            }

            switch (method) {
                case "getData":
                    {
                        P2PMessage<GetDataRequestMessageData> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<
                                                P2PMessage<GetDataRequestMessageData>>() {});

                        GetDataRequest getDataRequest = p2pRequest.getData().getData();
                        GetDataResponse getDataResponse = resourceObj.getData(getDataRequest);

                        p2pResponse.setData(getDataResponse);
                        break;
                    }
                case "setData":
                    {
                        P2PMessage<SetDataRequestMessageData> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<
                                                P2PMessage<SetDataRequestMessageData>>() {});

                        SetDataRequest setDataRequest =
                                (SetDataRequest) p2pRequest.getData().getData();
                        SetDataResponse setDataResponse =
                                (SetDataResponse) resourceObj.setData(setDataRequest);

                        p2pResponse.setData(setDataResponse);
                        break;
                    }
                case "call":
                    {
                        P2PMessage<TransactionRequestMessageData> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<
                                                P2PMessage<TransactionRequestMessageData>>() {});

                        TransactionRequest transactionRequest =
                                (TransactionRequest) p2pRequest.getData().getData();
                        TransactionResponse transactionResponse =
                                (TransactionResponse) resourceObj.call(transactionRequest);

                        p2pResponse.setData(transactionResponse);
                        break;
                    }
                case "sendTransaction":
                    {
                        P2PMessage<TransactionRequestMessageData> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<
                                                P2PMessage<TransactionRequestMessageData>>() {});

                        TransactionRequest transactionRequest =
                                (TransactionRequest) p2pRequest.getData().getData();
                        TransactionResponse transactionResponse =
                                (TransactionResponse)
                                        resourceObj.sendTransaction(transactionRequest);

                        p2pResponse.setData(transactionResponse);
                        break;
                    }
                default:
                    {
                        p2pResponse.setResult(-1);
                        p2pResponse.setMessage("Unsupport method: " + method);
                        break;
                    }
            }
        } catch (Exception e) {
            logger.warn("Process request error:", e);

            p2pResponse.setResult(-1);
            p2pResponse.setMessage(e.getLocalizedMessage());
        }

        return p2pResponse;
    }
}
