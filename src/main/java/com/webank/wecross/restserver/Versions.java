package com.webank.wecross.restserver;

public class Versions {

    private static final String versionList[] = {
        "0.0", // no longer supported
        "0.1", "0.2", // current version
    };

    private static String oldestVersion = "0.1";

    public static Boolean checkVersion(String version) {
        Integer index = getIndex(oldestVersion);
        for (; index < versionList.length; ++index) {
            if (versionList[index].equals(version)) {
                return true;
            }
        }
        return false;
    }

    private static Integer getIndex(String version) {
        for (Integer index = 0; index < versionList.length; ++index) {
            if (versionList[index].equals(version)) {
                return index;
            }
        }
        return -1;
    }
}
