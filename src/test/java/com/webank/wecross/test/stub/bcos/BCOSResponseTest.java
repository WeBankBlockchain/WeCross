package com.webank.wecross.test.stub.bcos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.proof.ProofConfig;
import com.webank.wecross.stub.bcos.BCOSResponse;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.junit.Assert;
import org.junit.Test;

public class BCOSResponseTest {
    @Test
    public void verifyTest() throws Exception {
        String path = BCOSResponseTest.class.getClassLoader().getResource("data/transaction_response_with_proof.json").getPath();
        System.out.println(path);
        File file = new File(path);

        String content = FileUtils.readFileToString(file, "UTF-8");

        BCOSResponse response =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(content, new TypeReference<BCOSResponse>() {});

        Assert.assertTrue(ProofConfig.supportSPV(response.getType()));

        Assert.assertTrue(response.verify());
    }
}
