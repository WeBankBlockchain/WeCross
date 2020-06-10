package com.webank.wecross.stub;

public interface WeCrossContext {
    public void registerCommand(String command, String description);

    public void unregisterCommand(String command);
}
