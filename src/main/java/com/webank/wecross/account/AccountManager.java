package com.webank.wecross.account;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.client.ClientMessageEngine;
import com.webank.wecross.network.client.Request;
import com.webank.wecross.network.client.Response;
import com.webank.wecross.stub.Account;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class AccountManager {
    private Logger logger = LoggerFactory.getLogger(AccountManager.class);

    private ClientMessageEngine engine;
    private UniversalAccountFactory universalAccountFactory;

    private AdminContext adminContext;

    private Map<JwtToken, UniversalAccount> token2UA;

    public Account getAccount(String name) {
        String message =
                "Please delete this function and use getUniversalAccount(context).getAccount(type)";
        logger.error(message);
        Assert.isTrue(false, message);
        return null;
    }

    public List<Account> getAccounts(UserContext userContext) {
        JwtToken token = userContext.getToken();
        if (token == null) {
            return new LinkedList<>();
        }

        UniversalAccount ua = getUniversalAccount(userContext);
        return ua.getAccounts();
    }

    public UniversalAccount getUniversalAccount(UserContext userContext) {

        JwtToken token = userContext.getToken();
        if (token == null) {
            return null;
        }

        if (token.hasExpired() || !token2UA.containsKey(token)) {
            UniversalAccount newUA = fetchUA(token); // from WeCross-Account-Manager
            if (newUA != null) {
                token2UA.put(token, newUA);
            }
        }

        return token2UA.get(token);
    }

    public void setUniversalAccountFactory(UniversalAccountFactory universalAccountFactory) {
        this.universalAccountFactory = universalAccountFactory;
    }

    @Data
    @Builder
    public static class GetUniversalAccountByChainAccountIdentityRequest {
        private String identity;
    }

    public UniversalAccount getUniversalAccountByAccount(String accountIdentity) {
        JwtToken token = adminContext.getToken(); // only admin can query

        Request<Object> request = new Request();
        request.setData(
                GetUniversalAccountByChainAccountIdentityRequest.builder()
                        .identity(accountIdentity)
                        .build());
        request.setMethod("/auth/getUniversalAccountByChainAccountIdentity");
        request.setAuth(token.getTokenStrWithPrefix());

        try {
            // TODO: cache the response
            Response<UniversalAccount.UADetails> response =
                    engine.send(
                            request, new TypeReference<Response<UniversalAccount.UADetails>>() {});

            if (response.getErrorCode() != 0) {
                throw new WeCrossException(
                        WeCrossException.ErrorCode.FETCH_UA_BY_ACCOUNT_FAILED,
                        response.getMessage());
            }

            UniversalAccount ua = universalAccountFactory.buildUA(response.getData());

            return ua;

        } catch (Exception e) {
            logger.error("get universal account by account failed: " + e.getMessage());
            return null;
        }
    }

    public void setAdminContext(AdminContext adminContext) {
        this.adminContext = adminContext;
    }

    private UniversalAccount fetchUA(JwtToken token) {

        Request<Object> request = new Request();
        request.setData(new Object());
        request.setMethod("/auth/getUniversalAccount");
        request.setAuth(token.getTokenStrWithPrefix());

        try {

            Response<UniversalAccount.UADetails> response =
                    engine.send(
                            request, new TypeReference<Response<UniversalAccount.UADetails>>() {});

            if (response.getErrorCode() != 0) {
                throw new WeCrossException(
                        WeCrossException.ErrorCode.FETCH_UA_FAILED, response.getMessage());
            }

            UniversalAccount ua = universalAccountFactory.buildUA(response.getData());

            return ua;

        } catch (Exception e) {
            logger.error("FetchUA failed: " + e.getMessage());
            return null;
        }
    }

    public void setEngine(ClientMessageEngine engine) {
        this.engine = engine;
    }
}
