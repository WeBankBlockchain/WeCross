package com.webank.wecross.account;

import static com.webank.wecross.exception.WeCrossException.ErrorCode.GET_UA_FAILED;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.client.ClientMessageEngine;
import com.webank.wecross.network.client.Request;
import com.webank.wecross.network.client.Response;
import com.webank.wecross.stub.Account;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class AccountManager {
    private Logger logger = LoggerFactory.getLogger(AccountManager.class);

    private ClientMessageEngine engine;
    private UniversalAccountFactory universalAccountFactory;

    private AdminContext adminContext;
    private AccountSyncManager accountSyncManager;

    private Map<String, UniversalAccount> token2UA = new ConcurrentHashMap<>();

    public Account getAccount(String name) {
        String message =
                "Please delete this function and use getUniversalAccount(context).getAccount(type)";
        logger.error(message);
        Assert.isTrue(false, message);
        return null;
    }

    public List<Account> getAccounts(UserContext userContext) throws WeCrossException {
        String token = userContext.getToken();
        if (token == null) {
            return new LinkedList<>();
        }

        UniversalAccount ua = getUniversalAccount(userContext);
        return ua.getAccounts();
    }

    public UniversalAccount getUniversalAccount(UserContext userContext) throws WeCrossException {

        String token = userContext.getToken();
        if (token == null) {
            return null;
        }

        if (!token2UA.containsKey(token)) {
            UniversalAccount newUA = fetchUA(token); // from WeCross-Account-Manager

            if (newUA != null) {
                token2UA.put(token, newUA);
            } else {
                throw new WeCrossException(GET_UA_FAILED, "UA is not exist or login expired");
            }
        }

        UniversalAccount ua = token2UA.get(token);

        // query login state every x seconds
        if (!((UniversalAccount) ua).isActive()) {
            if (!fetchHasLoginStatus(token)) {
                token2UA.remove(token);
                ua = null;
                throw new WeCrossException(GET_UA_FAILED, "login expired");
            } else {
                ((UniversalAccount) ua).activate();
            }
        }

        return ua;
    }

    public void setUniversalAccountFactory(UniversalAccountFactory universalAccountFactory) {
        this.universalAccountFactory = universalAccountFactory;
    }

    public void setAccountSyncManager(AccountSyncManager accountSyncManager) {
        this.accountSyncManager = accountSyncManager;
    }

    public static class GetUniversalAccountByChainAccountIdentityRequest {
        public String identity;
    }

    public UniversalAccount getUniversalAccountByIdentity(String accountIdentity) {
        String token = adminContext.getToken(); // only admin can query

        Request<Object> request = new Request();
        GetUniversalAccountByChainAccountIdentityRequest
                getUniversalAccountByChainAccountIdentityRequest =
                        new GetUniversalAccountByChainAccountIdentityRequest();
        getUniversalAccountByChainAccountIdentityRequest.identity = accountIdentity;
        request.setData(getUniversalAccountByChainAccountIdentityRequest);
        request.setMethod("/auth/getUniversalAccountByChainAccountIdentity");
        request.setAuth(token);

        try {
            // TODO: cache the response
            Response<UADetails> response =
                    engine.send(request, new TypeReference<Response<UADetails>>() {});

            if (response.getErrorCode() != 0) {
                throw new WeCrossException(
                        WeCrossException.ErrorCode.GET_UA_BY_ACCOUNT_FAILED, response.getMessage());
            }

            return universalAccountFactory.buildUA(response.getData());

        } catch (Exception e) {
            logger.error("get ua by identity failed: " + e.getMessage());
            return null;
        }
    }

    public void setAdminContext(AdminContext adminContext) {
        this.adminContext = adminContext;
    }

    private boolean fetchHasLoginStatus(String token) {
        Request<Object> request = new Request();
        request.setData(null);
        request.setMethod("/auth/hasLogin");
        request.setAuth(token);

        try {
            Response<?> response = engine.send(request, new TypeReference<Response<?>>() {});

            if (response.getErrorCode() != 0) {
                return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private UniversalAccount fetchUA(String token) {

        Request<Object> request = new Request();
        request.setData(null);
        request.setMethod("/auth/getUniversalAccount");
        request.setAuth(token);

        try {

            Response<UADetails> response =
                    engine.send(request, new TypeReference<Response<UADetails>>() {});

            if (response.getErrorCode() != 0) {
                throw new WeCrossException(GET_UA_FAILED, response.getMessage());
            }

            UniversalAccount ua = universalAccountFactory.buildUA(response.getData());

            accountSyncManager.onNewUA(ua);
            return ua;

        } catch (Exception e) {
            logger.error("FetchUA failed: ", e);
            return null;
        }
    }

    public UniversalAccount getAdminUA() throws WeCrossException {
        UniversalAccount ua = null;
        try {
            ua = getUniversalAccount(adminContext);
        } catch (WeCrossException e) {
            logger.debug("admin login expired, try re-login");
            adminContext.reLogin();
            ua = getUniversalAccount(adminContext);
        }
        return ua;
    }

    public void setEngine(ClientMessageEngine engine) {
        this.engine = engine;
    }
}
