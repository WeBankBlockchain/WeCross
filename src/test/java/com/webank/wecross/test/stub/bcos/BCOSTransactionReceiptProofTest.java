package com.webank.wecross.test.stub.bcos;

import com.webank.wecross.stub.bcos.BCOSReceiptProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.junit.Assert;
import org.junit.Test;

public class BCOSTransactionReceiptProofTest {
    @Test
    public void test() {
        TransactionReceipt receipt = MockBCOSReceiptFactory.newReceipt("data/mock_receipt.json");

        BCOSReceiptProof proof = new BCOSReceiptProof(receipt);

        System.out.println(proof.getLeaf());
        System.out.println(proof.getProof());

        Assert.assertEquals("0x1", proof.getIndex());
        Assert.assertEquals(
                "0x187645f8724f37d063e56b6191119fffac3f82953948642139dcf5cca09b08fb",
                proof.getLeaf());
        Assert.assertTrue(
                proof.verifyLeaf(
                        "0x187645f8724f37d063e56b6191119fffac3f82953948642139dcf5cca09b08fb"));
        Assert.assertEquals(
                "0x41ea1a43a95c9ed2a1d3739cd793a34bbd50b0f9ebed3a2f2247e4a16d027120",
                proof.getProof());
    }
}
