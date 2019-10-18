package com.webank.wecross.jdchain.config;

import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.crypto.KeyGenUtils;
import com.jd.blockchain.crypto.PrivKey;
import com.jd.blockchain.crypto.PubKey;
import com.jd.blockchain.ledger.BlockchainKeypair;
import com.jd.blockchain.sdk.BlockchainService;
import com.jd.blockchain.sdk.client.GatewayServiceFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jdchain-service-manager")
public class JDChainServiceConfig {
    private Logger logger = LoggerFactory.getLogger(JDChainServiceConfig.class);
    private Map<String, JDChainService> services = new HashMap<String, JDChainService>();

    public Map<String, JDChainService> getServices() {
        return services;
    }

    public void setServices(Map<String, JDChainService> services) {
        this.services = services;
    }

    @Bean
    public Map<String, JDChainSdk> getJdChainSdkMap() {

        Map<String, JDChainSdk> result = new HashMap<String, JDChainSdk>();
        try {
            for (Entry<String, JDChainService> entry : services.entrySet()) {
                JDChainSdk sdk = new JDChainSdk();
                JDChainService service = entry.getValue();
                String publicKey = service.getPublicKey();
                String privateKey = service.getPrivateKey();
                String passWord = service.getPassWord();
                PrivKey privKey = KeyGenUtils.decodePrivKeyWithRawPassword(privateKey, passWord);
                PubKey pubKey = KeyGenUtils.decodePubKey(publicKey);
                sdk.setAdminKey(new BlockchainKeypair(pubKey, privKey));
                for (int i = 0; i < service.getConnectionsStr().size(); i++) {
                    String connStr = service.getConnectionsStr().get(i);
                    String[] addressLiSt = connStr.split(":");
                    if (addressLiSt.length != 2) {
                        continue;
                    }
                    String ip = addressLiSt[0];
                    int port = Integer.parseInt(addressLiSt[1]);

                    GatewayServiceFactory serviceFactory =
                            GatewayServiceFactory.connect(ip, port, false, sdk.getAdminKey());
                    BlockchainService blockchainService = serviceFactory.getBlockchainService();
                    sdk.getBlockchainService().add(blockchainService);

                    if (i == 0) {
                        HashDigest[] ledgerHashs = blockchainService.getLedgerHashs();
                        sdk.setLedgerHash(ledgerHashs[0]);
                    }
                }
                result.put(entry.getKey(), sdk);
            }
            return result;

        } catch (Exception e) {
            return result;
        }
    }
}
