package com.webank.wecross.common;

public class WeCrossDefault {
    public static final String TEMPLATE_URL = "http://127.0.0.1:8080/";

    // Config
    public static final String MAIN_CONFIG_FILE = "classpath:wecross.toml";
    public static final String STUB_CONFIG_FILE = "stub.toml";

    // size for list method
    public static final int MAX_SIZE_FOR_LIST = 1024;

    // User Context
    public static final String EMPTY_TOKEN = "_wecross_local_account_";
}
