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
        Transaction transaction = new Transaction(100, "0x", request, response);

        transaction.setXaTransactionSeq(0);
        transaction.setXaTransactionID("0");
        transaction.setResource("c");

        Assert.assertEquals(true, transactionValidator.verify(transaction));

        Transaction transaction1 = new Transaction(1000, "0xx", request, response);
        Assert.assertEquals(false, transactionValidator.verify(transaction1));
    }
}
