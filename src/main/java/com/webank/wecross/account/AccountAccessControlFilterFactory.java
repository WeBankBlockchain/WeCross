package com.webank.wecross.account;

import com.webank.wecross.exception.WeCrossException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountAccessControlFilterFactory {
    private static Logger logger = LoggerFactory.getLogger(AccountAccessControlFilterFactory.class);

    private boolean enableAccessControl;

    public AccountAccessControlFilter buildFilter(
            String username, boolean isAdmin, String[] accountAllowPaths) throws WeCrossException {
        if (enableAccessControl && !isAdmin) {
            AccountAccessControlFilter filter = new AccountAccessControlFilter(accountAllowPaths);
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Build account access control filter, account:{} filter:{}",
                        username,
                        Arrays.toString(filter.dumpPermission()));
            }
            return filter;
        } else {
            return new DisabledAccountAccessControlFilter(accountAllowPaths);
        }
    }

    public void setEnableAccessControl(boolean enableAccessControl) {
        this.enableAccessControl = enableAccessControl;
    }
}
