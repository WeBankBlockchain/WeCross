package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stubmanager.MemoryBlockHeaderManagerFactory;
import com.webank.wecross.stubmanager.StubManager;
import com.webank.wecross.utils.ConfigUtils;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.ChainInfo;
import com.webank.wecross.zone.Zone;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZonesConfig {
    private Logger logger = LoggerFactory.getLogger(ZonesConfig.class);

    @Resource Toml toml;

    @Resource StubManager stubManager;

    @Resource MemoryBlockHeaderManagerFactory resourceBlockHeaderManagerFactory;

    @Bean
    public Map<String, Zone> newZoneMap() {
        System.out.println("Initializing ZoneMap ...");

        Map<String, Zone> result = new HashMap<>();
        try {
            String network = toml.getString("common.zone");
            if (network == null) {
                String errorMessage =
                        "\"zone\" in [common] item not found, please check "
                                + WeCrossDefault.MAIN_CONFIG_FILE;
                logger.error(errorMessage);
                System.exit(1);
            }

            Boolean visible = toml.getBoolean("common.visible");
            if (visible == null) {
                String errorMessage =
                        "\"visible\" in [common] item  not found, please check "
                                + WeCrossDefault.MAIN_CONFIG_FILE;
                logger.error(errorMessage);
                System.exit(1);
            }

            String stubsPath = toml.getString("chains.path");
            if (stubsPath == null) {
                stubsPath = toml.getString("stubs.path"); // To support old version
            }
            if (stubsPath == null) {
                String errorMessage =
                        "\"path\" in [chains] item  not found, please check "
                                + WeCrossDefault.MAIN_CONFIG_FILE;
                throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
            }

            Map<String, String> stubsDir = ConfigUtils.getStubsDir(stubsPath);
            Map<String, Chain> stubsBean = getChains(network, stubsDir);
            Zone networkBean = new Zone();
            if (stubsBean != null) {
                // init network bean
                networkBean.setChains(stubsBean);
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

    public Map<String, Chain> getChains(String zone, Map<String, String> chainsDir)
            throws WeCrossException {
        Map<String, Chain> stubMap = new HashMap<>();

        for (String chainName : chainsDir.keySet()) {
            String stubPath = chainsDir.get(chainName);
            String stubFile = stubPath + File.separator + WeCrossDefault.STUB_CONFIG_FILE;
            Toml stubToml;
            try {
                stubToml = ConfigUtils.getToml(stubFile);
            } catch (WeCrossException e) {
                String errorMessage = "Parse " + stubFile + " failed";
                logger.error(errorMessage, e);
                throw new WeCrossException(
                        WeCrossException.ErrorCode.UNEXPECTED_CONFIG, errorMessage);
            }

            String type = stubToml.getString("common.type");
            if (type == null) {
                String errorMessage =
                        "\"type\" in [common] item  not found, please check " + stubFile;
                throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
            }

            StubFactory stubFactory = stubManager.getStubFactory(type);
            if (stubFactory == null) {
                logger.error("Can not find stub type: {}", type);

                throw new WeCrossException(-1, "Cannot find stub type: " + type);
            }
            Connection localConnection = stubFactory.newConnection(stubPath);

            if (localConnection == null) {
                logger.error("Init localConnection: {}-{} failed", stubPath, type);

                throw new WeCrossException(-1, "Init localConnection failed");
            }

            List<ResourceInfo> resources = localConnection.getResources();

            ChainInfo chainInfo = new ChainInfo();
            chainInfo.setName(chainName);
            chainInfo.setProperties(localConnection.getProperties());
            chainInfo.setStubType(type);
            chainInfo.setResources(resources);

            Driver driver = stubFactory.newDriver();

            Chain chain =
                    new Chain(
                            zone,
                            chainInfo.getName(),
                            chainInfo.getStubType(),
                            driver,
                            localConnection);
            chain.setDriver(stubFactory.newDriver());
            chain.setBlockHeaderManager(resourceBlockHeaderManagerFactory.build(chain));
            for (ResourceInfo resourceInfo : resources) {
                com.webank.wecross.resource.Resource resource =
                        new com.webank.wecross.resource.Resource();
                resource.setDriver(chain.getDriver());
                resource.addConnection(null, localConnection);
                resource.setType(type);
                resource.setResourceInfo(resourceInfo);

                resource.setBlockHeaderManager(chain.getBlockHeaderManager());

                chain.getResources().put(resourceInfo.getName(), resource);
                logger.info(
                        "Load local resource({}.{}.{}): {}",
                        zone,
                        chainName,
                        resource.getResourceInfo().getName(),
                        resource.getResourceInfo());
            }
            stubMap.put(chainName, chain);
            chain.start();
        }

        return stubMap;
    }

    public Toml getToml() {
        return toml;
    }

    public void setToml(Toml toml) {
        this.toml = toml;
    }
}
