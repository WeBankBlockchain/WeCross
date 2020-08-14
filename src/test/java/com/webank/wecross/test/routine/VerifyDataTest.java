package com.webank.wecross.test.routine;

import com.webank.wecross.routine.htlc.VerifyData;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import org.junit.Assert;
import org.junit.Test;

public class VerifyDataTest {
    @Test
    public void equalsTest() throws Exception {
        VerifyData verifyData =
                new VerifyData(
                        100,
                        "0x",
                        "test",
                        new String[] {"hello", "world"},
                        new String[] {"hello", "world"});
        TransactionRequest request =
                new TransactionRequest("test", new String[] {"hello", "world"});
        TransactionResponse response = new TransactionResponse();
        response.setResult(new String[] {"hello", "world"});
        VerifiedTransaction verifiedTransaction =
                new VerifiedTransaction(
                        100, "0x", Path.decode("test.test.test"), "0x", request, response);
        Assert.assertEquals(true, verifyData.verify(verifiedTransaction));

        VerifiedTransaction unVerifiedTransaction =
                new VerifiedTransaction(
                        1000, null, Path.decode("test.test.test"), "0xx", request, response);
        Assert.assertEquals(false, verifyData.verify(unVerifiedTransaction));
    }
}
