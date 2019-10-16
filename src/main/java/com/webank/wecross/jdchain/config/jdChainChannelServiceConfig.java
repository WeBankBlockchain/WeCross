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
@ConfigurationProperties(prefix = "jdchain-channel-service-manager")
public class jdChainChannelServiceConfig {
    private Logger logger = LoggerFactory.getLogger(jdChainChannelServiceConfig.class);
    private Map<String, jdChainChannelService> services =
            new HashMap<String, jdChainChannelService>();

    public Map<String, jdChainChannelService> getServices() {
        return services;
    }

    public void setServices(Map<String, jdChainChannelService> services) {
        this.services = services;
    }

    @Bean
    public Map<String, jdChainSdk> getJdChainSdkMap() {
        Map<String, jdChainSdk> result = new HashMap<String, jdChainSdk>();
        for (Entry<String, jdChainChannelService> entry : services.entrySet()) {
            jdChainSdk sdk = new jdChainSdk();
            jdChainChannelService service = entry.getValue();
            String publicKey = service.getPublicKey();
            String privateKey = service.getPrivateKey();
            String passWord = service.getPassWord();
            PrivKey privKey = KeyGenUtils.decodePrivKeyWithRawPassword(privateKey, passWord);
            PubKey pubKey = KeyGenUtils.decodePubKey(publicKey);
            sdk.setAdminKey(new BlockchainKeypair(pubKey, privKey));
            for (int i = 0; i < service.getAllChannelConnections().size(); i++) {
                String connStr = service.getAllChannelConnections().get(i);
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
    }
}
