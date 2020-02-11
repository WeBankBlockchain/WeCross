package com.webank.wecross.common;

public class WeCrossDefault {

    public static final String MAIN_CONFIG_FILE = "classpath:wecross.toml";
    public static final String MAIN_CONFIG_TEST_FILE = "classpath:data/test.toml";
    public static final String STUB_CONFIG_FILE = "stub.toml";
    public static final String TEMPLATE_URL = "http://127.0.0.1:8080/";
    public static final Integer DEFAULT_TIME_OUT = 60 * 1000;
    public static final long DEFAULT_PROPOSAL_WAIT_TIME = 120000; // ms
}
