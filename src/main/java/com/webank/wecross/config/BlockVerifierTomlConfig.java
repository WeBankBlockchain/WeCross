package com.webank.wecross.config;

import static com.webank.wecross.utils.ConfigUtils.fileIsExists;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moandjiezana.toml.Toml;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.utils.ConfigUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class BlockVerifierTomlConfig {
    private Logger logger = LoggerFactory.getLogger(BlockVerifierTomlConfig.class);

    @Bean
    public Verifiers newVerifiers() {
        Toml toml = null;
        Verifiers verifiers = new Verifiers();
        try {
            toml = ConfigUtils.getToml(WeCrossDefault.BLOCK_VERIFIER_CONFIG_FILE);
        } catch (WeCrossException e) {
            if (e.getErrorCode() == WeCrossException.ErrorCode.DIR_NOT_EXISTS) {
                logger.warn(
                        "Open verifier.toml fail: {}, do not verify block, err: {}",
                        e.getErrorCode(),
                        e.getMessage());
                return verifiers;
            }
            logger.error("Read verifier.toml fail, err: {}", e.getMessage());
            System.exit(1);
        }
        try {
            // have verifier toml, but did not config in right way.
            verifiers.addVerifiers(toml);
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
                endorserCA = {
                    Org1MSP = '/path/to/org1/cacrt',
                    Org2MSP = '/path/to/org2/cacrt'
                }
                ordererCA = '/path/to/orderers/cacrt'
         */
        private Map<String, BlockVerifier> verifiers = new HashMap<>();
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
                    if ("Fabric1.4".equals(chainType)) {
                        verifiers.put(
                                zone + "." + chain,
                                new FabricVerifier(zoneMap.get(zone).get(chain)));
                    } else if ("BCOS2.0".equals(chainType) || "GM_BCOS2.0".equals(chainType)) {
                        verifiers.put(
                                zone + "." + chain, new BCOSVerifier(zoneMap.get(zone).get(chain)));
                    } else {
                        throw new Exception("Wrong chainType in toml.");
                    }
                }
            }
        }

        public Map<String, BlockVerifier> getVerifiers() {
            return verifiers;
        }

        public void setVerifiers(Map<String, BlockVerifier> verifiers) {
            this.verifiers = verifiers;
        }

        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                property = "chainType",
                visible = true,
                include = JsonTypeInfo.As.EXISTING_PROPERTY)
        @JsonSubTypes(
                value = {
                    @JsonSubTypes.Type(value = BCOSVerifier.class, name = "BCOS2.0"),
                    @JsonSubTypes.Type(value = BCOSVerifier.class, name = "GM_BCOS2.0"),
                    @JsonSubTypes.Type(value = FabricVerifier.class, name = "Fabric1.4")
                })
        public static class BlockVerifier {
            protected String chainType;

            @JsonIgnore
            protected static final Logger logger = LoggerFactory.getLogger(BlockVerifier.class);

            public BlockVerifier() {}

            public BlockVerifier(String chainType) {
                this.chainType = chainType;
            }

            public BlockVerifier(Map<String, Object> blockVerifier) throws Exception {
                chainType = parseStringBase(blockVerifier, "chainType");
            }

            public String getChainType() {
                return chainType;
            }

            public String toJson() {
                return "{\n" + "\t\"chainType\": " + chainType + "\n}";
            }
        }

        public static class FabricVerifier extends BlockVerifier {
            Map<String, String> endorserCA;
            Map<String, String> ordererCA;

            public FabricVerifier() {}

            public FabricVerifier(
                    String chainType,
                    Map<String, String> endorserCA,
                    Map<String, String> ordererCA) {
                super(chainType);
                this.endorserCA = endorserCA;
                this.ordererCA = ordererCA;
            }

            public FabricVerifier(Map<String, Object> blockVerifier) throws Exception {
                super(blockVerifier);
                endorserCA = parseMapBase(blockVerifier, "endorserCA");
                ordererCA = parseMapBase(blockVerifier, "ordererCA");
                // mspID => path to mspID => cert
                readCertInMap(endorserCA);
                readCertInMap(ordererCA);
            }

            public Map<String, String> getEndorserCA() {
                return endorserCA;
            }

            public Map<String, String> getOrdererCA() {
                return ordererCA;
            }

            @Override
            public String toJson() {
                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                String json = null;
                try {
                    json = objectMapper.writeValueAsString(this);
                } catch (JsonProcessingException e) {
                    logger.error("Write FabricVerifier to JSON error.");
                }
                return json;
            }
        }

        public static class BCOSVerifier extends BlockVerifier {
            List<String> pubKey;

            public BCOSVerifier() {}

            public BCOSVerifier(String blockType, List<String> pubKey) {
                super(blockType);
                this.pubKey = pubKey;
            }

            public BCOSVerifier(Map<String, Object> blockVerifier) throws Exception {
                super(blockVerifier);
                pubKey = parseStringList(blockVerifier, "pubKey");
            }

            public List<String> getPubKey() {
                return pubKey;
            }

            @Override
            public String toJson() {
                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                String json = null;
                try {
                    json = objectMapper.writeValueAsString(this);
                } catch (JsonProcessingException e) {
                    logger.error("Write BCOSVerifier to JSON error.");
                }
                return json;
            }
        }

        private static Map<String, String> parseMapBase(Map<String, Object> map, String key)
                throws IOException {
            Map<String, String> res = (Map<String, String>) map.get(key);

            if (res == null) {
                throw new IOException("'" + key + "' item not found");
            }
            return res;
        }

        private static String parseStringBase(Map<String, Object> map, String key)
                throws IOException {
            String res = (String) map.get(key);

            if (res == null) {
                throw new IOException("'" + key + "' item not found");
            }
            return res;
        }

        private static List<String> parseStringList(Map<String, Object> map, String key)
                throws IOException {
            @SuppressWarnings("unchecked")
            List<String> res = (List<String>) map.get(key);

            if (res == null) {
                throw new IOException("'" + key + "' item illegal");
            }
            return res;
        }

        private static void readCertInMap(Map<String, String> map) throws WeCrossException {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (!fileIsExists(entry.getValue())) {
                    String errorMessage = "File: " + entry.getValue() + " is not exists";
                    throw new WeCrossException(
                            WeCrossException.ErrorCode.DIR_NOT_EXISTS, errorMessage);
                }
                PathMatchingResourcePatternResolver resolver =
                        new PathMatchingResourcePatternResolver();
                try {
                    Path path = Paths.get(resolver.getResource(entry.getValue()).getURI());
                    String newContent = new String(Files.readAllBytes(path));
                    map.replace(entry.getKey(), newContent);
                } catch (IOException e) {
                    throw new WeCrossException(
                            WeCrossException.ErrorCode.DIR_NOT_EXISTS,
                            "Read Cert fail: " + entry.getKey() + entry.getValue());
                }
            }
        }
    }
}
