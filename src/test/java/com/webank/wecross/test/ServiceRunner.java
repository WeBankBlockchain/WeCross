package com.webank.wecross.test;

import com.webank.wecross.Service;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.utils.CommonUtils;

public class ServiceRunner {

    public static void main(String[] args) throws WeCrossException {
        String[] a = {"a", "b"};
        Integer[] b = {1, 2};
        String a1 = "a";
        int b1 = 2;
        Object[] c = {a, b, a1, b1};
        System.out.println(c[0].getClass());
        System.out.println(c[1].getClass());
        System.out.println(c[2].getClass());
        System.out.println(c[3].getClass());
        System.out.println(CommonUtils.getTypeEnum(c[0]));
        System.out.println(CommonUtils.getTypeEnum(c[1]));
        System.out.println(CommonUtils.getTypeEnum(c[2]));
        System.out.println(CommonUtils.getTypeEnum(c[3]));
        Service.main(args);
    }
}
