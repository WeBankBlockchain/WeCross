package com.webank.wecross.test.stub.bcos;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.junit.Assert;

public class MockBCOSReceiptFactory {
    public static TransactionReceipt newReceipt(String receiptJsonFile) {
        try {

            String path =
                    MockBCOSTransactionFactory.class
                            .getClassLoader()
                            .getResource(receiptJsonFile)
                            .getPath();
            File file = new File(path);
            String content = FileUtils.readFileToString(file, "UTF-8");

            TransactionReceipt receipt =
                    ObjectMapperFactory.getObjectMapper()
                            .readValue(content, new TypeReference<TransactionReceipt>() {});

            System.out.println(receipt.toString());

            return receipt;
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
        return null;
    }
}
