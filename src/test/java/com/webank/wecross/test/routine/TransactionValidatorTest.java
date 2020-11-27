package com.webank.wecross.test.routine;

import com.webank.wecross.routine.TransactionValidator;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Transaction;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import org.junit.Assert;
import org.junit.Test;

public class TransactionValidatorTest {
    @Test
    public void equalsTest() throws Exception {
        TransactionValidator transactionValidator =
                new TransactionValidator(
                        100,
                        "0x",
                        "test",
                        new String[] {"hello", "world"},
                        new String[] {"hello", "world"});

        transactionValidator.setPath(Path.decode("a.b.c"));
        TransactionRequest request =
                new TransactionRequest("test", new String[] {"hello", "world"});
        TransactionResponse response = new TransactionResponse();
        response.setResult(new String[] {"hello", "world"});
        response.setHash("0x");
        response.setBlockNumber(100);
        Transaction transaction = new Transaction(request, response);
        Assert.assertEquals(true, transactionValidator.verify(transaction));

        response.setHash("0xx");
        response.setBlockNumber(1000);
        Transaction transaction1 = new Transaction(request, response);
        Assert.assertEquals(false, transactionValidator.verify(transaction1));
    }
}
