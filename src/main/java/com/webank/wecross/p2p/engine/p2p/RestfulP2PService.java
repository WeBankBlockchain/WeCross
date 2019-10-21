package com.webank.wecross.p2p.engine.p2p;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.peer.PeerInfoMessageData;
import com.webank.wecross.p2p.peer.PeerRequestPeerInfoMessageData;
import com.webank.wecross.p2p.peer.PeerRequestSeqMessageData;
import com.webank.wecross.p2p.peer.PeerSeqMessageData;
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

        logger.info("request string: {}", p2pRequestString);

        try {

            switch (method) {
                case "requestSeq":
                    {
                        logger.info("request method: peer/" + method);
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
                        logger.info("request method: peer/" + method);
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
                        logger.info("request method: peer/" + method);
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
                        logger.info("request method: peer/" + method);
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
                        logger.info("request method: peer/" + method);
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

        logger.info("Response " + response);
        return response;
    }

    @RequestMapping(value = "/stub/{method}", method = RequestMethod.POST)
    public P2PHttpResponse<Object> handleStub(
            @PathVariable("method") String method, @RequestBody String p2pRequestString) {

        P2PHttpResponse<Object> response = new P2PHttpResponse<Object>();
        response.setVersion("0.1");
        response.setResult(0);

        logger.info("request string: {}", p2pRequestString);

        try {

            switch (method) {
                case "requestChainState":
                    {
                        logger.info("request method: stub/" + method);
                        response.setMessage("request stub/" + method + " method success");
                        break;
                    }

                case "chainState":
                    {
                        logger.info("request method: stub/" + method);
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

    @RequestMapping(value = "/remote", method = RequestMethod.POST)
    public P2PHttpResponse<Object> handleRemote(
            @PathVariable("method") String method, @RequestBody String p2pRequestString) {

        P2PHttpResponse<Object> response = new P2PHttpResponse<Object>();
        response.setVersion("0.1");
        response.setResult(0);

        logger.info("request string: {}", p2pRequestString);

        try {
            switch (method) {
                case "call":
                    {
                        logger.info("request method: remote/" + method);
                        response.setMessage("request remote/" + method + " method success");
                        break;
                    }
                case "sendTransaction":
                    {
                        logger.info("request method: remote/" + method);
                        response.setMessage("request remote/" + method + " method success");
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
}
