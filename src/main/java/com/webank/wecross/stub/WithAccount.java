package com.webank.wecross.stub;

import com.webank.wecross.account.Account;

public class WithAccount<T> {
    private Account account;
    private T data;

    public WithAccount(T data, Account account) {
        this.data = data;
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
