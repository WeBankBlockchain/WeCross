package com.webank.wecross.account;

import com.webank.wecross.stub.Account;
import java.util.Map;

public interface AccountManager {

    Map<String, Account> getAccounts();

    void setAccounts(Map<String, Account> accounts);

    void addAccount(String name, Account account);

    Account getAccount(String name);
}
