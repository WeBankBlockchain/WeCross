package com.webank.wecross.test.core;

import com.webank.wecross.utils.core.SeqUtils;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class SeqUtilsTest {
    @Test
    public void newSeqTest() {
        Set<Integer> integerSet = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            Integer seq = SeqUtils.newSeq();
            Assert.assertNotEquals(0, seq.intValue());
            Assert.assertFalse(integerSet.contains(seq));
            integerSet.add(seq);
        }
    }
}
