package com.webank.wecross.interchain;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.polling.Task;
import com.webank.wecross.polling.TaskManager;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.StubConstant;
import com.webank.wecross.zone.ChainInfo;
import com.webank.wecross.zone.ZoneManager;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterchainManager {
    private Logger logger = LoggerFactory.getLogger(InterchainManager.class);

    private AccountManager accountManager;
    private ZoneManager zoneManager;

    public void registerTask(TaskManager taskManager) {
        if (Objects.isNull(accountManager) || Objects.isNull(zoneManager)) {
            logger.error("InterchainManager has not been initialized");
            return;
        }

        InterchainTaskFactory interchainTaskFactory = new InterchainTaskFactory();
        SystemResource[] systemResources = initSystemResources();
        if (Objects.nonNull(systemResources) && systemResources.length > 0) {
            Task[] tasks =
                    interchainTaskFactory.load(
                            systemResources,
                            InterchainDefault.INTER_CHAIN_JOB_DATA_KEY,
                            InterchainJob.class);
            taskManager.registerTasks(tasks);
        }
    }

    private SystemResource[] initSystemResources() {
        Map<String, ChainInfo> chainInfoMap = zoneManager.getAllChainsInfo(true);
        Set<String> chainPaths = chainInfoMap.keySet();
        if (chainPaths.isEmpty()) {
            return null;
        }

        SystemResource[] systemResources = new SystemResource[chainPaths.size()];
        int num = 0;
        for (String chaiPath : chainPaths) {
            try {
                Path path = Path.decode(chaiPath);
                path.setResource(StubConstant.PROXY_NAME);
                Path proxyPath = new Path(path);
                Resource proxyResource = zoneManager.fetchResource(proxyPath);

                path.setResource(StubConstant.HUB_NAME);
                Path hubPath = new Path(path);
                Resource hubResource = zoneManager.fetchResource(hubPath);

                systemResources[num++] =
                        new SystemResource(accountManager, zoneManager, proxyResource, hubResource);
            } catch (Exception e) {
                logger.error("Decode chain path {} failed: ", chaiPath, e);
            }
        }
        return systemResources;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public void setZoneManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }
}
