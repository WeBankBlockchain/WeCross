package com.webank.wecross.network.rpc.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.restserver.response.AccountResponse;
import com.webank.wecross.stub.Account;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** GET/POST /listAccounts */
public class ListAccountsURIHandler implements URIHandler {
    private static final Logger logger = LoggerFactory.getLogger(ListAccountsURIHandler.class);

    private WeCrossHost host;
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    public ListAccountsURIHandler(WeCrossHost host) {
        this.host = host;
    }

    public WeCrossHost getHost() {
        return host;
    }

    public void setHost(WeCrossHost host) {
        this.host = host;
    }

    @Override
    public void handle(String uri, String method, String content, Callback callback) {

        RestResponse<AccountResponse> restResponse = new RestResponse<>();
        if (logger.isDebugEnabled()) {
            logger.debug(" request string: {}", content);
        }

        try {
            AccountManager accountManager = host.getAccountManager();
            RestRequest restRequest =
                    objectMapper.readValue(content, new TypeReference<RestRequest>() {});
            restRequest.checkRestRequest("", "listAccounts");
            Map<String, Account> accounts = accountManager.getAccounts();
            List<Map<String, String>> accountInfos = new ArrayList<>();
            for (Account account : accounts.values()) {
                Map<String, String> accountInfo = new HashMap<>();
                accountInfo.put("name", account.getName());
                accountInfo.put("type", account.getType());

                accountInfos.add(accountInfo);
            }

            AccountResponse accountResponse = new AccountResponse();
            accountResponse.setAccountInfos(accountInfos);
            restResponse.setData(accountResponse);
        } catch (WeCrossException e) {
            logger.warn("Process request error", e);
            restResponse.setErrorCode(NetworkQueryStatus.EXCEPTION_FLAG + e.getErrorCode());
            restResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error", e);
            restResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
            restResponse.setMessage(e.getMessage());
        }

        callback.onResponse(restResponse);
    }
}
