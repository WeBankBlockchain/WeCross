package com.webank.wecross.restserver;

public class Versions {

    private static final String versionList[] = {"0.1", "0.2", "1"};

    public static String currentVersion = "1";

    public static Boolean checkVersion(String version) {
        for (String v : versionList) {
            if (v.equals(version)) {
                return true;
            }
        }
        return false;
    }
}
