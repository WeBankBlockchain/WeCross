package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.routine.htlc.HTLCManager;
import com.webank.wecross.routine.htlc.HTLCTaskInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HTLCManagerConfig {
    private Logger logger = LoggerFactory.getLogger(ConfigReaderConfig.class);

    @Resource Toml toml;

    @Bean
    public HTLCManager newHTLCManager() {
        HTLCManager htlcManager = new HTLCManager();
        List<Map<String, String>> infoList = toml.getList("htlc");
        if (infoList == null) {
            return htlcManager;
        }
        List<HTLCTaskInfo> htlcTaskInfos = new ArrayList<>(infoList.size());
        for (Map<String, String> infoMap : infoList) {
            HTLCTaskInfo htlcTaskInfo = new HTLCTaskInfo();
            String selfPath = getSelfPath(infoMap);
            htlcTaskInfo.setSelfPath(selfPath);
            htlcTaskInfo.setSelfAccount(getSelfAccount(infoMap));
            htlcTaskInfo.setCounterpartyPath(getCounterpartyPath(infoMap));
            htlcTaskInfo.setCounterpartyAccount(getCounterpartyAccount(infoMap));
            htlcTaskInfos.add(htlcTaskInfo);
            htlcManager.addPath(selfPath);
        }
        htlcManager.setHtlcTaskInfos(htlcTaskInfos);
        return htlcManager;
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

    private String getSelfAccount(Map<String, String> infoMap) {
        String selfAccount = infoMap.get("selfAccount");
        if (selfAccount == null) {
            String errorMessage =
                    "Something wrong with [htlc] item, please check [selfAccount] in "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return selfAccount;
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

    private String getCounterpartyAccount(Map<String, String> infoMap) {
        String counterpartyAccount = infoMap.get("counterpartyAccount");
        if (counterpartyAccount == null) {
            String errorMessage =
                    "Something wrong with [htlc] item, please check [counterpartyAccount] in "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return counterpartyAccount;
    }
}
