package com.webank.wecross.common;

import java.io.File;

public class WeCrossDefault {
    public static final String TEMPLATE_URL = "http://127.0.0.1:8080/";

    // Account
    public static final String ACCOUNTS_BASE = "classpath:accounts" + File.separator;
    public static final String ACCOUNTS_CONFIG_NAME = "accounts.toml";
    public static final String ACCOUNTS_CONFIG_FILE = ACCOUNTS_BASE + ACCOUNTS_CONFIG_NAME;

    // Config
    public static final String MAIN_CONFIG_FILE = "classpath:wecross.toml";
    public static final String STUB_CONFIG_FILE = "chain.toml";
}
