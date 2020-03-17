package com.webank.wecross.stub;

import com.webank.wecross.account.Account;

public class TransactionContext<T> {
    private T data;
    private Account account;
    private Path path;

    public TransactionContext(T data, Account account, Path path) {
        this.data = data;
        this.account = account;
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
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
