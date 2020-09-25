package com.webank.wecross.account;

import static com.webank.wecross.exception.WeCrossException.ErrorCode;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.p2p.netty.common.Node;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.UniversalAccount;
import com.webank.wecross.stubmanager.StubManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountSyncManager {
    private static Logger logger = LoggerFactory.getLogger(AccountSyncManager.class);

    private StubManager stubManager;

    private Map<Node, Integer> nodeSeq = new HashMap<>();

    private Map<String, UAProofInfo> myUAProofs = new HashMap<>(); // caID 2 UAPRoof

    private Map<String, String> cAID2UAID = new HashMap<>(); // chain account id 2 uaid

    public void addMyUAProof(String chainAccountID, String uaID, String type, String uaProof) {
        myUAProofs.put(
                chainAccountID,
                UAProofInfo.builder()
                        .chainAccountID(chainAccountID)
                        .uaID(uaID)
                        .type(type)
                        .uaProof(uaProof)
                        .build());
    }

    public void addCAID2UAID(String chainAccountID, String uaID) throws WeCrossException {
        String currentUAID = cAID2UAID.get(chainAccountID);

        if (currentUAID != null && !currentUAID.equals(uaID)) {
            throw new WeCrossException(
                    ErrorCode.DIFFRENT_CHAIN_ACCOUNT_ID_TO_UA_ID,
                    "got different chain account id to ua id. caid: "
                            + chainAccountID
                            + " current ua: "
                            + currentUAID
                            + " receive ua: "
                            + uaID);
        }

        cAID2UAID.putIfAbsent(chainAccountID, uaID);
    }

    public Integer getSeq() {
        return myUAProofs.size();
    }

    public void updatePeerSeq(Node node, Integer seq) {
        nodeSeq.putIfAbsent(node, seq);
    }

    public void removeNodeSeq(Node node) {
        if (nodeSeq.containsKey(node)) {
            nodeSeq.remove(node);
        }
    }

    public boolean hasPeerChanged(Node node, Integer currentSeq) {
        Integer recordSeq = nodeSeq.get(node);
        return recordSeq == null || !currentSeq.equals(recordSeq);
    }

    public String getUAIDByChainAccountIdentity(String identity) {
        return cAID2UAID.get(identity);
    }

    public Collection<UAProofInfo> getUAProofs() {
        return myUAProofs.values();
    }

    public void setStubManager(StubManager stubManager) {
        this.stubManager = stubManager;
    }

    public void updateByUAProofs(Collection<UAProofInfo> uaProofInfos) {
        Map<String, String> tmpCAID2UAID = new HashMap<>();
        for (UAProofInfo info : uaProofInfos) {
            String uaID = info.getUaID();
            String caID = info.getChainAccountID();

            if (verifyUAProof(info)) {
                String oldUAID = cAID2UAID.get(caID);

                if (oldUAID != null && !oldUAID.equals(uaID)) {
                    logger.warn("Overwrite account {} uaID from {} to {}", caID, oldUAID, uaID);
                }

                cAID2UAID.put(caID, uaID);
            } else {
                logger.warn("Receive invalid UAProof: " + uaProofInfos);
            }
        }
    }

    private boolean verifyUAProof(UAProofInfo uaProofInfo) {
        // TODO: verify uaproof
        return true;
    }

    public void onNewUA(UniversalAccount ua) {
        String uaID = ua.getUAID();
        logger.debug("On newUA" + ua.getName() + " " + ua.getUAID());
        for (Account account : ua.getAccounts()) {
            String caID = account.getIdentity();

            if (myUAProofs.containsKey(caID) && uaID.equals(myUAProofs.get(caID).getUaID())) {
                continue;
            }

            String uaProof = "uaProof!!"; // TODO: generate it
            UAProofInfo info =
                    UAProofInfo.builder()
                            .chainAccountID(caID)
                            .uaID(uaID)
                            .type(account.getType())
                            .uaProof(uaProof)
                            .build();

            myUAProofs.put(caID, info);
            try {
                addCAID2UAID(caID, uaID);
            } catch (WeCrossException e) {
                logger.warn("onNewUA: " + e.getMessage());
            }
        }
    }

    public String dumpCAID2UAID() {
        String dumpStr = "";

        for (Map.Entry<String, String> entry : cAID2UAID.entrySet()) {
            dumpStr +=
                    "("
                            + entry.getKey().substring(0, 8)
                            + "->"
                            + entry.getValue().substring(0, 8)
                            + ") ";
        }
        return dumpStr;
    }
}
