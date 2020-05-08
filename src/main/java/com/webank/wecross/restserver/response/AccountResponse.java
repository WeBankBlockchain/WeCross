package com.webank.wecross.restserver.response;

import java.util.List;
import java.util.Map;

public class AccountResponse {
    private List<Map<String, String>> accountInfos;

    public List<Map<String, String>> getAccountInfos() {
        return accountInfos;
    }

    public void setAccountInfos(List<Map<String, String>> accountInfos) {
        this.accountInfos = accountInfos;
    }
}
