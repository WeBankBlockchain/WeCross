package com.webank.wecross.account;

import java.util.HashMap;
import java.util.Map;

public class AccountManager {
    private Map<String, Account> accounts = new HashMap<String, Account>();

    public void addAccount(String name, Account account) {
        accounts.put(name, account);
    }

    public Account getAccount(String name) {
        return accounts.get(name);
    }
}
