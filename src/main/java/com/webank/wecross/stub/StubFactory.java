package com.webank.wecross.stub;

import com.webank.wecross.account.Account;

public interface StubFactory {
    /**
     * create a driver
     *
     * @return
     */
    public Driver newDriver();

    /**
     * create a connection
     *
     * @return Connection
     */
    public Connection newConnection(String path);

    /** load account */
    public Account newAccount(String name, String path);
}
