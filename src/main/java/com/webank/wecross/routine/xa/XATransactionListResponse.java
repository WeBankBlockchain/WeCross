package com.webank.wecross.routine.xa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.account.UniversalAccount;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class XATransactionListResponse {
    private List<XATransactionInfo> infoList = new ArrayList<>();
    private List<Integer> offsets = new ArrayList<>();
    private boolean finished = false;

    public void addXATransactionInfo(XATransactionInfo info) {
        infoList.add(info);
    }

    public void recoverUsername(AccountManager accountManager) {
        for (XATransactionInfo xaTransactionInfo : infoList) {
            UniversalAccount ua =
                    accountManager.getUniversalAccountByIdentity(
                            xaTransactionInfo.getAccountIdentity());
            String username = Objects.nonNull(ua) ? ua.getUsername() : "unknown";

            xaTransactionInfo.setUsername(username);
            xaTransactionInfo.setAccountIdentity(null);
        }
    }

    public List<XATransactionInfo> getInfoList() {
        return infoList;
    }

    public void setInfoList(List<XATransactionInfo> infoList) {
        this.infoList = infoList;
    }

    public List<Integer> getOffsets() {
        return offsets;
    }

    public void setOffsets(List<Integer> offsets) {
        this.offsets = offsets;
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
                + "infoList="
                + infoList
                + ", offsets="
                + offsets
                + ", finished="
                + finished
                + '}';
    }

    public static class XATransactionInfo {
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
