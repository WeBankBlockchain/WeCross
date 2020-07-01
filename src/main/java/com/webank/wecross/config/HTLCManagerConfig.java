package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.routine.RoutineDefault;
import com.webank.wecross.routine.htlc.*;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Path;
import com.webank.wecross.zone.ZoneManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
        Map<String, HTLCTaskData> htlcTaskDataMap = new HashMap<>();

        try {
            for (Map<String, String> infoMap : infoList) {
                HTLCTaskData htlcTaskData = new HTLCTaskData();

                String selfPath = getSelfPath(infoMap);
                checkHTLCResources(selfPath);

                String counterpartyPath = getCounterpartyPath(infoMap);
                String account1 = getAccount1(infoMap);
                String account2 = getAccount2(infoMap);

                htlcTaskData.setSelfPath(Path.decode(selfPath));
                htlcTaskData.setAccount1(accountManager.getAccount(account1));
                htlcTaskData.setCounterpartyPath(Path.decode(counterpartyPath));
                htlcTaskData.setAccount2(accountManager.getAccount(account2));
                htlcTaskData.setCounterpartyAddress(getCounterpartyHtlcAddress(selfPath, account1));

                htlcTaskDataMap.put(selfPath, htlcTaskData);
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

    private String getCounterpartyHtlcAddress(String iPath, String accountName) throws Exception {
        com.webank.wecross.resource.Resource resource = zoneManager.getResource(Path.decode(iPath));
        Account account = accountManager.getAccount(accountName);
        return getCounterpartyHtlcAddress(resource, account);
    }

    private String getCounterpartyHtlcAddress(
            com.webank.wecross.resource.Resource resource, Account account) throws Exception {
        HTLC htlc = new AssetHTLC();

        CompletableFuture<String> future = new CompletableFuture<>();
        htlc.getCounterpartyHtlcAddress(
                resource,
                account,
                (exception, address) -> {
                    if (exception != null) {
                        logger.error(
                                "Failed to get counterparty htlc address, errorMessage: {}, internalMessage: {}",
                                exception.getLocalizedMessage(),
                                exception.getInternalMessage());
                        future.complete(null);
                    } else {
                        if (address == null || RoutineDefault.NULL_FLAG.equals(address)) {
                            logger.error("Counterparty htlc address has not set.");
                            address = null;
                        }
                        if (!future.isCancelled()) {
                            future.complete(address);
                        }
                    }
                });

        String result;
        try {
            result = future.get(RoutineDefault.CALLBACK_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            result = null;
        }

        if (result == null) {
            throw (new Exception("GET_COUNTERPARTY_HTLC_ADDRESS_ERROR"));
        }
        return result;
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
