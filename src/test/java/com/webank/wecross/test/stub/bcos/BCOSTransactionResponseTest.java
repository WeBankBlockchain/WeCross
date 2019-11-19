package com.webank.wecross.test.stub.bcos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.proof.ProofConfig;
import com.webank.wecross.stub.bcos.BCOSTransactionResponse;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.junit.Assert;
import org.junit.Test;

public class BCOSTransactionResponseTest {
    @Test
    public void verifyTest() throws Exception {
        String path =
                BCOSTransactionResponseTest.class
                        .getClassLoader()
                        .getResource("data/transaction_response_with_proof.json")
                        .getPath();
        System.out.println(path);
        File file = new File(path);

        String content = FileUtils.readFileToString(file, "UTF-8");

        BCOSTransactionResponse response =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(content, new TypeReference<BCOSTransactionResponse>() {});

        Assert.assertTrue(ProofConfig.supportSPV(response.getType()));

        Assert.assertTrue(response.verify());
    }
}
