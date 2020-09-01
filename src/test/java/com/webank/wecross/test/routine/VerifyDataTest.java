package com.webank.wecross.test.routine;

import com.webank.wecross.routine.htlc.VerifyData;
import com.webank.wecross.stub.Transaction;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
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
        Transaction verifiedTransaction = new Transaction(100, "0x", request, response);
        Assert.assertEquals(true, verifyData.verify(verifiedTransaction));

        Transaction unVerifiedTransaction = new Transaction(1000, "0xx", request, response);
        Assert.assertEquals(false, verifyData.verify(unVerifiedTransaction));
    }
}
