package com.webank.wecross.config;

import static com.webank.wecross.common.WeCrossDefault.BCOS_NODE_ID_LENGTH;
import static com.webank.wecross.common.WeCrossDefault.SUPPORTED_STUBS;
import static com.webank.wecross.utils.ConfigUtils.*;

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
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class BlockVerifierTomlConfig {
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
            checkVerifiers(verifiers);
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
                    if ("Fabric1.4".equals(chainType)) {
                        verifierHashMap.put(
                                zone + "." + chain,
                                new FabricVerifier(zoneMap.get(zone).get(chain)));
                    } else if ("BCOS2.0".equals(chainType) || "GM_BCOS2.0".equals(chainType)) {
                        verifierHashMap.put(
                                zone + "." + chain, new BCOSVerifier(zoneMap.get(zone).get(chain)));
                    } else {
                        throw new Exception("Wrong chainType in toml.");
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

            public void checkBlockVerifier() throws WeCrossException {
                if (chainType == null) {
                    throw new WeCrossException(
                            WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                            "chainType is null, please check.");
                }
                if (!SUPPORTED_STUBS.contains(chainType)) {
                    throw new WeCrossException(
                            WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                            "Verifiers chainType is not supported, please check. chainType is : "
                                    + chainType);
                }
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

            @Override
            public void checkBlockVerifier() throws WeCrossException {
                super.checkBlockVerifier();
                if (endorserCA == null
                        || ordererCA == null
                        || endorserCA.size() == 0
                        || ordererCA.size() == 0) {
                    throw new WeCrossException(
                            WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                            "Fabric block verifier config is wrong, endorserCA or ordererCA is null, please check.");
                }
                for (Map.Entry<String, String> entry : endorserCA.entrySet()) {
                    if (!Pattern.compile(CERT, Pattern.MULTILINE)
                            .matcher(entry.getValue())
                            .matches()) {
                        throw new WeCrossException(
                                WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                                "Fabric endorserCA cert pattern matches error, please check.");
                    }
                }
                for (Map.Entry<String, String> entry : ordererCA.entrySet()) {
                    if (!Pattern.compile(CERT, Pattern.MULTILINE)
                            .matcher(entry.getValue())
                            .matches()) {
                        throw new WeCrossException(
                                WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                                "Fabric ordererCA cert pattern matches error, please check.");
                    }
                }
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
            public void checkBlockVerifier() throws WeCrossException {
                super.checkBlockVerifier();
                if (pubKey == null) {
                    throw new WeCrossException(
                            WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                            "pubKey is null in BCOS Verifier.");
                }
                for (String key : pubKey) {
                    if (key.length() != BCOS_NODE_ID_LENGTH) {
                        throw new WeCrossException(
                                WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                                "pubKey length is not in conformity with the BCOS right way, pubKey: "
                                        + key
                                        + " length is "
                                        + key.length());
                    }
                }
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
    }

    public static void readCertInMap(Map<String, String> map) throws WeCrossException {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!fileIsExists(entry.getValue())) {
                String errorMessage = "File: " + entry.getValue() + " is not exists";
                throw new WeCrossException(WeCrossException.ErrorCode.DIR_NOT_EXISTS, errorMessage);
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

    public static void checkVerifiers(Verifiers verifiers) throws WeCrossException {
        for (Map.Entry<String, Verifiers.BlockVerifier> blockVerifierEntry :
                verifiers.verifierHashMap.entrySet()) {
            if (blockVerifierEntry.getValue().chainType == null) {
                throw new WeCrossException(
                        WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                        "Verifiers chainType is null, please check.");
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
