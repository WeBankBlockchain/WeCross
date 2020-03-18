package com.webank.wecross.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.util.CharsetUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class KeyCertLoader {
    private static final Pattern CERT_PATTERN =
            Pattern.compile(
                    "-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+"
                            + // Header
                            "([a-z0-9+/=\\r\\n]+)"
                            + // Base64 text
                            "-+END\\s+.*CERTIFICATE[^-]*-+", // Footer
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern KEY_PATTERN =
            Pattern.compile(
                    "-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+"
                            + // Header
                            "([a-z0-9+/=\\r\\n]+)"
                            + // Base64 text
                            "-+END\\s+.*PRIVATE\\s+KEY[^-]*-+", // Footer
                    Pattern.CASE_INSENSITIVE);

    PKCS8EncodedKeySpec generateKeySpec(char[] password, byte[] key)
            throws IOException, NoSuchAlgorithmException, NoSuchPaddingException,
                    InvalidKeySpecException, InvalidKeyException,
                    InvalidAlgorithmParameterException {

        if (password == null) {
            return new PKCS8EncodedKeySpec(key);
        }

        EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(key);
        SecretKeyFactory keyFactory =
                SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.getAlgName());
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);

        Cipher cipher = Cipher.getInstance(encryptedPrivateKeyInfo.getAlgName());
        cipher.init(Cipher.DECRYPT_MODE, pbeKey, encryptedPrivateKeyInfo.getAlgParameters());

        return encryptedPrivateKeyInfo.getKeySpec(cipher);
    }

    PrivateKey getPrivateKeyFromByteBuffer(ByteBuf encodedKeyBuf, String keyPassword)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
                    InvalidAlgorithmParameterException, KeyException, IOException {

        byte[] encodedKey = new byte[encodedKeyBuf.readableBytes()];
        encodedKeyBuf.readBytes(encodedKey).release();

        PKCS8EncodedKeySpec encodedKeySpec =
                generateKeySpec(keyPassword == null ? null : keyPassword.toCharArray(), encodedKey);
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException ignore) {
            try {
                return KeyFactory.getInstance("DSA").generatePrivate(encodedKeySpec);
            } catch (InvalidKeySpecException ignore2) {
                try {
                    return KeyFactory.getInstance("EC").generatePrivate(encodedKeySpec);
                } catch (InvalidKeySpecException e) {
                    throw new InvalidKeySpecException("Neither RSA, DSA nor EC worked", e);
                }
            }
        }
    }

    public PrivateKey toPrivateKey(InputStream keyInputStream, String keyPassword)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
                    InvalidAlgorithmParameterException, KeyException, IOException {
        if (keyInputStream == null) {
            return null;
        }
        return getPrivateKeyFromByteBuffer(readPrivateKey(keyInputStream), keyPassword);
    }

    ByteBuf readPrivateKey(InputStream in) throws KeyException {
        String content;
        try {
            content = readContent(in);
        } catch (IOException e) {
            throw new KeyException("failed to read key input stream", e);
        }

        Matcher m = KEY_PATTERN.matcher(content);
        if (!m.find()) {
            throw new KeyException(
                    "could not find a PKCS #8 private key in input stream"
                            + " (see https://netty.io/wiki/sslcontextbuilder-and-private-key.html for more information)");
        }

        ByteBuf base64 = Unpooled.copiedBuffer(m.group(1), CharsetUtil.US_ASCII);
        ByteBuf der = Base64.decode(base64);
        base64.release();
        return der;
    }

    public String readContent(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[8192];
            for (; ; ) {
                int ret = in.read(buf);
                if (ret < 0) {
                    break;
                }
                out.write(buf, 0, ret);
            }
            return out.toString(CharsetUtil.US_ASCII.name());
        } finally {
            out.close();
        }
    }

    public ByteBuf[] readCertificates(InputStream in) throws CertificateException {
        String content;
        try {
            content = readContent(in);
        } catch (IOException e) {
            throw new CertificateException("failed to read certificate input stream", e);
        }

        List<ByteBuf> certs = new ArrayList<ByteBuf>();
        Matcher m = CERT_PATTERN.matcher(content);
        int start = 0;
        for (; ; ) {
            if (!m.find(start)) {
                break;
            }

            ByteBuf base64 = Unpooled.copiedBuffer(m.group(1), CharsetUtil.US_ASCII);
            ByteBuf der = Base64.decode(base64);
            base64.release();
            certs.add(der);

            start = m.end();
        }

        if (certs.isEmpty()) {
            throw new CertificateException("found no certificates in input stream");
        }

        return certs.toArray(new ByteBuf[0]);
    }

    public X509Certificate[] toX509Certificates(InputStream in) throws CertificateException {
        if (in == null) {
            return null;
        }
        return getCertificatesFromBuffers(readCertificates(in));
    }

    private X509Certificate[] getCertificatesFromBuffers(ByteBuf[] certs)
            throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate[] x509Certs = new X509Certificate[certs.length];

        try {
            for (int i = 0; i < certs.length; i++) {
                InputStream is = new ByteBufInputStream(certs[i], false);
                try {
                    x509Certs[i] = (X509Certificate) cf.generateCertificate(is);
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // This is not expected to happen, but re-throw in case it does.
                        throw new RuntimeException(e);
                    }
                }
            }
        } finally {
            for (ByteBuf buf : certs) {
                buf.release();
            }
        }
        return x509Certs;
    }
}
