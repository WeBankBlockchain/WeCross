package com.webank.wecross.proof;

import java.math.BigInteger;
import org.fisco.bcos.web3j.crypto.Hash;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.rlp.RlpEncoder;
import org.fisco.bcos.web3j.rlp.RlpString;
import org.fisco.bcos.web3j.utils.Numeric;

public class BCOSTransactionProof extends LeafProof {
    private String index;

    public BCOSTransactionProof(Transaction transaction) {
        index = transaction.getTransactionIndexRaw();
        leaf = transaction.getHash();
        BigInteger indexValue = Numeric.toBigInt(index);
        byte[] byteIndex = RlpEncoder.encode(RlpString.create(indexValue));
        proof = Hash.sha3(Numeric.toHexString(byteIndex) + leaf.substring(2));
    }

    @Override
    public boolean verifyLeaf(String leaf) {
        return getLeaf().equals(leaf);
    }

    public String getIndex() {
        return index;
    }
}
