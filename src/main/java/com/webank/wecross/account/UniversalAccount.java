package com.webank.wecross.account;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecross.stub.Account;
import com.webank.wecross.utils.SM2;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniversalAccount {
    private static Logger logger = LoggerFactory.getLogger(UniversalAccount.class);

    private String username;
    private String uaID;
    private String pubKey;
    private String secKey;
    private boolean isAdmin;
    private Long version;
    private long lastActiveTimestamp;
    private String[] allowChainPaths;
    private AccountAccessControlFilter accessControlFilter; // json ignore

    private final long QUERY_ACTIVE_EXPIRES = 10 * 1000; // 10s

    private Map<String, Map<Integer, Account>> type2ChainAccounts = new HashMap<>();

    private Map<String, Account> type2DefaultAccount = new HashMap<>();

    // make every chain has a default account
    private Map<String, Account> chain2DefaultChainAccount = new HashMap<>();

    // chain name is like "payment.fabric-mychannel"
    public Account getChainAccount(String type, String chainName) {
        String typeChain = type + ":" + chainName;
        return chain2DefaultChainAccount.get(typeChain);
    }

    // chain name is like "payment.fabric-mychannel"
    public void setDefaultChainAccount(String chainName, Account account) {
        String typeChain = account.getType() + ":" + chainName;
        chain2DefaultChainAccount.put(typeChain, account);
        logger.info("setDefaultChainAccount {} {}", chainName, account);
    }

    // this func should be added to utils
    public static boolean checkChainName(String chainName) {
        if (chainName.length() == 0) {
            return false;
        }
        String regex = "[a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(chainName);
        return m.matches();
    }

    // only use in asyncSendTransaction, asyncCall. maybe asyncCustomCommand can use too.
    // if not set default chain account, use the universal default type account.
    public Account getSendAccount(String type, String chainName) {

        Account account = getChainAccount(type, chainName);
        if (account != null) {
            return account;
        }

        return getAccount(type);
    }

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

    public String getName() {
        return username;
    }

    public String getUAID() {
        return uaID;
    }

    public String getPub() {
        return pubKey;
    }

    public byte[] sign(byte[] message) {
        try {
            return SM2.sign(secKey, message);
        } catch (Exception e) {
            logger.error("sign exception: ", e);
            return null;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUaID() {
        return uaID;
    }

    public void setUaID(String uaID) {
        this.uaID = uaID;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getSecKey() {
        return secKey;
    }

    public void setSecKey(String secKey) {
        this.secKey = secKey;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isActive() {
        return System.currentTimeMillis() - lastActiveTimestamp < QUERY_ACTIVE_EXPIRES;
    }

    public void activate() {
        lastActiveTimestamp = System.currentTimeMillis();
    }

    public static UniversalAccountBuilder builder() {
        return new UniversalAccountBuilder();
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String[] getAllowChainPaths() {
        return allowChainPaths;
    }

    public void setAllowChainPaths(String[] allowChainPaths) {
        this.allowChainPaths = allowChainPaths;
    }

    @JsonIgnore
    public AccountAccessControlFilter getAccessControlFilter() {
        return accessControlFilter;
    }

    public void setAccessControlFilter(AccountAccessControlFilter accessControlFilter) {
        this.accessControlFilter = accessControlFilter;
    }

    @Override
    public String toString() {
        return "UniversalAccount{"
                + "username='"
                + username
                + '\''
                + ", uaID='"
                + uaID
                + '\''
                + ", pubKey='"
                + pubKey
                + '\''
                + ", secKey='"
                + secKey
                + '\''
                + ", isAdmin="
                + isAdmin
                + ", version="
                + version
                + ", lastActiveTimestamp="
                + lastActiveTimestamp
                + ", allowChainPaths="
                + Arrays.toString(allowChainPaths)
                + '}';
    }

    public static final class UniversalAccountBuilder {
        private String username;
        private String uaID;
        private String pubKey;
        private String secKey;
        private boolean isAdmin;
        private String[] allowChainPaths;
        private Long version;

        private long lastActiveTimestamp;

        private UniversalAccountBuilder() {}

        public static UniversalAccountBuilder anUniversalAccount() {
            return new UniversalAccountBuilder();
        }

        public UniversalAccountBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UniversalAccountBuilder uaID(String uaID) {
            this.uaID = uaID;
            return this;
        }

        public UniversalAccountBuilder pubKey(String pubKey) {
            this.pubKey = pubKey;
            return this;
        }

        public UniversalAccountBuilder secKey(String secKey) {
            this.secKey = secKey;
            return this;
        }

        public UniversalAccountBuilder isAdmin(boolean isAdmin) {
            this.isAdmin = isAdmin;
            return this;
        }

        public UniversalAccountBuilder lastActiveTimestamp(long lastActiveTimestamp) {
            this.lastActiveTimestamp = lastActiveTimestamp;
            return this;
        }

        public UniversalAccountBuilder allowChainPaths(String[] allowChainPaths) {
            this.allowChainPaths = allowChainPaths;
            return this;
        }

        public UniversalAccountBuilder version(Long version) {
            this.version = version;
            return this;
        }

        public UniversalAccount build() {
            UniversalAccount universalAccount = new UniversalAccount();
            universalAccount.setUsername(username);
            universalAccount.setUaID(uaID);
            universalAccount.setPubKey(pubKey);
            universalAccount.setSecKey(secKey);
            universalAccount.isAdmin = this.isAdmin;
            universalAccount.lastActiveTimestamp = this.lastActiveTimestamp;
            universalAccount.allowChainPaths = this.allowChainPaths;
            universalAccount.version = this.version;
            return universalAccount;
        }
    }
}
