package com.webank.wecross.account;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.webank.wecross.stub.Account;
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
public class UniversalAccount {
    private static Logger logger = LoggerFactory.getLogger(UniversalAccount.class);
    private String username;
    private String uaID;
    private String pubKey;
    private boolean isAdmin;

    private Map<String, Map<Integer, Account>> type2ChainAccounts = new HashMap<>();

    private Map<String, Account> type2DefaultAccount;

    public Account getAccount(String type) {
        return type2DefaultAccount.get(type);
    }

    public void setDefaultAccount(String type, Account account) {
        type2DefaultAccount.put(type, account);
    }

    public void addAccount(String type, int keyID, Account account, ChainAccountDetails details) {
        type2ChainAccounts.putIfAbsent(type, new HashMap<>());
        type2ChainAccounts.get(type).put(new Integer(keyID), account);
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

    @Data
    public class ChainAccountDetails {
        private String username; // ua
        private Integer keyID;
        private String type;

        @JsonProperty("isDefault")
        private boolean isDefault;

        private String pubKey;
        @JsonIgnore private String secKey;
        @JsonIgnore private String ext0;
        @JsonIgnore private String ext1;
        @JsonIgnore private String ext2;
        @JsonIgnore private String ext3;

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
    public class UADetails {
        private String username;
        private String uaID;
        private String pubKey;
        private String password;
        private String secKey;
        private String role;
        private boolean isAdmin;
        private Map<String, Map<Integer, ChainAccountDetails>> type2ChainAccounts;
    }
}
