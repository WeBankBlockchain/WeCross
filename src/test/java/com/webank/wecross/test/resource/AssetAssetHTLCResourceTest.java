package com.webank.wecross.test.resource;

/*
import com.webank.wecross.common.ResourceQueryStatus;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Path;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.routine.htlc.AssetHTLCResource;
import com.webank.wecross.stub.bcos.BCOSContractResource;
import org.junit.Assert;
import org.junit.Test;

public class AssetAssetHTLCResourceTest {
    @Test
    public void handleRequestTest() throws Exception {
        BCOSContractResource bcosContractResource = new BCOSContractResource();
        // bcosContractResource.setPath(Path.decode("a.b.c"));
        AssetHTLCResource assetHtlcResource = new AssetHTLCResource(bcosContractResource);
        TransactionRequest request = new TransactionRequest();

        try {
            request.setMethod("getSecret");
            request.setFromP2P(true);
            assetHtlcResource.handleCallRequest(request);
        } catch (WeCrossException e) {
            Assert.assertEquals(
                    java.util.Optional.of(ResourceQueryStatus.ASSET_HTLC_NO_PERMISSION),
                    java.util.Optional.of(e.getErrorCode()));
        }

        try {
            request.setMethod("unlock");
            assetHtlcResource.handleSendTransactionRequest(request);
        } catch (WeCrossException e) {
            Assert.assertEquals(
                    java.util.Optional.of(ResourceQueryStatus.ASSET_HTLC_REQUEST_ERROR),
                    java.util.Optional.of(e.getErrorCode()));
        }

        try {
            request.setMethod("unlock");
            request.setArgs(new Object[] {"a", "b", "c"});
            assetHtlcResource.handleSendTransactionRequest(request);
        } catch (WeCrossException e) {
            Assert.assertEquals(
                    java.util.Optional.of(ResourceQueryStatus.ASSET_HTLC_VERIFY_LOCK_ERROR),
                    java.util.Optional.of(e.getErrorCode()));
        }

        try {
            request.setMethod("lock");
            TransactionRequest newRequest = assetHtlcResource.handleSendTransactionRequest(request);
            Assert.assertEquals(request, newRequest);
        } catch (WeCrossException e) {
            Assert.fail();
        }
    }
}
*/
