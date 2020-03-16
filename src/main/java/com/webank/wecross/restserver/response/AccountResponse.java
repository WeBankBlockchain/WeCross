package com.webank.wecross.restserver.response;

import com.webank.wecross.account.Account;
import com.webank.wecross.account.AccountManager;
import java.util.Map;

public class AccountResponse {

    AccountInfo[] accountInfos;

    static class AccountInfo {
        String name;
        String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public AccountInfo[] getAccountInfos() {
        return accountInfos;
    }

    public void setAccountInfos(AccountInfo[] accountInfos) {
        this.accountInfos = accountInfos;
    }

    public void setAccountInfos(AccountManager accountManager) {
        Map<String, com.webank.wecross.account.Account> accounts = accountManager.getAccounts();
        AccountInfo[] accountInfos = new AccountInfo[accounts.size()];
        int i = 0;
        for (Account account : accounts.values()) {
            accountInfos[i].setName(account.getName());
            accountInfos[i++].setType(account.getType());
        }
    }
}
