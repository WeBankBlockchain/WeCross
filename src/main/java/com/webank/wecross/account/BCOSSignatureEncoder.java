package com.webank.wecross.account;

import java.util.Arrays;
import org.fisco.bcos.web3j.crypto.Sign;

public class BCOSSignatureEncoder {
    /*
     *   encodedBytes = v(byte), r(byte[32]), s(byte[32]), pub(byte[64])
     *                 [0],     [1 .. 32],   [33 .. 64],  [65 .. 128]
     * */

    // decode
    public Sign.SignatureData decode(byte[] encodedBytes) throws Exception {
        if (encodedBytes.length != 129) {
            throw new Exception("Illegal signature bytes");
        }

        return new Sign.SignatureData(
                encodedBytes[0],
                Arrays.copyOfRange(encodedBytes, 1, 33),
                Arrays.copyOfRange(encodedBytes, 33, 65),
                Arrays.copyOfRange(encodedBytes, 65, 129));
    }

    // encode
    public byte[] encode(Sign.SignatureData signatureData) throws Exception {
        byte[] encodedBytes = new byte[129];
        byte v = signatureData.getV();
        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        byte[] pub = signatureData.getPub();

        encodedBytes[0] = v;
        System.arraycopy(r, 0, encodedBytes, 1, r.length);
        System.arraycopy(s, 0, encodedBytes, 33, s.length);
        if (pub != null) {
            System.arraycopy(pub, 0, encodedBytes, 65, pub.length);
        }

        return encodedBytes;
    }
}
