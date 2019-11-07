package com.webank.wecross.p2p.netty.message;

/** message type for p2p message communication with each other */
public interface MessageType {
    /** heart beat message */
    Short HEARTBEAT = 0x01;
    /** resource remote request */
    Short RESOURCE_REQUEST = 0x10;
    /** resource remote request */
    Short RESOURCE_RESPONSE = 0x11;
}
