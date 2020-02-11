package com.webank.wecross.utils;

import java.math.BigInteger;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.Sign;
import org.fisco.bcos.web3j.crypto.SignedExtendedRawTransaction;
import org.fisco.bcos.web3j.rlp.RlpDecoder;
import org.fisco.bcos.web3j.rlp.RlpList;
import org.fisco.bcos.web3j.rlp.RlpString;
import org.fisco.bcos.web3j.utils.Numeric;

// There is a bug in sdk, this is the fix code, waiting sdk to fix
public class ExtendedTransactionDecoderV2 {
    public static ExtendedRawTransaction decode(byte[] transaction) {
        RlpList rlpList = RlpDecoder.decode(transaction);
        RlpList values = (RlpList) rlpList.getValues().get(0);
        BigInteger randomid = ((RlpString) values.getValues().get(0)).asPositiveBigInteger();
        BigInteger gasPrice = ((RlpString) values.getValues().get(1)).asPositiveBigInteger();
        BigInteger gasLimit = ((RlpString) values.getValues().get(2)).asPositiveBigInteger();
        BigInteger blockLimit = ((RlpString) values.getValues().get(3)).asPositiveBigInteger();
        String to = ((RlpString) values.getValues().get(4)).asString();
        BigInteger value = ((RlpString) values.getValues().get(5)).asPositiveBigInteger();
        String data = ((RlpString) values.getValues().get(6)).asString();

        // add extra data
        BigInteger chainId = ((RlpString) values.getValues().get(7)).asPositiveBigInteger();
        BigInteger groupId = ((RlpString) values.getValues().get(8)).asPositiveBigInteger();
        String extraData = ((RlpString) values.getValues().get(9)).asString();
        if (values.getValues().size() > 10) { // The original bug is 9 here
            byte v = ((RlpString) values.getValues().get(10)).getBytes()[0];
            byte[] r =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) values.getValues().get(11)).getBytes()),
                            32);
            byte[] s =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) values.getValues().get(12)).getBytes()),
                            32);
            Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
            return new SignedExtendedRawTransaction(
                    randomid,
                    gasPrice,
                    gasLimit,
                    blockLimit,
                    to,
                    value,
                    data,
                    chainId,
                    groupId,
                    extraData,
                    signatureData);
        } else {
            return ExtendedRawTransaction.createTransaction(
                    randomid,
                    gasPrice,
                    gasLimit,
                    blockLimit,
                    to,
                    value,
                    data,
                    chainId,
                    groupId,
                    extraData);
        }
    }
}
