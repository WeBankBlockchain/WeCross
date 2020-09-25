package com.webank.wecross.network.rpc;

import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.network.rpc.handler.ListResourcesURIHandler;
import com.webank.wecross.network.rpc.handler.ListStubsURIHandler;
import com.webank.wecross.network.rpc.handler.ResourceURIHandler;
import com.webank.wecross.network.rpc.handler.StateURIHandler;
import com.webank.wecross.network.rpc.handler.TestURIHandler;
import com.webank.wecross.network.rpc.handler.URIHandler;
import com.webank.wecross.network.rpc.handler.XATransactionHandler;
import com.webank.wecross.network.rpc.netty.URIMethod;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URIHandlerDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(URIHandlerDispatcher.class);

    public static final URIMethod RESOURCE_URIMETHOD =
            new URIMethod("POST", "/network/stub/resource/method");

    private Map<URIMethod, URIHandler> requestURIMapper = new HashMap<>();

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

        TestURIHandler testURIHandler = new TestURIHandler();
        registerURIHandler(new URIMethod("GET", "/test"), testURIHandler);
        registerURIHandler(new URIMethod("POST", "/test"), testURIHandler);

        StateURIHandler stateURIHandler = new StateURIHandler(host);
        registerURIHandler(new URIMethod("GET", "/state"), stateURIHandler);
        registerURIHandler(new URIMethod("POST", "/state"), stateURIHandler);

        ListStubsURIHandler listStubsURIHandler = new ListStubsURIHandler(host);
        registerURIHandler(new URIMethod("GET", "/supportedStubs"), listStubsURIHandler);
        registerURIHandler(new URIMethod("POST", "/supportedStubs"), listStubsURIHandler);

        ListResourcesURIHandler listResourcesURIHandler = new ListResourcesURIHandler(host);
        registerURIHandler(new URIMethod("GET", "/listResources"), listResourcesURIHandler);
        registerURIHandler(new URIMethod("POST", "/listResources"), listResourcesURIHandler);

        /*
                ListAccountsURIHandler listAccountsURIHandler = new ListAccountsURIHandler(host);
                listAccountsURIHandler.setUserContext(userContext);
                registerURIHandler(new URIMethod("GET", "/listAccounts"), listAccountsURIHandler);
                registerURIHandler(new URIMethod("POST", "/listAccounts"), listAccountsURIHandler);
        */
        XATransactionHandler xaTransactionHandler = new XATransactionHandler();
        xaTransactionHandler.setXaTransactionManager(
                host.getRoutineManager().getXaTransactionManager());
        xaTransactionHandler.setHost(host);
        registerURIHandler(new URIMethod("POST", "/startTransaction"), xaTransactionHandler);
        registerURIHandler(new URIMethod("POST", "/commitTransaction"), xaTransactionHandler);
        registerURIHandler(new URIMethod("POST", "/rollbackTransaction"), xaTransactionHandler);
        registerURIHandler(new URIMethod("POST", "/getTransactionInfo"), xaTransactionHandler);
        registerURIHandler(new URIMethod("POST", "/getTransactionIDs"), xaTransactionHandler);

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
        URIHandler uriHandler = requestURIMapper.get(uriMethod);
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
}
