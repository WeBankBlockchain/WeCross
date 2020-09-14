package com.webank.wecross.account;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.webank.wecross.network.client.ClientMessageEngine;
import com.webank.wecross.network.client.Request;
import com.webank.wecross.network.client.Response;
import com.webank.wecross.restserver.RPCContext;
import com.webank.wecross.stub.Account;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteAccountManager implements AccountManager {
    private Logger logger = LoggerFactory.getLogger(RemoteAccountManager.class);

    private Map<String, Map<String, Account>> token2Accounts;
    private ClientMessageEngine engine;
    private RPCContext rpcContext;

    @Override
    public Map<String, Account> getAccounts() {
        String token = rpcContext.getToken();
        return token2Accounts.get(token);
    }

    @Override
    public void setAccounts(Map<String, Account> accounts) {
        String token = rpcContext.getToken();
        token2Accounts.putIfAbsent(token, accounts);
    }

    @Override
    public void addAccount(String name, Account account) {
        String token = rpcContext.getToken();
        token2Accounts.putIfAbsent(token, new HashMap<>());
        token2Accounts.get(token).putIfAbsent(name, account);
    }

    @Override
    public Account getAccount(String name) {
        String token = rpcContext.getToken();
        if (token != null) {
            if (!token2Accounts.containsKey(token) || hasExpiresed(token)) {
                fetchAccounts();
            }
        }

        token2Accounts.putIfAbsent(token, new HashMap<>());
        return token2Accounts.get(token).get(name);
    }

    public void setEngine(ClientMessageEngine engine) {
        this.engine = engine;
    }

    public void setRpcContext(RPCContext rpcContext) {
        this.rpcContext = rpcContext;
    }

    private boolean hasExpiresed(String token) {
        DecodedJWT jwt = JWT.decode(token);
        Date tokenExpiresDate = jwt.getExpiresAt();
        Date now = new Date();
        return tokenExpiresDate.before(now);
    }

    public class ChainAccountDetails {
        public String username; // ua
        public Integer keyID;
        public String type;
        public boolean isDefault;
        public String pubKey;
        public String secKey;
        public String UAProof;
        public String ext0;
        public String ext1;
        public String ext2;
        public String ext3;
    }

    public class UADetails {
        public String username;
        public String uaID;
        public String pubKey;
        public String password;
        public String secKey;
        public String role;
        public Map<String, Map<Integer, ChainAccountDetails>> type2ChainAccounts;
    }

    private void fetchAccounts() {

        Request<Object> request = new Request();
        request.setData(new Object());
        request.setMethod("/auth/getUniversalAccount");

        try {

            Response<UADetails> response = engine.send(request);
            // xxxxx

        } catch (Exception e) {
            logger.error("FetchAccount failed: " + e.getMessage());
        }
    }
}
