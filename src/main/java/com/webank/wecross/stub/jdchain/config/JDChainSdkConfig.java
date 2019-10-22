package com.webank.wecross.stub.jdchain.config;

import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.crypto.KeyGenUtils;
import com.jd.blockchain.crypto.PrivKey;
import com.jd.blockchain.crypto.PubKey;
import com.jd.blockchain.ledger.BlockchainKeypair;
import com.jd.blockchain.sdk.BlockchainService;
import com.jd.blockchain.sdk.client.GatewayServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainSdkConfig {

    private Logger logger = LoggerFactory.getLogger(JDChainSdkConfig.class);

    private JDChainService jdChainService;

    public JDChainSdkConfig(JDChainService jdChainService) {
        this.jdChainService = jdChainService;
    }

    public JDChainSdk getJdChainSdk() {

        JDChainSdk jdChainSdk = new JDChainSdk();
        if (jdChainService == null) {
            logger.info("no jdchain configuration found");
            return jdChainSdk;
        }
        try {
            String publicKey = jdChainService.getPublicKey();
            String privateKey = jdChainService.getPrivateKey();
            String passWord = jdChainService.getPassword();
            PrivKey privKey = KeyGenUtils.decodePrivKeyWithRawPassword(privateKey, passWord);
            PubKey pubKey = KeyGenUtils.decodePubKey(publicKey);
            jdChainSdk.setAdminKey(new BlockchainKeypair(pubKey, privKey));
            for (int i = 0; i < jdChainService.getConnectionsStr().size(); i++) {
                String connStr = jdChainService.getConnectionsStr().get(i);
                String[] addressLiSt = connStr.split(":");
                if (addressLiSt.length != 2) {
                    continue;
                }
                String ip = addressLiSt[0];
                int port = Integer.parseInt(addressLiSt[1]);

                GatewayServiceFactory serviceFactory =
                        GatewayServiceFactory.connect(ip, port, false, jdChainSdk.getAdminKey());
                BlockchainService blockchainService = serviceFactory.getBlockchainService();
                jdChainSdk.getBlockchainService().add(blockchainService);

                if (i == 0) {
                    HashDigest[] ledgerHashs = blockchainService.getLedgerHashs();
                    jdChainSdk.setLedgerHash(ledgerHashs[0]);
                }
            }
        } catch (Exception e) {
            logger.error("something wrong with getJdChainSdk: {}", e.toString());
            return null;
        }
        return jdChainSdk;
    }

    public JDChainService getJdChainService() {
        return jdChainService;
    }

    public void setJdChainService(JDChainService jdChainService) {
        this.jdChainService = jdChainService;
    }
}
