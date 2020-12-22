package com.webank.wecross.test.rpc;

import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.network.rpc.URIHandlerDispatcher;
import com.webank.wecross.network.rpc.handler.URIHandler;
import com.webank.wecross.network.rpc.netty.URIMethod;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.restserver.Versions;
import com.webank.wecross.routine.RoutineManager;
import io.netty.handler.codec.http.FullHttpResponse;
import java.io.File;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class URIHandlerDispatcherTest {
    @Test
    public void URIHandlerDispatcherTest() throws Exception {
        WeCrossHost host = Mockito.mock(WeCrossHost.class);
        Mockito.when(host.getAccountManager()).thenReturn(null);
        RoutineManager routineManager = Mockito.mock(RoutineManager.class);
        Mockito.when(host.getRoutineManager()).thenReturn(routineManager);
        Mockito.when(routineManager.getXaTransactionManager()).thenReturn(null);

        URIHandlerDispatcher uriHandlerDispatcher = new URIHandlerDispatcher();
        uriHandlerDispatcher.initializeRequestMapper(host);
        Assert.assertTrue(uriHandlerDispatcher.getRequestURIMapper().size() == 24);

        Assert.assertTrue(
                Objects.nonNull(
                        uriHandlerDispatcher.matchURIHandler(new URIMethod("GET", "/sys/test"))));
        Assert.assertTrue(
                Objects.nonNull(
                        uriHandlerDispatcher.matchURIHandler(new URIMethod("POST", "/sys/test"))));

        Assert.assertTrue(
                Objects.nonNull(
                        uriHandlerDispatcher.matchURIHandler(new URIMethod("GET", "/sys/state"))));
        Assert.assertTrue(
                Objects.nonNull(
                        uriHandlerDispatcher.matchURIHandler(
                                new URIMethod("GET", "/sys/supportedStubs"))));
        Assert.assertTrue(
                Objects.nonNull(
                        uriHandlerDispatcher.matchURIHandler(
                                new URIMethod("GET", "/sys/listResources"))));
        Assert.assertTrue(
                Objects.nonNull(
                        uriHandlerDispatcher.matchURIHandler(
                                new URIMethod("POST", "/sys/listResources"))));

        Assert.assertTrue(
                Objects.nonNull(
                        uriHandlerDispatcher.matchURIHandler(
                                new URIMethod("POST", "/resource/b/c/d/e"))));

        Assert.assertTrue(
                Objects.nonNull(
                        uriHandlerDispatcher.matchURIHandler(
                                URIHandlerDispatcher.RESOURCE_URIMETHOD)));

        Assert.assertTrue(
                Objects.nonNull(
                        uriHandlerDispatcher.matchURIHandler(
                                new URIMethod("POST", "/resource/b/c/d/e"))));

        Assert.assertTrue(
                Objects.isNull(
                        uriHandlerDispatcher.matchURIHandler(new URIMethod("GET", "/other"))));
        Assert.assertTrue(
                Objects.isNull(
                        uriHandlerDispatcher.matchURIHandler(new URIMethod("POST", "/other"))));
    }

    @Test
    public void TestURIHandlerTest() throws Exception {
        WeCrossHost host = Mockito.mock(WeCrossHost.class);
        Mockito.when(host.getAccountManager()).thenReturn(null);
        RoutineManager routineManager = Mockito.mock(RoutineManager.class);
        Mockito.when(host.getRoutineManager()).thenReturn(routineManager);
        Mockito.when(routineManager.getXaTransactionManager()).thenReturn(null);

        URIHandlerDispatcher uriHandlerDispatcher = new URIHandlerDispatcher();
        uriHandlerDispatcher.initializeRequestMapper(host);

        URIHandler uriHandler =
                uriHandlerDispatcher.matchURIHandler(new URIMethod("GET", "/sys/test"));

        final RestResponse[] restResp = {null};
        uriHandler.handle(
                null,
                null,
                null,
                null,
                new URIHandler.Callback() {
                    @Override
                    public void onResponse(String restResponse) {
                        return;
                    }

                    @Override
                    public void onResponse(FullHttpResponse fullHttpResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(File restResponse) {
                        return;
                    }

                    @Override
                    public void onResponse(RestResponse restResponse) {
                        restResp[0] = restResponse;
                    }
                });

        Assert.assertEquals(restResp[0].getData(), "OK!");
        Assert.assertEquals(restResp[0].getVersion(), Versions.currentVersion);
        Assert.assertEquals(restResp[0].getErrorCode().intValue(), NetworkQueryStatus.SUCCESS);
        Assert.assertEquals(
                restResp[0].getMessage(),
                NetworkQueryStatus.getStatusMessage(restResp[0].getErrorCode().intValue()));
    }
}
