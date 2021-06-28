package com.webank.wecross.test.account;

import com.webank.wecross.account.AccountAccessControlFilter;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class AccountAccessControlFilterTest {
    // private String[] accountAllowPaths = new String[] {"a.b.c", "a.b", "a", "d.e", "f.g.h"};
    private String[] accountAllowPaths = new String[] {"a.b.c", "a.b", "d.e", "f.g.h"};

    @Test
    public void hasPermissionTest() throws Exception {

        AccountAccessControlFilter filter = new AccountAccessControlFilter(accountAllowPaths);

        // Assert.assertTrue(filter.hasPermission("a") == true);
        Assert.assertTrue(filter.hasPermission("a.*") == false);
        Assert.assertTrue(filter.hasPermission("a.b") == true);
        Assert.assertTrue(filter.hasPermission("a.b.*") == true);
        Assert.assertTrue(filter.hasPermission("a.b.c") == true);
        Assert.assertTrue(filter.hasPermission("a.b.d") == true);
        // Assert.assertTrue(filter.hasPermission("d") == false);
        Assert.assertTrue(filter.hasPermission("d.e") == true);
        Assert.assertTrue(filter.hasPermission("d.e.*") == true);
        Assert.assertTrue(filter.hasPermission("f.g") == false);
        Assert.assertTrue(filter.hasPermission("f.g.h") == true);
        Assert.assertTrue(filter.hasPermission("i.j") == false);
        Assert.assertTrue(filter.hasPermission("i.j.*") == false);
    }

    @Test
    public void dumpTest() throws Exception {
        String[] allowPaths = new AccountAccessControlFilter(accountAllowPaths).dumpPermission();
        System.out.println(Arrays.toString(allowPaths));
        AccountAccessControlFilter filter = new AccountAccessControlFilter(allowPaths);

        // Assert.assertTrue(filter.hasPermission("a") == true);
        Assert.assertTrue(filter.hasPermission("a.*") == false);
        Assert.assertTrue(filter.hasPermission("a.b") == true);
        Assert.assertTrue(filter.hasPermission("a.b.*") == true);
        Assert.assertTrue(filter.hasPermission("a.b.c") == true);
        Assert.assertTrue(filter.hasPermission("a.b.d") == true);
        // Assert.assertTrue(filter.hasPermission("d") == false);
        Assert.assertTrue(filter.hasPermission("d.e") == true);
        Assert.assertTrue(filter.hasPermission("d.e.*") == true);
        Assert.assertTrue(filter.hasPermission("f.g") == false);
        Assert.assertTrue(filter.hasPermission("f.g.h") == true);
        Assert.assertTrue(filter.hasPermission("i.j") == false);
        Assert.assertTrue(filter.hasPermission("i.j.*") == false);
    }
}
