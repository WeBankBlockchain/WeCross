package com.webank.wecross.test.stub.bcos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.stub.bcos.BCOSTransactionProof;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.junit.Assert;
import org.junit.Test;

public class BCOSTransactionProofTest {
    public static class ProofResult {
        public String index;
        public String leaf;
        public String proof;
    }

    private ProofResult loadExpectedProof() throws Exception {
        String path =
                BCOSTransactionProofTest.class
                        .getClassLoader()
                        .getResource("data/mock_transaction_proof.json")
                        .getPath();
        File file = new File(path);
        String content = FileUtils.readFileToString(file, "UTF-8");

        ProofResult expectedProof =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(content, new TypeReference<ProofResult>() {});

        return expectedProof;
    }

    @Test
    public void test() throws Exception {
        Transaction tx = MockBCOSTransactionFactory.newTransaction("data/mock_transaction.json");

        BCOSTransactionProof proof = new BCOSTransactionProof(tx);

        System.out.println(proof.getLeaf());
        System.out.println(proof.getProof());

        ProofResult expected = loadExpectedProof();

        Assert.assertEquals(expected.index, proof.getIndex());
        Assert.assertEquals(expected.leaf, proof.getLeaf());
        Assert.assertTrue(proof.verifyLeaf(expected.leaf));
        Assert.assertEquals(expected.proof, proof.getProof());
    }
}
