package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.routine.htlc.*;
import com.webank.wecross.stub.Path;
import com.webank.wecross.zone.ZoneManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HTLCManagerConfig {
    private Logger logger = LoggerFactory.getLogger(HTLCManagerConfig.class);

    @Resource Toml toml;

    @Resource private AccountManager accountManager;

    @Resource private ZoneManager zoneManager;

    @Bean
    public HTLCManager newHTLCManager() {
        System.out.println("Initializing HTLCManager ...");

        HTLCManager htlcManager = new HTLCManager();
        List<Map<String, String>> infoList = toml.getList("htlc");
        if (infoList == null) {
            return htlcManager;
        }
        Map<String, HTLCContext> htlcTaskDataMap = new HashMap<>();

        try {
            for (Map<String, String> infoMap : infoList) {
                HTLCContext htlcContext = new HTLCContext();

                String selfPath = getSelfPath(infoMap);
                checkHTLCResources(selfPath);
                String counterpartyPath = getCounterpartyPath(infoMap);

                htlcContext.setSelfPath(Path.decode(selfPath));
                htlcContext.setCounterpartyPath(Path.decode(counterpartyPath));
                htlcContext.setAdminUA(accountManager.getAdminUA());

                htlcTaskDataMap.put(selfPath, htlcContext);
            }
        } catch (Exception e) {
            logger.error("Failed to new HTLCManager.", e);
            System.exit(1);
        }

        htlcManager.setHtlcTaskDataMap(htlcTaskDataMap);
        logger.info("HTLC resources: {}", Arrays.toString(htlcTaskDataMap.keySet().toArray()));

        htlcManager.initHTLCResourcePairs(zoneManager);
        return htlcManager;
    }

    public void checkHTLCResources(String path) throws Exception {
        com.webank.wecross.resource.Resource selfResource =
                zoneManager.fetchResource(Path.decode(path));
        if (selfResource == null) {
            throw new Exception("HTLC resource: " + path + " not found");
        }
    }

    private String getSelfPath(Map<String, String> infoMap) {
        String selfPath = infoMap.get("selfPath");
        if (selfPath == null) {
            String errorMessage =
                    "Something wrong with [htlc] item, please check [selfPath] in "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return selfPath;
    }

    private String getCounterpartyPath(Map<String, String> infoMap) {
        String counterpartyPath = infoMap.get("counterpartyPath");
        if (counterpartyPath == null) {
            String errorMessage =
                    "Something wrong with [htlc] item, please check [counterpartyPath] in "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return counterpartyPath;
    }
}
