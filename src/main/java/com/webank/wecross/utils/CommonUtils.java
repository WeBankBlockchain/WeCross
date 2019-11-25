package com.webank.wecross.utils;

import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import java.util.ArrayList;

public class CommonUtils {

    public static TypeEnum getTypeEnum(Object obj) throws WeCrossException {
        Class thisClass = obj.getClass();
        if (thisClass.equals(Integer.class) || thisClass.equals(int.class)) {
            return TypeEnum.Int;
        } else if (thisClass.equals(String.class)) {
            return TypeEnum.String;
        } else if (thisClass.equals(int[].class) || thisClass.equals(Integer[].class)) {
            return TypeEnum.IntArray;
        } else if (thisClass.equals(String[].class)) {
            return TypeEnum.StringArray;
        } else if (thisClass.equals(ArrayList.class)) {
            try {
                // An exception will be thrown since obj's type is not IntArray
                ArrayList<Integer> tempList = (ArrayList<Integer>) obj;
                if (tempList.size() == 0) {
                    return TypeEnum.IntArray;
                }
                Integer item = tempList.get(0);
                return TypeEnum.IntArray;
            } catch (Exception e1) {
                try {
                    // An exception will be thrown since obj's type is not StringArray
                    ArrayList<String> tempList = (ArrayList<String>) obj;
                    if (tempList.size() == 0) {
                        return TypeEnum.StringArray;
                    }
                    String item = tempList.get(0);
                    return TypeEnum.StringArray;
                } catch (Exception e2) {
                    throw new WeCrossException(
                            Status.UNSUPPORTED_TYPE, "Unsupported type :" + thisClass.getName());
                }
            }
        } else {
            throw new WeCrossException(
                    Status.UNSUPPORTED_TYPE, "Unsupported type :" + thisClass.getName());
        }
    }
}
