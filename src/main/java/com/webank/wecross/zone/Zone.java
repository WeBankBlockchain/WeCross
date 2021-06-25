package com.webank.wecross.zone;

import com.webank.wecross.account.AccountAccessControlFilter;
import com.webank.wecross.stub.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Zone {

    private Logger logger = LoggerFactory.getLogger(Zone.class);

    private Map<String, Chain> chains = new LinkedHashMap<>(); // need order output

    // Access control
    private Boolean visible;

    public Chain getChain(Path path) {
        return getChain(path.getChain());
    }

    public Chain getChain(String name) {
        Chain stub = chains.get(name);
        return stub;
    }

    public boolean isEmpty() {
        return getChains() == null || getChains().isEmpty();
    }

    public Map<String, Chain> getChains() {
        return chains;
    }

    public Map<String, Chain> getChainsWithFilter(AccountAccessControlFilter filter) {
        Map<String, Chain> filteredChains = new HashMap<>();
        for (Map.Entry<String, Chain> entry : chains.entrySet()) {
            Path path = new Path();
            path.setZone(entry.getValue().getZoneName());
            path.setChain(entry.getValue().getName());
            logger.info("Check path: {} with filter:{}", path.toString(), filter.toString());
            if (filter.hasPermission(path)) {
                logger.info("Add filtered chain path:{}", path.toString());
                filteredChains.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredChains;
    }

    public void setChains(Map<String, Chain> stubs) {
        this.chains = stubs;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
