package com.webank.wecross.test.stub.bcos;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.junit.Assert;

public class MockBCOSTransactionFactory {

    public static Transaction newTransaction(String txJsonFile) {
        try {

            String path =
                    MockBCOSTransactionFactory.class
                            .getClassLoader()
                            .getResource(txJsonFile)
                            .getPath();
            File file = new File(path);
            String content = FileUtils.readFileToString(file, "UTF-8");

            Transaction tx =
                    ObjectMapperFactory.getObjectMapper()
                            .readValue(content, new TypeReference<Transaction>() {});

            System.out.println(tx.toString());

            return tx;
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
        return null;
    }
}
