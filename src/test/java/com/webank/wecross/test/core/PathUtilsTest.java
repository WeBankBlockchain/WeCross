package com.webank.wecross.test.core;

import com.webank.wecross.utils.core.PathUtils;
import org.junit.Assert;
import org.junit.Test;

public class PathUtilsTest {
    @Test
    public void toPureNameTest() {
        Assert.assertTrue(PathUtils.toPureName(".a\\b\\c/d/.e.f").equals("abcdef"));
    }
}
