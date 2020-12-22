package com.webank.wecross.account;

import com.webank.wecross.account.uaproof.UAProof;
import com.webank.wecross.account.uaproof.UAProofGenerator;
import com.webank.wecross.account.uaproof.UAProofInfo;
import com.webank.wecross.account.uaproof.UAProofVerifier;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.p2p.netty.common.Node;
import com.webank.wecross.stub.Account;
import com.webank.wecross.utils.core.SeqUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountSyncManager {
    private static Logger logger = LoggerFactory.getLogger(AccountSyncManager.class);

    private UAProofVerifier uaProofVerifier;

    private UAProofGenerator uaProofGenerator;

    private Map<Node, Integer> nodeSeq = new HashMap<>();

    private Integer seq;

    // TODO: save myUAProofs in disk
    private Map<String, UAProofInfo> myUAProofs = new HashMap<>(); // caID 2 UAPRoof

    // TODO: save caID2UaID in disk
    private Map<String, String> caID2UaID = new HashMap<>(); // chain account id 2 uaid

    public AccountSyncManager() {
        updateSeq();
    }

    public synchronized Integer getSeq() {
        return seq;
    }

    private void updateSeq() {
        seq = SeqUtils.newSeq();
    }

    public void updatePeerSeq(Node node, Integer seq) {
        logger.info("update peer account seq, from: {}, to: {}", nodeSeq.get(node), seq);

        nodeSeq.put(node, seq);
    }

    public void removeNodeSeq(Node node) {
        if (nodeSeq.containsKey(node)) {
            nodeSeq.remove(node);
        }
    }

    public void updateCA2UA(String caID, String uaID, UAProofInfo uaProof, boolean needUpdateSeq) {
        logger.info("updateCA2UA {}", uaProof);
        caID2UaID.put(caID, uaID);
        myUAProofs.put(caID, uaProof);
        if (needUpdateSeq) {
            updateSeq();
        }
    }

    public boolean hasPeerChanged(Node node, Integer currentSeq) {
        Integer recordSeq = nodeSeq.get(node);
        return recordSeq == null || !currentSeq.equals(recordSeq);
    }

    public synchronized Collection<UAProofInfo> getUAProofs() {
        return myUAProofs.values();
    }

    public synchronized void updateByUAProofs(Collection<UAProofInfo> uaProofInfos) {
        for (UAProofInfo info : uaProofInfos) {
            String uaID = info.getUaID();
            String caID = info.getChainAccountID();

            if (verifyUAProof(info)) {
                String oldUAID = caID2UaID.get(caID);

                if (oldUAID != null && oldUAID.equals(uaID)) {
                    continue;
                }

                updateCA2UA(caID, uaID, info, false); // only newUA need update seq
            } else {
                logger.warn("Receive invalid UAProof: " + uaProofInfos);
            }
        }
    }

    private boolean verifyUAProof(UAProofInfo uaProofInfo) {
        try {
            logger.debug("Try to verifyUAProof: {}", uaProofInfo);

            String uaProofStr = uaProofInfo.getUaProof();
            if (uaProofStr == null || uaProofStr.length() == 0) {
                logger.warn("Empty UAProof: " + uaProofInfo);
                return false;
            }

            UAProof uaProof = UAProof.perseFrom(uaProofStr);

            if (!uaProofVerifier.verify(uaProof)) {
                logger.warn("UAProof verify failed: " + uaProof);
                return false;
            }

            if (!uaProofInfo.getChainAccountID().equals(uaProof.getCaID())
                    || !uaProofInfo.getUaID().equals(uaProof.getUaID())) {
                logger.warn(
                        "Identities are not belong to the UAProof identity:{} UAProof:{}",
                        uaProofInfo,
                        uaProof);
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("verifyUAProof excpetion: ", e);
            return false;
        }
    }

    public synchronized void onNewUA(UniversalAccount ua) {
        String uaID = ua.getUAID();
        logger.debug(
                "On newUA"
                        + ua.getName()
                        + " version: "
                        + ua.getVersion()
                        + " UAID:"
                        + ua.getUAID());
        for (Account account : ua.getAccounts()) {
            String caID = account.getIdentity();

            if (myUAProofs.containsKey(caID) && uaID.equals(myUAProofs.get(caID).getUaID())) {
                continue;
            }

            try {
                String uaProof = uaProofGenerator.generate(ua, account).toJsonString();
                UAProofInfo info =
                        UAProofInfo.builder()
                                .chainAccountID(caID)
                                .uaID(uaID)
                                .type(account.getType())
                                .uaProof(uaProof)
                                .build();

                updateCA2UA(caID, uaID, info, true); // only newUA need update seq
            } catch (WeCrossException e) {
                logger.warn("onNewUA: ", e);
            } catch (Exception e) {
                logger.warn("onNewUA: ", e);
            }
        }
    }

    public String dumpCaID2UaID() {
        String dumpStr = "";

        for (Map.Entry<String, String> entry : caID2UaID.entrySet()) {
            dumpStr +=
                    "("
                            + entry.getKey().substring(0, 32).replaceAll("\n", "")
                            + "->"
                            + entry.getValue().substring(0, 32).replaceAll("\n", "")
                            + ") ";
        }
        return dumpStr;
    }

    public long getCaID2UaIDSize() {
        return caID2UaID.size();
    }

    public void setUaProofVerifier(UAProofVerifier uaProofVerifier) {
        this.uaProofVerifier = uaProofVerifier;
    }

    public void setUaProofGenerator(UAProofGenerator uaProofGenerator) {
        this.uaProofGenerator = uaProofGenerator;
    }
}
