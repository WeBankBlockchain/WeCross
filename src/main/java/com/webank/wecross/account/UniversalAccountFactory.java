package com.webank.wecross.account;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stubmanager.StubManager;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniversalAccountFactory {
    private static Logger logger = LoggerFactory.getLogger(UniversalAccountFactory.class);
    private StubManager stubManager;

    private AccountAccessControlFilterFactory filterFactory;

    public UniversalAccount buildUA(UADetails uaDetails) throws WeCrossException {
        UniversalAccount ua =
                UniversalAccount.builder()
                        .username(uaDetails.getUsername())
                        .uaID(uaDetails.getUaID())
                        .pubKey(uaDetails.getPubKey())
                        .secKey(uaDetails.getSecKey())
                        .isAdmin(uaDetails.isAdmin())
                        .lastActiveTimestamp(System.currentTimeMillis())
                        .allowChainPaths(uaDetails.getAllowChainPaths())
                        .version(uaDetails.getVersion())
                        .build();

        AccountAccessControlFilter filter =
                filterFactory.buildFilter(
                        ua.getName(), ua.isAdmin(), uaDetails.getAllowChainPaths());
        ua.setAccessControlFilter(filter);

        // foreach details, set default account into ua
        for (Map<Integer, ChainAccountDetails> chainAccountDetailsMap :
                uaDetails.getType2ChainAccountDetails().values()) {
            for (ChainAccountDetails details : chainAccountDetailsMap.values()) {

                String type = details.getType();

                Account account = stubManager.newStubAccount(type, details.toProperties());

                if (account == null) {
                    logger.error(
                            "Default account generate failed! properties:"
                                    + details.toProperties());
                    continue;
                }

                // TODO: no need to add account, just to use default account
                ua.addAccount(type, details.getKeyID(), account, details);

                if (details.getIsDefault().booleanValue()) {
                    ua.setDefaultAccount(type, account);
                }
                if (UniversalAccount.isFabricType(type)&&UniversalAccount.checkChainName(details.getFabricDefault())){
                    ua.setDefaultFabricAccount(details.getFabricDefault(), account);
                }
            }
        }

        return ua;
    }

    public void setStubManager(StubManager stubManager) {
        this.stubManager = stubManager;
    }

    public void setFilterFactory(AccountAccessControlFilterFactory filterFactory) {
        this.filterFactory = filterFactory;
    }

    public AccountAccessControlFilterFactory getFilterFactory() {
        return filterFactory;
    }
}
