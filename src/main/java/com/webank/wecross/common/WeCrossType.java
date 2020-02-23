package com.webank.wecross.common;

public class WeCrossType {

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
    public static final String RESOURCE_TYPE_HTLC_CONTRACT = "HTLC_CONTRACT";

    // transaction response message type
    public static final String TRANSACTION_RSP_TYPE_NORMAL = "NORMAL";
    public static final String TRANSACTION_RSP_TYPE_BCOS = TRANSACTION_RSP_TYPE_NORMAL;
    public static final String TRANSACTION_RSP_TYPE_JDCHAIN = "JDCHAIN";
    public static final String TRANSACTION_RSP_TYPE_FABRIC = "FABRIC";

    // encrypt type
    public static final String ENCRYPT_TYPE_NORMAL = "NORMAL";
    public static final String ENCRYPT_TYPE_GUOMI = "GUOMI";

    // crypto suite of proposal bytes to sign
    public static final String BCOS_SHA3_256_SECP256K1 = "BCOS_SHA3_256_SECP256K1";
    public static final String BCOS_SM2_SM3 = "BCOS_SM2_SM3";
    public static final String BC_SECP256R1 = "BC_SECP256R1";
}
