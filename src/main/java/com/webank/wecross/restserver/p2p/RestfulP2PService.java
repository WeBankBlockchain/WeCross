package com.webank.wecross.restserver.p2p;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.peer.PeerSeqMessageData;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@RestController
@SpringBootApplication
public class RestfulP2PService {
    private Logger logger = LoggerFactory.getLogger(RestfulP2PService.class);
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    @RequestMapping(value = "/p2p/peer/{method}", method = RequestMethod.POST)
    public P2PHttpResponse<Object> handlePeer(
            @PathVariable("method") String method, @RequestBody String restRequestString) {

        P2PHttpResponse<Object> response = new P2PHttpResponse<Object>();
        response.setVersion("0.1");
        response.setResult(0);

        logger.info("request string: {}", restRequestString);

        try {

            switch (method) {
                case "requestSeq":
                    {
                        logger.info("request method: peer/" + method);
                        P2PMessage<Object> restRequest =
                                objectMapper.readValue(
                                        restRequestString,
                                        new TypeReference<P2PMessage<Object>>() {});

                        PeerSeqMessageData data = new PeerSeqMessageData();

                        // --------- mock handle seq request here
                        data.setDataSeq(666);
                        // --------- end

                        response.setSeq(restRequest.getSeq());
                        response.setResult(0);
                        response.setMessage("request peer/" + method + " method success");
                        response.setData(data);
                        break;
                    }

                case "request":
                    {
                        logger.info("request method: peer/" + method);
                        response.setMessage("request peer/" + method + " method success");
                        break;
                    }

                case "peer":
                    {
                        logger.info("request method: peer/" + method);
                        response.setMessage("request peer/" + method + " method success");
                        break;
                    }
                default:
                    {
                        response.setResult(-1);
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

    @RequestMapping(value = "/p2p/stub/{method}", method = RequestMethod.POST)
    public P2PHttpResponse<Object> handleStub(
            @PathVariable("method") String method, @RequestBody String restRequestString) {

        P2PHttpResponse<Object> response = new P2PHttpResponse<Object>();
        response.setVersion("0.1");
        response.setResult(0);

        logger.info("request string: {}", restRequestString);

        try {

            switch (method) {
                case "request":
                    {
                        logger.info("request method: stub/" + method);
                        response.setMessage("request stub/" + method + " method success");
                        break;
                    }

                case "response":
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

    @RequestMapping(value = "/p2p/remoteCall", method = RequestMethod.POST)
    public P2PHttpResponse<Object> handleRemoteCall(
            @PathVariable("method") String method, @RequestBody String restRequestString) {

        P2PHttpResponse<Object> response = new P2PHttpResponse<Object>();
        response.setVersion("0.1");
        response.setResult(0);

        logger.info("request string: {}", restRequestString);

        try {

            logger.info("request method: remoteCall");
            response.setMessage("request remoteCall method success");

        } catch (Exception e) {
            logger.warn("Process request error:", e);

            response.setResult(-1);
            response.setMessage(e.getLocalizedMessage());
        }

        return response;
    }
}
