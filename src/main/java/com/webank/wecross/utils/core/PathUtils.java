package com.webank.wecross.utils.core;

public class PathUtils {
    public static String toPureName(String name) {
        // delete "\" "/" "." from name
        return name.replace("\\", "").replace("/", "").replace(".", "");
    }
}
