package com.webank.wecross.routine.xa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.account.UniversalAccount;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class XATransactionListResponse {
    private List<XA> xaList = new ArrayList<>();
    private Map<String, Integer> nextOffsets = Collections.synchronizedMap(new HashMap<>());
    private boolean finished = false;

    public void addXATransactionInfo(XA info) {
        xaList.add(info);
    }

    public void addOffset(String chain, Integer offset) {
        nextOffsets.put(chain, offset);
    }

    public void recoverUsername(AccountManager accountManager) {
        for (XA xa : xaList) {
            UniversalAccount ua =
                    accountManager.getUniversalAccountByIdentity(xa.getAccountIdentity());
            String username = Objects.nonNull(ua) ? ua.getUsername() : "unknown";

            xa.setUsername(username);
            xa.setAccountIdentity(null);
        }
    }

    public List<XA> getXaList() {
        return xaList;
    }

    public void setXaList(List<XA> xaList) {
        this.xaList = xaList;
    }

    public Map<String, Integer> getNextOffsets() {
        return nextOffsets;
    }

    public void setNextOffsets(Map<String, Integer> nextOffsets) {
        this.nextOffsets = nextOffsets;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "XATransactionListResponse{"
                + "xaList="
                + xaList
                + ", nextOffsets="
                + nextOffsets
                + ", finished="
                + finished
                + '}';
    }

    public static class XA {
        private String xaTransactionID;
        private String accountIdentity;
        private String username;
        private String status;
        private long timestamp;

        public String getXaTransactionID() {
            return xaTransactionID;
        }

        public void setXaTransactionID(String xaTransactionID) {
            this.xaTransactionID = xaTransactionID;
        }

        public String getAccountIdentity() {
            return accountIdentity;
        }

        public void setAccountIdentity(String accountIdentity) {
            this.accountIdentity = accountIdentity;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "XATransactionInfo{"
                    + "xaTransactionID='"
                    + xaTransactionID
                    + '\''
                    + ", accountIdentity='"
                    + accountIdentity
                    + '\''
                    + ", username='"
                    + username
                    + '\''
                    + ", status='"
                    + status
                    + '\''
                    + ", timestamp="
                    + timestamp
                    + '}';
        }
    }
}
