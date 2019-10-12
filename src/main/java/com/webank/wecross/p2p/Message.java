package com.webank.wecross.p2p;

import java.security.SecureRandom;

public class Message {
    static final int SEQ_BOUND = Integer.MAX_VALUE - 1;

    private String buffer;
    private int seq;

    public Message(String buffer) {
        this.buffer = buffer;
        this.seq = this.newSeq();
    }

    public static int newSeq() {
        SecureRandom rand = new SecureRandom();
        return rand.nextInt(SEQ_BOUND);
    }

    public int size() {
        return buffer.length();
    }

    public String getBuffer() {
        return buffer;
    }

    public void setBuffer(String buffer) {
        this.buffer = buffer;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
}
