package com.webank.wecross.account;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.utils.ConfigUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountsFactory {
    public Logger logger = LoggerFactory.getLogger(AccountsFactory.class);

    private static final String TYPE_BCOS = "BCOS";
    private static final String TYPE_BCOS_GM = "BCOS_SMCRYPTO";
    private static final String TYPE_FABRIC = "Fabric";

    public Accounts build() throws Exception {
        Accounts accounts = new Accounts(buildAccountsMap());
        return accounts;
    }

    private Map<String, Account> buildAccountsMap() throws Exception {
        Map<String, Account> accountsMap = new HashMap<>();
        try {
            Toml config = ConfigUtils.getToml(WeCrossDefault.ACCOUNTS_CONFIG_FILE);
            List<Map<String, String>> accountConfigList = config.getList("accounts");
            for (Map<String, String> accountConfig : accountConfigList) {
                String name = accountConfig.get("name");
                String type = accountConfig.get("type");

                if (accountsMap.containsKey(name)) {
                    throw new Exception("Duplicate account name: " + name);
                }

                Account account = buildAccount(name, type);
                accountsMap.put(name, account);
            }
        } catch (Exception e) {
            String errorMsg = "Load accounts exception: " + e.getMessage();
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }
        return accountsMap;
    }

    private Account buildAccount(String name, String type) throws Exception {
        if (name == null) {
            throw new Exception(
                    "\"name\" field has not configured in " + WeCrossDefault.ACCOUNTS_CONFIG_FILE);
        }
        if (type == null) {
            throw new Exception(
                    "\"type\" field has not configured in " + WeCrossDefault.ACCOUNTS_CONFIG_FILE);
        }

        String accountPath = WeCrossDefault.ACCOUNTS_BASE + name;
        Account account;
        switch (type) {
            case TYPE_BCOS:
                BCOSAccountFactory bcosAccountFactory = new BCOSAccountFactory();
                account = bcosAccountFactory.build(name, accountPath);
                break;
            case TYPE_BCOS_GM:
                BCOSAccountFactory bcosAccountFactoryGM = new BCOSAccountFactory();
                account = bcosAccountFactoryGM.buildGM(name, accountPath);
                break;
            case TYPE_FABRIC:
                FabricAccountFactory fabricAccountFactory = new FabricAccountFactory();
                account = fabricAccountFactory.build(name, accountPath);
                break;
            default:
                throw new Exception("Unrecognized account type: " + type);
        }
        return account;
    }
}
