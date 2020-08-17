package com.webank.wecross.network.rpc;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Account;
import java.util.Objects;

public class RequestUtils {
    public static void checkAccountAndResource(Account account, Resource resource)
            throws WeCrossException {
        if (Objects.isNull(account)) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.ACCOUNT_ERROR, "Account not found");
        }

        if (Objects.isNull(resource)) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.RESOURCE_ERROR, "Resource not found");
        }

        if (!account.getType().equals(resource.getStubType())) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.ACCOUNT_ERROR,
                    "Account type '"
                            + account.getType()
                            + "' does not match the stub type '"
                            + resource.getStubType()
                            + "'");
        }
    }
}
