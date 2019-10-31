package com.webank.wecross.p2p.engine.restful;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.peer.PeerInfoMessageData;
import com.webank.wecross.peer.PeerSeqMessageData;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import javax.servlet.http.HttpServletRequest;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("p2p")
public class RestfulP2PService {

    @javax.annotation.Resource private WeCrossHost host;

    private Logger logger = LoggerFactory.getLogger(RestfulP2PService.class);
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    @RequestMapping(value = "/{method}", method = RequestMethod.POST)
    public P2PHttpResponse<Object> handlePeer(
            @PathVariable("method") String method,
            @RequestBody String p2pRequestString,
            HttpServletRequest request) {

        P2PHttpResponse<Object> response = new P2PHttpResponse<Object>();
        response.setVersion("0.2");
        response.setResult(Status.SUCCESS);

        logger.debug("request string: {}", p2pRequestString);

        try {
            switch (method) {
                case "requestSeq":
                    {
                        logger.debug("request method: " + method);
                        P2PMessage<Object> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<Object>>() {});

                        p2pRequest.checkRestRequest(method);
                        PeerSeqMessageData data =
                                (PeerSeqMessageData) host.onRestfulPeerMessage(method, p2pRequest);

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
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<Object>>() {});

                        p2pRequest.checkRestRequest(method);

                        PeerInfoMessageData data =
                                (PeerInfoMessageData) host.onRestfulPeerMessage(method, p2pRequest);

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
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<PeerSeqMessageData>>() {});

                        p2pRequest.checkRestRequest(method);

                        host.onRestfulPeerMessage(method, p2pRequest);

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
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<PeerInfoMessageData>>() {});

                        p2pRequest.checkRestRequest(method);

                        host.onRestfulPeerMessage(method, p2pRequest);

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
                                objectMapper.readValue(
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

    @RequestMapping(
            value = {"/{network}/{stub}/{resource}/{method}"},
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
        p2pResponse.setVersion("0.2");
        p2pResponse.setResult(Status.SUCCESS);

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
                        P2PMessage<GetDataRequest> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<GetDataRequest>>() {});

                        p2pRequest.checkRestRequest(method);

                        GetDataRequest getDataRequest = p2pRequest.getData();
                        GetDataResponse getDataResponse = resourceObj.getData(getDataRequest);

                        p2pResponse.setData(getDataResponse);
                        p2pResponse.setSeq(p2pRequest.getSeq());
                        break;
                    }
                case "setData":
                    {
                        P2PMessage<SetDataRequest> p2pRequest =
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<SetDataRequest>>() {});

                        p2pRequest.checkRestRequest(method);

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
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<TransactionRequest>>() {});

                        p2pRequest.checkRestRequest(method);

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
                                objectMapper.readValue(
                                        p2pRequestString,
                                        new TypeReference<P2PMessage<TransactionRequest>>() {});

                        p2pRequest.checkRestRequest(method);

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
                                objectMapper.readValue(
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
