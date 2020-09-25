package com.webank.wecross.account;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Account;
import com.webank.wecross.utils.SM2;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@Builder
public class UniversalAccountImpl implements com.webank.wecross.stub.UniversalAccount {
    private static Logger logger = LoggerFactory.getLogger(UniversalAccountImpl.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    private String username;
    private String uaID;
    private String pubKey;
    private String secKey;
    private boolean isAdmin;
    private long lastActiveTimestamp;

    private final long QUERY_ACTIVE_EXPIRES = 1000; // 1s

    @Builder.Default
    private Map<String, Map<Integer, Account>> type2ChainAccounts = new HashMap<>();

    @Builder.Default private Map<String, Account> type2DefaultAccount = new HashMap<>();

    @Override
    public Account getAccount(String type) {
        return type2DefaultAccount.get(type);
    }

    public void setDefaultAccount(String type, Account account) {
        type2DefaultAccount.put(type, account);
        logger.info("setDefaultAccount {} {}", type, account);
    }

    public void addAccount(String type, int keyID, Account account, ChainAccountDetails details) {
        type2ChainAccounts.putIfAbsent(type, new HashMap<>());
        type2ChainAccounts.get(type).put(new Integer(keyID), account);

        logger.info("addAccount {} {} {} {}", type, keyID, account, details);
    }

    @JsonGetter("chainAccounts")
    public List<Account> getAccounts() {
        List<Account> accounts = new LinkedList<>();
        for (Map<Integer, Account> accountMap : type2ChainAccounts.values()) {
            for (Account account : accountMap.values()) {
                accounts.add(account);
            }
        }
        return accounts;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public String getUAID() {
        return uaID;
    }

    @Override
    public String getPub() {
        return pubKey;
    }

    @Override
    public byte[] sign(byte[] message) {
        try {
            return SM2.sign(secKey, message);
        } catch (Exception e) {
            logger.error("sign exception: ", e);
            return null;
        }
    }

    @Override
    public boolean verify(byte[] signData, byte[] originData) {
        try {
            // Notice: Just verify signData, not verify signData belongs to this UA
            return SM2.verify(signData, originData);
        } catch (Exception e) {
            logger.error("sign exception: ", e);
            return false;
        }
    }

    @Data
    public static class ChainAccountDetails {
        private String username; // ua
        private Integer keyID;
        private String identity;
        private String type;

        @JsonProperty("isDefault")
        private Boolean isDefault;

        private String pubKey;
        private String secKey;
        private String ext0;
        private String ext1;
        private String ext2;
        private String ext3;

        public Map<String, Object> toProperties() {
            Map<String, Object> properties = new HashMap<>();
            properties.put("username", username);
            properties.put("keyID", keyID);
            properties.put("type", type);
            properties.put("isDefault", isDefault);
            properties.put("pubKey", pubKey);
            properties.put("secKey", secKey);
            properties.put("ext0", ext0);
            properties.put("ext1", ext1);
            properties.put("ext2", ext2);
            properties.put("ext3", ext3);
            return properties;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChainAccountDetails)) return false;
            ChainAccountDetails that = (ChainAccountDetails) o;
            return getKeyID().equals(that.getKeyID()) && getType().equals(that.getType());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getKeyID(), getType());
        }
    }

    @Data
    public static class UADetails {
        private String username;
        private String uaID;
        private String pubKey;
        private String password;
        private String secKey;
        private String role;

        @JsonProperty("isAdmin")
        private boolean isAdmin;

        private Map<String, Map<Integer, ChainAccountDetails>> type2ChainAccountDetails;
    }

    public boolean isActive() {
        return System.currentTimeMillis() - lastActiveTimestamp < QUERY_ACTIVE_EXPIRES;
    }

    public void activate() {
        lastActiveTimestamp = System.currentTimeMillis();
    }
}
