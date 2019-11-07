package com.webank.wecross.p2p.netty.response;

public interface StatusCode {
    int SUCCESS = 0;
    int UNREACHABLE = 101;
    int TIMEOUT = 102;
}
