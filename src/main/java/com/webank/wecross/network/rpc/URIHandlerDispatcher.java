package com.webank.wecross.network.rpc;

import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.network.UriDecoder;
import com.webank.wecross.network.rpc.handler.*;
import com.webank.wecross.network.rpc.netty.URIMethod;
import com.webank.wecross.network.rpc.web.WebService;
import com.webank.wecross.restserver.fetcher.ResourceFetcher;
import com.webank.wecross.restserver.fetcher.TransactionFetcher;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URIHandlerDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(URIHandlerDispatcher.class);

    public static final URIMethod RESOURCE_URIMETHOD =
            new URIMethod("POST", "/resource/network/stub/resource/method");

    private Map<URIMethod, URIHandler> requestURIMapper = new HashMap<>();

    private WebService webService;

    public Map<URIMethod, URIHandler> getRequestURIMapper() {
        return requestURIMapper;
    }

    public void setRequestURIMapper(Map<URIMethod, URIHandler> requestURIMapper) {
        this.requestURIMapper = requestURIMapper;
    }

    /**
     * initialize uri request mapper
     *
     * @param host
     */
    public void initializeRequestMapper(WeCrossHost host) {

        // Others
        TestURIHandler testURIHandler = new TestURIHandler();
        registerURIHandler(new URIMethod("GET", "/sys/test"), testURIHandler);
        registerURIHandler(new URIMethod("POST", "/sys/test"), testURIHandler);

        StateURIHandler stateURIHandler = new StateURIHandler(host);
        registerURIHandler(new URIMethod("GET", "/sys/state"), stateURIHandler);

        ListStubsURIHandler listStubsURIHandler = new ListStubsURIHandler(host);
        registerURIHandler(new URIMethod("GET", "/sys/supportedStubs"), listStubsURIHandler);

        ResourceFetcher resourceFetcher = new ResourceFetcher(host.getZoneManager());
        ListResourcesURIHandler listResourcesURIHandler =
                new ListResourcesURIHandler(resourceFetcher);
        registerURIHandler(new URIMethod("GET", "/sys/listResources"), listResourcesURIHandler);
        registerURIHandler(new URIMethod("POST", "/sys/listResources"), listResourcesURIHandler);

        TransactionFetcher transactionFetcher =
                new TransactionFetcher(host.getZoneManager(), host.getAccountManager());
        TransactionURIHandler transactionURIHandler = new TransactionURIHandler(transactionFetcher);
        registerURIHandler(new URIMethod("GET", "/trans/getTransaction"), transactionURIHandler);
        registerURIHandler(new URIMethod("GET", "/trans/listTransactions"), transactionURIHandler);

        ConnectionURIHandler connectionURIHandler = new ConnectionURIHandler();
        connectionURIHandler.setP2PService(host.getP2PService());
        connectionURIHandler.setPeerManager(host.getPeerManager());
        connectionURIHandler.setZoneManager(host.getZoneManager());
        registerURIHandler(new URIMethod("GET", "/conn/listChains"), connectionURIHandler);
        registerURIHandler(new URIMethod("POST", "/conn/addChain"), connectionURIHandler);
        registerURIHandler(new URIMethod("POST", "/conn/updateChain"), connectionURIHandler);
        registerURIHandler(new URIMethod("POST", "/conn/removeChain"), connectionURIHandler);
        registerURIHandler(new URIMethod("GET", "/conn/listPeers"), connectionURIHandler);
        registerURIHandler(new URIMethod("POST", "/conn/addPeer"), connectionURIHandler);
        registerURIHandler(new URIMethod("POST", "/conn/removePeer"), connectionURIHandler);

        XATransactionHandler xaTransactionHandler = new XATransactionHandler();
        xaTransactionHandler.setXaTransactionManager(
                host.getRoutineManager().getXaTransactionManager());
        xaTransactionHandler.setHost(host);
        registerURIHandler(new URIMethod("POST", "/xa/startXATransaction"), xaTransactionHandler);
        registerURIHandler(new URIMethod("POST", "/xa/commitXATransaction"), xaTransactionHandler);
        registerURIHandler(
                new URIMethod("POST", "/xa/rollbackXATransaction"), xaTransactionHandler);
        registerURIHandler(new URIMethod("POST", "/xa/getXATransaction"), xaTransactionHandler);
        registerURIHandler(new URIMethod("POST", "/xa/listXATransactions"), xaTransactionHandler);

        ResourceURIHandler resourceURIHandler = new ResourceURIHandler(host);
        registerURIHandler(RESOURCE_URIMETHOD, resourceURIHandler);

        logger.info(" initialize size: {}", requestURIMapper.size());
        logger.info(" URIMethod: {} ", requestURIMapper.keySet());
    }

    /**
     * register uri handler by method and uri
     *
     * @param path method and uri
     * @param uriHandler
     */
    public void registerURIHandler(URIMethod path, URIHandler uriHandler) {
        requestURIMapper.putIfAbsent(path, uriHandler);
        logger.info(" path: {}, handler: {} ", path, uriHandler.getClass().getName());
    }

    /**
     * @param uriMethod
     * @return
     */
    public URIHandler matchURIHandler(URIMethod uriMethod) {
        URIHandler uriHandler = matchWebURIHandler(uriMethod);
        if (!Objects.isNull(uriHandler)) {
            return uriHandler;
        }

        UriDecoder uriDecoder = new UriDecoder(uriMethod.getUri());
        uriHandler =
                requestURIMapper.get(
                        new URIMethod(
                                uriMethod.getMethod(), uriDecoder.getURIWithoutQueryString()));

        if (Objects.isNull(uriHandler) && uriMethod.isResourceURI()) {
            uriHandler = requestURIMapper.get(RESOURCE_URIMETHOD);
        }

        if (Objects.isNull(uriHandler)) {
            logger.warn(
                    " Not found, method: {}, uri: {} ", uriMethod.getMethod(), uriMethod.getUri());
        } else {
            logger.trace(" path: {}, handler: {}", uriMethod, uriHandler.getClass().getName());
        }

        return uriHandler;
    }

    public URIHandler matchWebURIHandler(URIMethod uriMethod) {
        if (uriMethod.getUri().startsWith("/s/")) {
            // /s/index.html
            return webService.getHandler();
        }
        return null;
    }

    public void setWebService(WebService webService) {
        this.webService = webService;
    }
}
