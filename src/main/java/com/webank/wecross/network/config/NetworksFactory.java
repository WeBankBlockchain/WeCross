package com.webank.wecross.network.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.config.ConfigInfo;
import com.webank.wecross.config.ConfigUtils;
import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.Network;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.bcos.BCOSStub;
import com.webank.wecross.stub.bcos.config.BCOSStubFactory;
import com.webank.wecross.stub.jdchain.JDChainStub;
import com.webank.wecross.stub.jdchain.config.JDChainStubFactory;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NetworksFactory {

    private Logger logger = LoggerFactory.getLogger(NetworksFactory.class);

    @Resource(name = "produceToml")
    Toml toml;

    @Bean
    public Map<String, Network> produceNetworks() {
        Map<String, Network> result = new HashMap<>();

        try {
            String network = toml.getString("common.network");
            if (network == null) {
                String errorMessage =
                        "\"network\" in [common] item  not found, please check "
                                + ConfigInfo.MAIN_CONFIG_FILE;
                logger.error(errorMessage);
                System.exit(1);
            }

            Boolean visible = toml.getBoolean("common.visible");
            if (visible == null) {
                String errorMessage =
                        "\"visible\" in [common] item  not found, please check "
                                + ConfigInfo.MAIN_CONFIG_FILE;
                logger.error(errorMessage);
                System.exit(1);
            }

            String stubsPath = toml.getString("stubs.path");
            if (stubsPath == null) {
                String errorMessage =
                        "\"path\" in [stubs] item  not found, please check "
                                + ConfigInfo.MAIN_CONFIG_FILE;
                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
            }

            Map<String, String> stubsDir = ConfigUtils.getStubsDir(stubsPath);
            Map<String, Stub> stubsBean = getStubs(network, stubsDir);
            Network networkBean = new Network();
            if (stubsBean != null) {
                // init network bean
                networkBean.setStubs(stubsBean);
                networkBean.setVisible(visible);
                result.put(network, networkBean);
            } else {
                logger.error("No stubs found in {}", network);
                System.exit(1);
            }

        } catch (WeCrossException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }

        return result;
    }

    public Map<String, Stub> getStubs(String network, Map<String, String> stubsDir)
            throws WeCrossException {
        Map<String, Stub> stubMap = new HashMap<>();

        for (String stub : stubsDir.keySet()) {
            String stubPath = stubsDir.get(stub);
            Toml stubToml;
            try {
                stubToml = ConfigUtils.getToml(stubPath);
            } catch (WeCrossException e) {
                logger.warn(e.getMessage());
                continue;
            }

            String stubName = stubToml.getString("common.stub");
            if (stubName == null) {
                String errorMessage =
                        "\"stub\" in [common] item  not found, please check " + stubPath;
                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
            }

            if (!stub.equals(stubName)) {
                String errorMessage =
                        "\"stub\" in [common] item  must be same with directory name, please check "
                                + stubPath;
                throw new WeCrossException(Status.UNEXPECTED_CONFIG, errorMessage);
            }

            String type = stubToml.getString("common.type");
            if (type == null) {
                String errorMessage =
                        "\"type\" in [common] item  not found, please check " + stubPath;
                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
            }

            Map<String, Object> stubConfig = stubToml.toMap();

            switch (type) {
                case ConfigInfo.STUB_TYPE_BCOS:
                    {
                        BCOSStub bcosStub =
                                BCOSStubFactory.getBcosStub(network, stub, stubPath, stubConfig);
                        stubMap.put(stub, bcosStub);
                        break;
                    }
                case ConfigInfo.STUB_TYPE_JDCHAIN:
                    {
                        JDChainStub jdChainStub =
                                JDChainStubFactory.getJDChainStub(
                                        network, stub, stubPath, stubConfig);
                        stubMap.put(stub, jdChainStub);
                        break;
                    }
                default:
                    {
                        String errorMessage = "Undefined stub type: " + type;
                        throw new WeCrossException(Status.UNEXPECTED_CONFIG, errorMessage);
                    }
            }
        }

        return stubMap;
    }

    //
    //    public Map<String, Stub> initStub(String networkName, Map<String, Object> stubs)
    //            throws WeCrossException {
    //        if (stubs == null) {
    //            return null;
    //        }
    //
    //        Map<String, Stub> stubsBean = new HashMap<>();
    //
    //        for (String stubName : stubs.keySet()) {
    //            @SuppressWarnings("unchecked")
    //            Map<String, Object> stubConfig = (Map<String, Object>) stubs.get(stubName);
    //
    //            if (!stubConfig.containsKey("type") || ((String)
    // stubConfig.get("type")).equals("")) {
    //                String errorMessage = "\"type\" of stub not found: " + stubName;
    //                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //            }
    //
    //            String Stubtype = (String) stubConfig.get("type");
    //
    //            if (Stubtype.equalsIgnoreCase(ConfigInfo.STUB_TYPE_BCOS)) {
    //                BCOSStub bcosStub = getBcosStub(networkName, stubName, stubConfig);
    //                stubsBean.put(stubName, bcosStub);
    //
    //            } else if (Stubtype.equalsIgnoreCase(ConfigInfo.STUB_TYPE_JDCHAIN)) {
    //                JDChainStub jdChainStub = getJdStub(networkName, stubName, stubConfig);
    //                stubsBean.put(stubName, jdChainStub);
    //
    //            } else {
    //                String errorMessage = "Undefined stub type: " + Stubtype;
    //                throw new WeCrossException(Status.UNEXPECTED_CONFIG, errorMessage);
    //            }
    //        }
    //
    //        logger.debug("Init stubs finished");
    //        return stubsBean;
    //    }
    //
    //    public BCOSStub getBcosStub(String networkName, String stubName, Map<String, Object>
    // stubConfig)
    //            throws WeCrossException {
    //        if (!stubConfig.containsKey("accounts")) {
    //            String errorMessage = "\"accounts\" of bcos stub not found: " + stubName;
    //            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //        }
    //
    //        @SuppressWarnings("unchecked")
    //        Map<String, String> accountConfig = (Map<String, String>) stubConfig.get("accounts");
    //        Account account = ConfigUtils.getBcosAccount(accountConfig);
    //
    //        if (!stubConfig.containsKey("channelService") || stubConfig.get("channelService") ==
    // null) {
    //            String errorMessage = "\"channelService\" of bcos stub not found: " + stubName;
    //            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //        }
    //
    //        @SuppressWarnings("unchecked")
    //        Map<String, Object> channelServiceConfig =
    //                (Map<String, Object>) stubConfig.get("channelService");
    //        ChannelService channelService = ConfigUtils
    //            .getBcosChannelServiceYmlVersion(channelServiceConfig);
    //
    //        if (!stubConfig.containsKey("resources")) {
    //            String warnMessage = "\"resources\" of bcos stub not found: " + stubName;
    //            logger.warn(warnMessage);
    //
    //            BCOSStubFactory bcosStubConfig = new BCOSStubFactory();
    //            BCOSStub bcosStub =
    //                bcosStubConfig.initBCOSStubYmlVersion(
    //                            networkName, stubName, account, channelService, null);
    //            return bcosStub;
    //        } else {
    //            // parse bcos resources
    //            @SuppressWarnings("unchecked")
    //            Map<String, Map<String, String>> bcosResources =
    //                    (Map<String, Map<String, String>>) stubConfig.get("resources");
    //
    //            BCOSStubFactory bcosStubConfig = new BCOSStubFactory();
    //            BCOSStub bcosStub =
    //                bcosStubConfig.initBCOSStubYmlVersion(
    //                            networkName, stubName, account, channelService, bcosResources);
    //            return bcosStub;
    //        }
    //    }
    //
    //    public JDChainStub getJdStub(
    //            String networkName, String stubName, Map<String, Object> stubConfig)
    //            throws WeCrossException {
    //        if (!stubConfig.containsKey("jdService")) {
    //            String errorMessage = "\"jdService\" of jdchain stub not found: " + stubName;
    //            throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
    //        }
    //
    //        @SuppressWarnings("unchecked")
    //        Map<String, Object> jdChainServiceConfig =
    //                (Map<String, Object>) stubConfig.get("jdService");
    //        List<JDChainService> jdChainService = ConfigUtils
    //            .getJDChainServiceYmlVersion(jdChainServiceConfig);
    //
    //        if (!stubConfig.containsKey("resources")) {
    //            String warnMessage = "\"resources\" of jdchain stub not found: " + stubName;
    //            logger.warn(warnMessage);
    //
    //            JDChainStubFactory jdChainStubConfig = new JDChainStubFactory();
    //            JDChainStub jdChainStub =
    //                jdChainStubConfig
    //                    .initJdChainStubYmlVersion(networkName, stubName, jdChainService, null);
    //            return jdChainStub;
    //        }
    //
    //        @SuppressWarnings("unchecked")
    //        Map<String, Map<String, String>> jdChainResources =
    //                (Map<String, Map<String, String>>) stubConfig.get("resources");
    //
    //        JDChainStubFactory jdChainStubConfig = new JDChainStubFactory();
    //        JDChainStub jdChainStub =
    //            jdChainStubConfig.initJdChainStubYmlVersion(
    //                        networkName, stubName, jdChainService, jdChainResources);
    //        return jdChainStub;
    //    }
}
