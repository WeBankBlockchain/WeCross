package com.webank.wecross.p2p;

import java.security.SecureRandom;

public class P2PMessage<T> {
    static final int SEQ_BOUND = Integer.MAX_VALUE - 1;

    private T data;
    private int seq;

    public P2PMessage(T data) {
        this.data = data;
        this.seq = this.newSeq();
    }

    public static int newSeq() {
        SecureRandom rand = new SecureRandom();
        return rand.nextInt(SEQ_BOUND);
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
