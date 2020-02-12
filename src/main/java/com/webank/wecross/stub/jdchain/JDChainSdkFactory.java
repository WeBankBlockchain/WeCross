package com.webank.wecross.stub.jdchain;

import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.crypto.KeyGenUtils;
import com.jd.blockchain.crypto.PrivKey;
import com.jd.blockchain.crypto.PubKey;
import com.jd.blockchain.ledger.BlockchainKeypair;
import com.jd.blockchain.sdk.BlockchainService;
import com.jd.blockchain.sdk.client.GatewayServiceFactory;
import com.webank.wecross.exception.ErrorCode;
import com.webank.wecross.exception.WeCrossException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainSdkFactory {

    private Logger logger = LoggerFactory.getLogger(JDChainSdkFactory.class);

    private List<JDChainService> jdChainService = new ArrayList<JDChainService>();

    public JDChainSdkFactory(List<JDChainService> jdChainService) {
        this.jdChainService = jdChainService;
    }

    public JDChainSdk getJdChainSdk() throws WeCrossException {

        JDChainSdk jdChainSdk = new JDChainSdk();
        try {
            for (int i = 0; i < jdChainService.size(); i++) {
                JDChainService value = jdChainService.get(i);
                String publicKey = value.getPublicKey();
                String privateKey = value.getPrivateKey();
                String passWord = value.getPassword();
                PrivKey privKey = KeyGenUtils.decodePrivKeyWithRawPassword(privateKey, passWord);
                PubKey pubKey = KeyGenUtils.decodePubKey(publicKey);
                BlockchainKeypair adminKey = new BlockchainKeypair(pubKey, privKey);
                String connStr = value.getConnectionsStr();
                String[] addressLiSt = connStr.split(":");
                if (addressLiSt.length != 2) {
                    continue;
                }
                String ip = addressLiSt[0];
                int port = Integer.parseInt(addressLiSt[1]);
                GatewayServiceFactory serviceFactory =
                        GatewayServiceFactory.connect(ip, port, false, adminKey);
                BlockchainService blockchainService = serviceFactory.getBlockchainService();
                jdChainSdk.getBlockchainService().add(blockchainService);
                jdChainSdk.getAdminKey().add(adminKey);

                if (i == 0) {
                    HashDigest[] ledgerHashs = blockchainService.getLedgerHashs();
                    jdChainSdk.setLedgerHash(ledgerHashs[0]);
                }
            }
            logger.debug("Init jdChainSdk finished");
            return jdChainSdk;

        } catch (Exception e) {
            throw new WeCrossException(ErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    public List<JDChainService> getJdChainService() {
        return jdChainService;
    }

    public void setJdChainService(List<JDChainService> jdChainService) {
        this.jdChainService = jdChainService;
    }
}
