package com.webank.wecross.common;

import java.util.Arrays;
import java.util.List;

public class WeCrossDefault {
    public static final String VERSION = "v1.3.1";

    public static final String TEMPLATE_URL = "http://127.0.0.1:8080/";

    // Config
    public static final String MAIN_CONFIG_FILE = "classpath:wecross.toml";
    public static final String STUB_CONFIG_FILE = "stub.toml";
    public static final String BLOCK_VERIFIER_CONFIG_FILE = "classpath:verifier.toml";

    // size for list method
    public static final int MAX_SIZE_FOR_LIST = 1024;

    // User Context
    public static final String EMPTY_TOKEN = "_wecross_local_account_";

    // Block Verifier Config
    public static final List<String> SUPPORTED_STUBS =
            Arrays.asList(
                    "BCOS3_ECDSA_EVM",
                    "BCOS3_GM_EVM",
                    "BCOS2.0",
                    "GM_BCOS2.0",
                    "Fabric1.4",
                    "Fabric2.0");
}
