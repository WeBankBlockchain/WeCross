package com.webank.wecross.config;

public class ConfigInfo {

    // config file
    public static final String MAIN_CONFIG_FILE = "classpath:wecross.toml";
    public static final String STUB_CONFIG_FILE = "stub.toml";
    public static final String TEMPLATE_URL = "http://127.0.0.1:8080/";
    public static final Integer DEFAULT_TIME_OUT = 60 * 1000;

    // stub type
    public static final String STUB_TYPE_BCOS = "BCOS";
    public static final String STUB_TYPE_JDCHAIN = "JDCHAIN";
    public static final String STUB_TYPE_FABRIC = "FABRIC";
    public static final String STUB_TYPE_REMOTE = "REMOTE";

    // resource type
    public static final String RESOURCE_TYPE_BCOS_CONTRACT = "BCOS_CONTRACT";
    public static final String RESOURCE_TYPE_JDCHAIN_CONTRACT = "JDCHAIN_CONTRACT";
    public static final String RESOURCE_TYPE_TEST = "TEST_RESOURCE";
    public static final String RESOURCE_TYPE_FABRIC_CONTRACT = "FABRIC_CONTRACT";

    // transaction response message type
    public static final String TRANSACTION_RSP_TYPE_NORMAL = "NORMAL";
    public static final String TRANSACTION_RSP_TYPE_BCOS = TRANSACTION_RSP_TYPE_NORMAL;
    public static final String TRANSACTION_RSP_TYPE_JDCHAIN = "JDCHAIN";
    public static final String TRANSACTION_RSP_TYPE_FABRIC = "FABRIC";
}
