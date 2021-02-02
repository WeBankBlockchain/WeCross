package com.webank.wecross.routine.xa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.account.UniversalAccount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class XATransactionListResponse {
    private XAResponse xaResponse;
    private List<XA> xaList = new ArrayList<>();
    private Map<String, Long> nextOffsets = Collections.synchronizedMap(new HashMap<>());
    private boolean finished = false;

    public void addXATransactionInfo(XA xa) {
        xaList.add(xa);
    }

    public void addOffset(String chain, Long offset) {
        nextOffsets.put(chain, offset);
    }

    public void recoverUsername(AccountManager accountManager) {
        for (XA xa : xaList) {
            UniversalAccount ua =
                    accountManager.getUniversalAccountByIdentity(xa.getAccountIdentity());
            String username = Objects.nonNull(ua) ? ua.getUsername() : null;

            xa.setUsername(username);
            xa.setAccountIdentity(null);
        }
    }

    public XAResponse getXaResponse() {
        return xaResponse;
    }

    public void setXaResponse(XAResponse xaResponse) {
        this.xaResponse = xaResponse;
    }

    public List<XA> getXaList() {
        return xaList;
    }

    public void setXaList(List<XA> xaList) {
        this.xaList = xaList;
    }

    public Map<String, Long> getNextOffsets() {
        return nextOffsets;
    }

    public void setNextOffsets(Map<String, Long> nextOffsets) {
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
                + "xaResponse="
                + xaResponse
                + ", xaList="
                + xaList
                + ", nextOffsets="
                + nextOffsets
                + ", finished="
                + finished
                + '}';
    }

    public static class XAList {
        private Long total;
        private XA[] xaTransactions;

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public XA[] getXaTransactions() {
            return xaTransactions;
        }

        public void setXaTransactions(XA[] xaTransactions) {
            this.xaTransactions = xaTransactions;
        }

        @Override
        public String toString() {
            return "XAList{"
                    + "total="
                    + total
                    + ", xaTransactions="
                    + Arrays.toString(xaTransactions)
                    + '}';
        }
    }

    public static class XA {
        private String xaTransactionID;
        private String accountIdentity;
        private String username;
        private String status;
        private long timestamp;
        private String[] paths;

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

        public String[] getPaths() {
            return paths;
        }

        public void setPaths(String[] paths) {
            this.paths = paths;
        }

        @Override
        public String toString() {
            return "XA{"
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
                    + ", paths="
                    + Arrays.toString(paths)
                    + '}';
        }
    }
}
