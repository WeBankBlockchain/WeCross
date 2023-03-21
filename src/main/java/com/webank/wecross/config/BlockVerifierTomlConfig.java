package com.webank.wecross.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moandjiezana.toml.Toml;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stubmanager.StubManager;
import com.webank.wecross.utils.ConfigUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class BlockVerifierTomlConfig {

    @Resource private StubManager stubManager;

    private static final Logger logger = LoggerFactory.getLogger(BlockVerifierTomlConfig.class);

    @Bean
    public Verifiers newVerifiers() {
        Toml toml = null;
        Verifiers verifiers = new Verifiers();
        try {
            toml = ConfigUtils.getToml(WeCrossDefault.BLOCK_VERIFIER_CONFIG_FILE);
        } catch (WeCrossException e) {
            // did not have verifier toml
            if (e.getErrorCode() == WeCrossException.ErrorCode.DIR_NOT_EXISTS) {
                logger.info(
                        "Open verifier.toml fail: {}, do not verify block, err: {}",
                        e.getErrorCode(),
                        e.getMessage());
                return verifiers;
            } else {
                // have verifier toml, but did not config in right way.
                logger.error("Read verifier.toml fail, err: {}", e.getMessage());
                System.exit(1);
            }
        }
        try {
            verifiers.addVerifiers(toml);
            checkVerifiers(verifiers, this.stubManager.getStubFactories().keySet());
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        return verifiers;
    }

    public static class Verifiers {
        /*
        [verifiers]
            [verifiers.payment.bcos]
                chainType = 'BCOS2.0'
                pubKey = [
                    '123....',
                    '456....'
                ]
             [verifiers.payment.fabric]
                chainType = 'Fabric1.4'
                [verifiers.payment.fabric.endorserCA]
                    Org1MSP = '/path/to/org1/cacrt',
                    Org2MSP = '/path/to/org2/cacrt'
                [verifiers.payment.fabric.ordererCA]
                    OrdererMSP = '/path/to/orderers/cacrt'
         */
        private Map<String, BlockVerifier> verifierHashMap = new HashMap<>();
        private static final Logger logger = LoggerFactory.getLogger(Verifiers.class);

        public Verifiers() {}

        public Verifiers(Toml toml) throws Exception {
            addVerifiers(toml);
        }

        public void addVerifiers(Toml toml) throws Exception {
            // zone => Map<chain, BlockVerifier>
            if (toml == null) {
                throw new IOException("Toml in addVerifiers is null.");
            }
            Map<String, Map<String, Map<String, Object>>> zoneMap =
                    (Map<String, Map<String, Map<String, Object>>>) toml.toMap().get("verifiers");
            if (zoneMap == null) {
                logger.error("Read 'verifiers' in toml illegal.");
                throw new IOException(
                        "Cannot read 'verifiers' in verifier.toml, is this config in right way?.");
            }
            for (String zone : zoneMap.keySet()) {
                for (String chain : zoneMap.get(zone).keySet()) {
                    String chainType =
                            String.valueOf(zoneMap.get(zone).get(chain).get("chainType"));
                    if (!WeCrossDefault.SUPPORTED_STUBS.contains(chainType)) {
                        throw new WeCrossException(
                                WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                                "Wrong chainType in toml.");
                    } else {
                        verifierHashMap.put(
                                zone + "." + chain,
                                new BlockVerifier(zoneMap.get(zone).get(chain)));
                    }
                }
            }
        }

        public Map<String, BlockVerifier> getVerifierHashMap() {
            return verifierHashMap;
        }

        public void setVerifierHashMap(Map<String, BlockVerifier> verifierHashMap) {
            this.verifierHashMap = verifierHashMap;
        }

        public static class BlockVerifier {
            protected Map<String, Object> verifierMap = new HashMap<>();

            @JsonIgnore
            protected static final Logger logger = LoggerFactory.getLogger(BlockVerifier.class);

            public BlockVerifier() {}

            public BlockVerifier(Map<String, Object> verifierMap) {
                this.verifierMap = verifierMap;
            }

            public void checkBlockVerifier() throws WeCrossException {}

            public Map<String, Object> getVerifierMap() {
                return verifierMap;
            }

            public String toJson() throws JsonProcessingException {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(verifierMap);
            }
        }
    }

    public static void readCertInMap(Map<String, String> map) throws WeCrossException {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!ConfigUtils.fileIsExists(entry.getValue())) {
                String errorMessage = "File: " + entry.getValue() + " is not exists";
                throw new WeCrossException(WeCrossException.ErrorCode.DIR_NOT_EXISTS, errorMessage);
            }
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            try {
                // to avoid path manipulation
                String filePath = entry.getValue();
                filePath = filePath.replace("..", "");
                Path path = Paths.get(resolver.getResource(filePath).getURI());
                String newContent = new String(Files.readAllBytes(path));
                map.replace(entry.getKey(), newContent);
            } catch (IOException e) {
                throw new WeCrossException(
                        WeCrossException.ErrorCode.DIR_NOT_EXISTS,
                        "Read Cert fail: " + entry.getKey() + entry.getValue());
            }
        }
    }

    public static void checkVerifiers(Verifiers verifiers, Set<String> stubTypes) throws Exception {
        if (stubTypes == null || stubTypes.isEmpty()) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                    "StubType is empty, please check plugins");
        }
        for (Map.Entry<String, Verifiers.BlockVerifier> blockVerifierEntry :
                verifiers.verifierHashMap.entrySet()) {
            String chainType =
                    (String) blockVerifierEntry.getValue().getVerifierMap().get("chainType");
            if (chainType == null || !stubTypes.contains(chainType)) {
                throw new WeCrossException(
                        WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                        "Verifiers chainType is error, chainType: " + chainType);
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "Check verifiers, verifier: {}, ",
                            blockVerifierEntry.getValue().toJson());
                }
                blockVerifierEntry.getValue().checkBlockVerifier();
            }
        }
    }
}
