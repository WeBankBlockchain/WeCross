package com.webank.wecross.test.stub.bcos;

import com.webank.wecross.stub.bcos.BCOSTransactionProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.junit.Assert;
import org.junit.Test;

public class BCOSTransactionProofTest {

    @Test
    public void test() throws Exception {
        Transaction tx = MockBCOSTransactionFactory.newTransaction("data/mock_transaction.json");

        BCOSTransactionProof proof = new BCOSTransactionProof(tx);

        System.out.println(proof.getLeaf());
        System.out.println(proof.getProof());

        Assert.assertEquals("0x1", proof.getIndex());
        Assert.assertEquals(
                "0x2536deb90a9dd076f532da130c16faa6bc195c734b8c6550db66b80241213c03",
                proof.getLeaf());
        Assert.assertTrue(
                proof.verifyLeaf(
                        "0x2536deb90a9dd076f532da130c16faa6bc195c734b8c6550db66b80241213c03"));
        Assert.assertEquals(
                "0xb3bf5ed6dfd4251c4a866d81275f0f31394c6e03d727b7c7cee5d95855f72d52",
                proof.getProof());
    }
}
