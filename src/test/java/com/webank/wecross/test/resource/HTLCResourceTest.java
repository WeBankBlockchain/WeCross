package com.webank.wecross.test.resource;

import com.webank.wecross.common.ResourceQueryStatus;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.htlc.HTLCResource;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import org.junit.Assert;
import org.junit.Test;

public class HTLCResourceTest {
    @Test
    public void handleRequestTest() throws Exception {
        Resource resource = new Resource();
        // bcosContractResource.setPath(Path.decode("a.b.c"));
        HTLCResource assetHtlcResource = new HTLCResource(resource);
        TransactionRequest request = new TransactionRequest();
        TransactionContext<TransactionRequest> context =
                new TransactionContext<TransactionRequest>(request, null, null);
        //        try {
        //            request.setMethod("getSecret");
        //            request.setFromP2P(true);
        //            context.setData(request);
        //            assetHtlcResource.handleCallRequest(context);
        //        } catch (WeCrossException e) {
        //            Assert.assertEquals(
        //                    java.util.Optional.of(ResourceQueryStatus.ASSET_HTLC_NO_PERMISSION),
        //                    java.util.Optional.of(e.getErrorCode()));
        //        }

        try {
            request.setMethod("unlock");
            context.setData(request);
            assetHtlcResource.handleSendTransactionRequest(context);
        } catch (WeCrossException e) {
            Assert.assertEquals(
                    java.util.Optional.of(ResourceQueryStatus.ASSET_HTLC_REQUEST_ERROR),
                    java.util.Optional.of(e.getErrorCode()));
        }

        try {
            request.setMethod("unlock");
            request.setArgs(new String[] {"a", "b", "c"});
            context.setData(request);
            assetHtlcResource.handleSendTransactionRequest(context);
        } catch (WeCrossException e) {
            Assert.assertEquals(
                    java.util.Optional.of(ResourceQueryStatus.ASSET_HTLC_VERIFY_LOCK_ERROR),
                    java.util.Optional.of(e.getErrorCode()));
        }

        try {
            request.setMethod("lock");
            context.setData(request);
            TransactionContext<TransactionRequest> newContext =
                    assetHtlcResource.handleSendTransactionRequest(context);
            Assert.assertEquals(context, newContext);
        } catch (WeCrossException e) {
            Assert.fail();
        }
    }
}
