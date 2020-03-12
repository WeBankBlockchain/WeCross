package com.webank.wecross.account;

import java.util.Map;

public class Accounts {
    private Map<String, Account> accountsMap;

    public Accounts(Map<String, Account> accountsMap) {
        this.accountsMap = accountsMap;
    }

    public Account getAccount(String name) throws Exception {
        if (!accountsMap.containsKey(name)) {
            throw new Exception("Account " + name + " does not exists");
        }
        return accountsMap.get(name);
    }

    public int size() {
        return accountsMap.size();
    }
}
