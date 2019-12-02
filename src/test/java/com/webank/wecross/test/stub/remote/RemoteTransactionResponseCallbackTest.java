package com.webank.wecross.test.stub.remote;

import com.webank.wecross.p2p.engine.P2PResponse;
import com.webank.wecross.stub.bcos.BCOSTransactionResponse;
import com.webank.wecross.stub.remote.RemoteTransactionResponseCallback;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class RemoteTransactionResponseCallbackTest {
    @Test
    public void parseContentTest() throws Exception {
        String path =
                RemoteTransactionResponseCallbackTest.class
                        .getClassLoader()
                        .getResource("data/p2p_response_bcos_transaction_response_with_proof.json")
                        .getPath();
        File file = new File(path);
        String content = FileUtils.readFileToString(file);

        RemoteTransactionResponseCallback callback = new RemoteTransactionResponseCallback();

        P2PResponse<Object> response = callback.parseContent(content);

        Assert.assertTrue(response.getResult().equals(0));

        BCOSTransactionResponse bcosTransactionResponse =
                (BCOSTransactionResponse) response.getData();
        Assert.assertTrue(bcosTransactionResponse.getErrorCode().equals(0));
        Assert.assertNotEquals(0, bcosTransactionResponse.getProofs().length);
        Assert.assertTrue(bcosTransactionResponse.verify());
    }
}
