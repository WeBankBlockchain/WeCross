package com.webank.wecross.network.p2p.netty.factory;

import com.webank.wecross.network.p2p.netty.SeqMapper;

public class SeqMapperFactory {
    public static SeqMapper build() {
        return new SeqMapper();
    }
}
