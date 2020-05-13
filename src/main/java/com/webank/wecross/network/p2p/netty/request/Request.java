package com.webank.wecross.network.p2p.netty.request;

public class Request {
    private Short type;
    private String content;
    /** timeout, default 60s */
    private Integer timeout = 60 * 1000;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }
}
