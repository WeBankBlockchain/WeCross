package com.webank.wecross.account;

import static com.webank.wecross.exception.WeCrossException.ErrorCode.GET_UA_FAILED;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.client.ClientMessageEngine;
import com.webank.wecross.network.client.Request;
import com.webank.wecross.network.client.Response;
import com.webank.wecross.stub.Account;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountManager {
    private Logger logger = LoggerFactory.getLogger(AccountManager.class);

    private ClientMessageEngine engine;
    private UniversalAccountFactory universalAccountFactory;

    private AdminContext adminContext;
    private AccountSyncManager accountSyncManager;

    private static Timer timer = new Timer("checkTokenTimer");
    private static final long checkTokenStateExpires = 25 * 6 * 60 * 1000; // s, 2.5h

    private Map<String, UniversalAccount> token2UA = new ConcurrentHashMap<>();

    public void start() {
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        // check token expires
                        try {
                            Collection<String> tokens = token2UA.keySet();
                            for (String token : tokens) {
                                if (hasTokenExpired(token)) {
                                    logger.info("Remove expired token: " + token);
                                    token2UA.remove(token);
                                }
                            }
                        } catch (Exception e) {
                            logger.error("checkTokenTimer exception: ", e);
                        }
                    }
                },
                checkTokenStateExpires,
                checkTokenStateExpires);
    }

    public List<Account> getAccounts(UserContext userContext) throws WeCrossException {
        String token = userContext.getToken();
        if (token == null) {
            return new LinkedList<>();
        }

        UniversalAccount ua = getUniversalAccount(userContext);
        return ua.getAccounts();
    }

    public void checkLogin(UserContext userContext) throws WeCrossException {
        getUniversalAccount(userContext);
    }

    public UniversalAccount getUniversalAccount(UserContext userContext) throws WeCrossException {

        String token = userContext.getToken();

        if (logger.isDebugEnabled()) {
            logger.debug("token: {}", token);
        }

        if (token == null) {
            throw new WeCrossException(GET_UA_FAILED, "token is null");
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
        if (!ua.isActive()) {
            Long knownUAVersion = fetchUAVersion(token);
            if (knownUAVersion < 0) {
                token2UA.remove(token);
                ua = null;
                throw new WeCrossException(GET_UA_FAILED, "UA is not exist or login expired");
            } else {
                // check version and update if version expires
                if (ua.getVersion() < knownUAVersion) {
                    UniversalAccount newUA = fetchUA(token);

                    if (newUA == null) {
                        throw new WeCrossException(
                                GET_UA_FAILED,
                                "UA is not exist or login expired when fetch new version("
                                        + knownUAVersion
                                        + ") UA");
                    } else if (newUA.getVersion() < knownUAVersion) {
                        throw new WeCrossException(
                                GET_UA_FAILED,
                                "Exception when ua version update, fetchUA version:"
                                        + newUA.getVersion()
                                        + " known Version:"
                                        + knownUAVersion);
                    } else {
                        logger.info("Update ua to: " + newUA.toString());
                        token2UA.put(token, newUA);
                    }
                } else {
                    ua.activate();
                }
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

    public void setTimer(Timer timer) {
        this.timer = timer;
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

    /**
     * Fetch ua version from Account-Manager
     *
     * @param token user login token
     * @return The version of ua, return -1 if token login failed
     */
    private Long fetchUAVersion(String token) {
        Request<Object> request = new Request();
        request.setData(null);
        request.setMethod("/auth/getUAVersion");
        request.setAuth(token);

        try {
            Response<Long> response = engine.send(request, new TypeReference<Response<Long>>() {});

            if (response.getErrorCode() != 0) {
                return new Long(-1);
            }

            return response.getData();

        } catch (Exception e) {
            return new Long(-1);
        }
    }

    private boolean hasTokenExpired(String token) {
        return fetchUAVersion(token) < 0;
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
