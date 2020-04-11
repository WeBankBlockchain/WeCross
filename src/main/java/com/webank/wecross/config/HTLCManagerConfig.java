package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.routine.htlc.HTLC;
import com.webank.wecross.routine.htlc.HTLCManager;
import com.webank.wecross.routine.htlc.HTLCTaskInfo;
import com.webank.wecross.routine.htlc.WeCrossHTLC;
import com.webank.wecross.stub.Account;
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
    private Logger logger = LoggerFactory.getLogger(ConfigReaderConfig.class);

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
        Map<String, HTLCTaskInfo> htlcTaskInfos = new HashMap<>();

        try {
            for (Map<String, String> infoMap : infoList) {
                HTLCTaskInfo htlcTaskInfo = new HTLCTaskInfo();

                String selfPath = getSelfPath(infoMap);
                checkHtlcResources(selfPath);

                String counterpartyPath = getCounterpartyPath(infoMap);
                String selfAccount = getAccount1(infoMap);
                String counterpartyAccount = getAccount2(infoMap);

                htlcTaskInfo.setSelfPath(selfPath);
                htlcTaskInfo.setSelfAccount(selfAccount);

                htlcTaskInfo.setCounterpartyPath(counterpartyPath);
                htlcTaskInfo.setCounterpartyAccount(counterpartyAccount);
                htlcTaskInfo.setCounterpartyAddress(getCounterpartyHtlc(selfPath, selfAccount));

                htlcTaskInfos.put(selfPath, htlcTaskInfo);
            }
        } catch (Exception e) {
            logger.error("failed to new HTLCManager: {}", e.getMessage());
            System.exit(-1);
        }

        htlcManager.setHtlcTaskInfos(htlcTaskInfos);
        logger.info("HTLC resources: {}", Arrays.toString(htlcTaskInfos.keySet().toArray()));
        return htlcManager;
    }

    public void checkHtlcResources(String path) throws Exception {
        com.webank.wecross.resource.Resource selfResource =
                zoneManager.getResource(Path.decode(path));
        if (selfResource == null) {
            throw new Exception("htlc resource: " + path + " not found");
        }
    }

    private String getCounterpartyHtlc(String iPath, String accountName) {
        Path path = null;
        try {
            path = Path.decode(iPath);
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(1);
        }

        com.webank.wecross.resource.Resource resource = zoneManager.getResource(path);
        HTLC htlc = new WeCrossHTLC();
        Account account = accountManager.getAccount(accountName);
        String counterpartyaddress = null;
        try {
            counterpartyaddress = htlc.getCounterpartyHtlc(resource, account);
        } catch (WeCrossException e) {
            logger.error("failed to getCounterpartyHtlc, path: {}", iPath);
            System.exit(1);
        }
        return counterpartyaddress;
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

    private String getAccount1(Map<String, String> infoMap) {
        String selfAccount = infoMap.get("account1");
        if (selfAccount == null) {
            String errorMessage =
                    "Something wrong with [htlc] item, please check [account1] in "
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

    private String getAccount2(Map<String, String> infoMap) {
        String counterpartyAccount = infoMap.get("account2");
        if (counterpartyAccount == null) {
            String errorMessage =
                    "Something wrong with [htlc] item, please check [account2] in "
                            + WeCrossDefault.MAIN_CONFIG_FILE;
            logger.error(errorMessage);
            System.exit(1);
        }
        return counterpartyAccount;
    }
}
