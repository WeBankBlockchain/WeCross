package com.webank.wecross.account;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.client.ClientMessageEngine;
import com.webank.wecross.network.client.Request;
import com.webank.wecross.network.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminContext extends UserContext {
    private static Logger logger = LoggerFactory.getLogger(AdminContext.class);

    private String username;
    private String password;

    public void setAccountManagerEngine(ClientMessageEngine accountManagerEngine) {
        this.accountManagerEngine = accountManagerEngine;
    }

    private ClientMessageEngine accountManagerEngine;

    static class LoginRequest {
        public String username;
        public String password;
    }

    static class LoginResponse {
        public int errorCode;
        public String message;
        public String credential;
        public UAInfo universalAccount;

        static class UAInfo {
            public String username;
            public String uaID;
            public String pubKey;
            public boolean isAdmin;
        }
    }

    public void login() throws WeCrossException {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.username = username;
        loginRequest.password = password;

        Request<LoginRequest> request = new Request<>();
        request.setMethod("/auth/login");
        request.setData(loginRequest);

        try {
            Response<LoginResponse> response =
                    accountManagerEngine.send(
                            request, new TypeReference<Response<LoginResponse>>() {
                            });

            if (response.getErrorCode() != 0) {
                throw new Exception(
                        "query error: status: "
                                + response.getErrorCode()
                                + " message: "
                                + response.getMessage());
            }

            if (response.getData().errorCode != 0) {
                throw new Exception(
                        "login error: status: "
                                + response.getData().errorCode
                                + " message: "
                                + response.getData().message);
            }

            if (!response.getData().universalAccount.isAdmin) {
                throw new Exception(
                        "login error: " + username + " is not admin in account manager");
            }

            super.setToken(response.getData().credential);

            logger.info("Admin login success with token: " + getToken());
        } catch (WeCrossException e) {
            logger.error("Admin login failed for {}, (Please check WeCross-Account-Manager service is available)", e.getMessage());
            throw new WeCrossException(
                    WeCrossException.ErrorCode.ADMIN_LOGIN_FAILED,
                    "Admin login failed for: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Admin login failed for: " + e.getMessage());
            throw new WeCrossException(
                    WeCrossException.ErrorCode.ADMIN_LOGIN_FAILED,
                    "Admin login failed for: " + e.getMessage());
        }
    }

    public void reLogin() {
        try {
            login();
        } catch (WeCrossException e) {
            logger.error(e.getMessage());
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
