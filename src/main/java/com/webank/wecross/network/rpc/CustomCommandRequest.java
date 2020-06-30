package com.webank.wecross.network.rpc;

import java.util.List;

public class CustomCommandRequest {
    private String Command;
    private List<Object> args;

    public String getCommand() {
        return Command;
    }

    public void setCommand(String command) {
        Command = command;
    }

    public List<Object> getArgs() {
        return args;
    }

    public void setArgs(List<Object> args) {
        this.args = args;
    }
}
