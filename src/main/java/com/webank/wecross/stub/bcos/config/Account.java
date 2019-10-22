package com.webank.wecross.stub.bcos.config;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import org.fisco.bcos.channel.client.P12Manager;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Account {

    private Logger logger = LoggerFactory.getLogger(Account.class);

    private String pemFile;
    private String p12File;
    private String password;

    public Account() {}

    public Account(String pemFile, String p12File, String password) {
        this.pemFile = pemFile;
        this.p12File = p12File;
        this.password = password;
    }

    public Credentials getCredentials(String pattern) {
        Credentials credentials = null;
        try {
            if (pattern.equals("pem")) {
                credentials = loadPemAccount();
            } else if (pattern.equals("p12")) {
                credentials = loadP12Account();
            } else {
                logger.error("get credentials failed, pattern: {} not found", pattern);
            }
        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | CertificateException
                | NoSuchProviderException
                | InvalidKeySpecException
                | UnrecoverableKeyException
                | IOException e) {
            logger.error("get credentials failed: {}", e.toString());
        }
        return credentials;
    }

    // load pem account file
    private Credentials loadPemAccount()
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
                    NoSuchProviderException, InvalidKeySpecException, UnrecoverableKeyException {
        PEMManager pem = new PEMManager();
        pem.setPemFile("classpath:" + pemFile);
        pem.load();
        ECKeyPair keyPair = pem.getECKeyPair();
        Credentials credentials = GenCredential.create(keyPair.getPrivateKey().toString(16));
        System.out.println(credentials.getAddress());
        return credentials;
    }

    // load p12 account file
    private Credentials loadP12Account()
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
                    NoSuchProviderException, InvalidKeySpecException, UnrecoverableKeyException {
        P12Manager p12Manager = new P12Manager();
        p12Manager.setP12File("classpath:" + p12File);
        p12Manager.setPassword(password);
        p12Manager.load();
        ECKeyPair keyPair = p12Manager.getECKeyPair();
        Credentials credentials = GenCredential.create(keyPair.getPrivateKey().toString(16));
        System.out.println(credentials.getAddress());
        return credentials;
    }
}
