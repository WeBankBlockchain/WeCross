package com.webank.wecross.core;

import java.security.SecureRandom;

public class SeqUtil {
    static final int SEQ_BOUND = Integer.MAX_VALUE - 1;

    public static int newSeq() {
        SecureRandom rand = new SecureRandom();
        return rand.nextInt(SEQ_BOUND);
    }
}
