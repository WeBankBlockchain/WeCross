package com.webank.wecross.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.ObjectMapperFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

/*
Tools for SM2, include:
   sign
   verify
   load
   write
*/
public class SM2 {

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static class SignatureData {
        @JsonIgnore
        private static ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

        public SignatureData(byte[] sign, byte[] pub) {
            this.sign = sign;
            this.pub = pub;
        }

        private byte[] sign;
        private byte[] pub;

        public byte[] getSign() {
            return sign;
        }

        public void setSign(byte[] sign) {
            this.sign = sign;
        }

        public byte[] getPub() {
            return pub;
        }

        public void setPub(byte[] pub) {
            this.pub = pub;
        }

        public String getHexPub() {
            return Hex.encodeHexString(pub);
        }

        public byte[] toBytes() {
            return ArrayUtils.addAll(pub, sign);
        }

        public static SignatureData parseFrom(byte[] signBytes) {
            byte[] pub = Arrays.copyOf(signBytes, 91);
            byte[] sign = Arrays.copyOfRange(signBytes, 91, signBytes.length);
            return new SignatureData(sign, pub);
        }
    }

    private static final byte[] withID =
            org.bouncycastle.util.encoders.Hex.decode("31323334353637383132333435363738");

    private static final int HEX_PUBLIC_KEY_LENGTH = 60;

    /**
     * create SM2 KeyPair
     *
     * @return
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     */
    public static KeyPair newKeyPair()
            throws NoSuchProviderException, NoSuchAlgorithmException,
                    InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        keyPairGenerator.initialize(new ECGenParameterSpec("sm2p256v1"), new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * @param params
     * @return
     */
    private static java.security.spec.ECParameterSpec tryFindNamedCurveSpec(
            java.security.spec.ECParameterSpec params) {
        ECParameterSpec bcSpec =
                org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util.convertSpec(params);
        for (Object name : Collections.list(org.bouncycastle.jce.ECNamedCurveTable.getNames())) {
            org.bouncycastle.jce.spec.ECNamedCurveParameterSpec bcNamedSpec =
                    org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec((String) name);
            if (bcNamedSpec.getN().equals(bcSpec.getN())
                    && bcNamedSpec.getH().equals(bcSpec.getH())
                    && bcNamedSpec.getCurve().equals(bcSpec.getCurve())
                    && bcNamedSpec.getG().equals(bcSpec.getG())) {
                return new org.bouncycastle.jce.spec.ECNamedCurveSpec(
                        bcNamedSpec.getName(),
                        bcNamedSpec.getCurve(),
                        bcNamedSpec.getG(),
                        bcNamedSpec.getN(),
                        bcNamedSpec.getH(),
                        bcNamedSpec.getSeed());
            }
        }
        return params;
    }

    /**
     * @param pemContent
     * @return
     */
    public static KeyPair loadKeyPair(String pemContent)
            throws IOException, NoSuchProviderException, NoSuchAlgorithmException,
                    InvalidKeySpecException {

        try (PemReader pemReader = new PemReader(new StringReader(pemContent))) {
            PemObject pem = pemReader.readPemObject();
            if (pem == null) {
                return null;
            }

            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(pem.getContent());
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");

            /*
            private key
             */
            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(encodedKeySpec);

            java.security.spec.ECParameterSpec params = privateKey.getParams();
            /*
            public key
             */
            ECParameterSpec bcSpec =
                    org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util.convertSpec(params);
            org.bouncycastle.math.ec.ECPoint q = bcSpec.getG().multiply(privateKey.getS());
            org.bouncycastle.math.ec.ECPoint bcW =
                    bcSpec.getCurve().decodePoint(q.getEncoded(false));
            ECPoint w =
                    new ECPoint(
                            bcW.getAffineXCoord().toBigInteger(),
                            bcW.getAffineYCoord().toBigInteger());
            ECPublicKeySpec keySpec = new ECPublicKeySpec(w, tryFindNamedCurveSpec(params));
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            return new KeyPair(publicKey, privateKey);
        }
    }

    /**
     * @param keyPair
     * @return
     * @throws IOException
     */
    public static String toPemContent(KeyPair keyPair) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(stringWriter)) {
            pemWriter.writeObject(new PemObject("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
        }
        return stringWriter.toString();
    }

    public static String toPubHexString(KeyPair keyPair) {
        BCECPublicKey bcecPublicKey = (BCECPublicKey) keyPair.getPublic();
        return Hex.encodeHexString(bcecPublicKey.getEncoded());
    }

    /**
     * @param keyPair
     * @param data
     * @return
     * @throws CryptoException
     */
    public static SignatureData sign(KeyPair keyPair, byte[] data) throws CryptoException {
        BCECPrivateKey bcecPrivateKey = (BCECPrivateKey) keyPair.getPrivate();
        BCECPublicKey bcecPublicKey = (BCECPublicKey) keyPair.getPublic();

        ECParameterSpec parameterSpec = bcecPrivateKey.getParameters();
        ECDomainParameters domainParameters =
                new ECDomainParameters(
                        parameterSpec.getCurve(),
                        parameterSpec.getG(),
                        parameterSpec.getN(),
                        parameterSpec.getH());

        ECPrivateKeyParameters ecPrivateKeyParameters =
                new ECPrivateKeyParameters(bcecPrivateKey.getD(), domainParameters);

        SM2Signer signer = new SM2Signer();
        CipherParameters parametersWithID =
                new ParametersWithID(
                        new ParametersWithRandom(ecPrivateKeyParameters, new SecureRandom()),
                        withID);
        signer.init(true, parametersWithID);
        signer.update(data, 0, data.length);

        return new SignatureData(signer.generateSignature(), bcecPublicKey.getEncoded());
    }

    public static byte[] sign(String secKeyContent, byte[] data)
            throws CryptoException, IOException, NoSuchProviderException, NoSuchAlgorithmException,
                    InvalidKeySpecException {
        KeyPair keyPair = loadKeyPair(secKeyContent);
        SignatureData signatureData = sign(keyPair, data);
        return signatureData.toBytes();
    }

    /**
     * @param signatureData
     * @param srcData
     */
    public static boolean verify(SignatureData signatureData, byte[] srcData)
            throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {

        X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(signatureData.getPub());
        KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
        BCECPublicKey ecPubKey = (BCECPublicKey) keyFactory.generatePublic(encodedKeySpec);

        ECParameterSpec parameterSpec = ecPubKey.getParameters();
        ECDomainParameters domainParameters =
                new ECDomainParameters(
                        parameterSpec.getCurve(),
                        parameterSpec.getG(),
                        parameterSpec.getN(),
                        parameterSpec.getH());

        CipherParameters param =
                new ParametersWithID(
                        new ECPublicKeyParameters(ecPubKey.getQ(), domainParameters), withID);

        SM2Signer signer = new SM2Signer();
        signer.init(false, param);
        signer.update(srcData, 0, srcData.length);

        return signer.verifySignature(signatureData.getSign());
    }

    public static boolean verify(byte[] signBytes, byte[] srcData)
            throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        SignatureData signatureData = SignatureData.parseFrom(signBytes);
        return verify(signatureData, srcData);
    }
}
