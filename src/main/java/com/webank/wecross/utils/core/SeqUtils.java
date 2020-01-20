package com.webank.wecross.utils.core;

import java.security.SecureRandom;

public class SeqUtils {
    static final int SEQ_BOUND = Integer.MAX_VALUE - 1;

    public static synchronized int newSeq() {
        SecureRandom rand = new SecureRandom();
        return rand.nextInt(SEQ_BOUND);
    }
}
