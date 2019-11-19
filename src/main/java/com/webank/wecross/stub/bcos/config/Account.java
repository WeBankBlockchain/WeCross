package com.webank.wecross.stub.bcos.config;

import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
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
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Account {

    private Logger logger = LoggerFactory.getLogger(Account.class);

    private String accountFile;
    private String password;
    private EncryptType encryptType;

    public Account(String accountFile, String password) {
        this.accountFile = accountFile;
        this.password = password;
    }

    public Credentials getCredentials() throws WeCrossException {
        Credentials credentials = null;
        try {
            if (accountFile.contains(".pem")) {
                credentials = loadPemAccount();
            } else if (accountFile.contains(".p12")) {
                credentials = loadP12Account();
            } else {
                throw new WeCrossException(Status.UNEXPECTED_CONFIG, "Unsupported account file");
            }
        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | CertificateException
                | NoSuchProviderException
                | InvalidKeySpecException
                | UnrecoverableKeyException
                | IOException e) {
            throw new WeCrossException(Status.INTERNAL_ERROR, e.toString());
        }
        return credentials;
    }

    // load pem account file
    private Credentials loadPemAccount()
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
                    NoSuchProviderException, InvalidKeySpecException, UnrecoverableKeyException {
        PEMManager pem = new PEMManager();
        pem.setPemFile("classpath:" + accountFile);
        pem.load();
        ECKeyPair keyPair = pem.getECKeyPair();
        Credentials credentials = GenCredential.create(keyPair.getPrivateKey().toString(16));

        logger.info("Bcos credentials address: {}", credentials.getAddress());
        return credentials;
    }

    // load p12 account file
    private Credentials loadP12Account()
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
                    NoSuchProviderException, InvalidKeySpecException, UnrecoverableKeyException {
        P12Manager p12Manager = new P12Manager();
        p12Manager.setP12File("classpath:" + accountFile);
        p12Manager.setPassword(password);
        p12Manager.load();
        ECKeyPair keyPair = p12Manager.getECKeyPair();
        Credentials credentials = GenCredential.create(keyPair.getPrivateKey().toString(16));

        logger.info("Bcos credentials address: {}", credentials.getAddress());
        return credentials;
    }

    public String getAccountFile() {
        return accountFile;
    }

    public void setAccountFile(String accountFile) {
        this.accountFile = accountFile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public EncryptType getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(EncryptType encryptType) {
        this.encryptType = encryptType;
    }
}
