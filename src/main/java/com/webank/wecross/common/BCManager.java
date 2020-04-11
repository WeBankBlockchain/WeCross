package com.webank.wecross.common;

import java.security.Provider;

public class BCManager {

    private Provider bouncyCastleProvider;

    public Provider getBouncyCastleProvider() {
        return bouncyCastleProvider;
    }

    public void setBouncyCastleProvider(Provider bouncyCastleProvider) {
        this.bouncyCastleProvider = bouncyCastleProvider;
    }
}
