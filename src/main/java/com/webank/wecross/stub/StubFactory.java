package com.webank.wecross.stub;

import java.util.Map;

public interface StubFactory {
    public String version = "1.0.0";

    /**
     * init a stub
     *
     * @param context
     */
    public void init(WeCrossContext context);

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

    /**
     * use sec and cert to new account
     *
     * @param properties
     * @return
     */
    public Account newAccount(Map<String, Object> properties);

    /**
     * generate account
     *
     * @param path
     * @param args
     */
    public void generateAccount(String path, String[] args);

    /**
     * generate stub
     *
     * @param path
     * @param args
     */
    public void generateConnection(String path, String[] args);
}
