package com.webank.wecross.account;

import com.webank.wecross.stub.Account;
import java.util.HashMap;
import java.util.Map;

public class LocalAccountManager implements AccountManager {
    private Map<String, Account> accounts = new HashMap<String, Account>();

    public Map<String, Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Map<String, Account> accounts) {
        this.accounts = accounts;
    }

    public void addAccount(String name, Account account) {
        accounts.put(name, account);
    }

    public Account getAccount(String name) {
        return accounts.get(name);
    }
}
