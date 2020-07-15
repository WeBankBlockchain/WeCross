package com.webank.wecross.routine.htlc;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.exception.WeCrossException.ErrorCode;
import com.webank.wecross.routine.RoutineDefault;
import com.webank.wecross.stub.VerifiedTransaction;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTLCScheduler {
    private Logger logger = LoggerFactory.getLogger(HTLCScheduler.class);

    private HTLC htlc;
    private HTLCProposal proposal;

    public HTLCScheduler(HTLC htlc) {
        this.htlc = htlc;
    }

    public interface Callback {
        void onReturn(WeCrossException exception, boolean state);
    }

    public void start(HTLCResourcePair htlcResourcePair, String hash, Callback callback) {
        HTLCResource selfResource = htlcResourcePair.getSelfHTLCResource();

        // get proposal info
        htlc.getProposalInfo(
                selfResource,
                selfResource.getAccount1(),
                hash,
                (exception, info) -> {
                    if (exception != null) {
                        callback.onReturn(exception, false);
                        return;
                    }

                    if (info == null || RoutineDefault.NULL_FLAG.equals(info)) {
                        callback.onReturn(
                                new WeCrossException(
                                        ErrorCode.HTLC_ERROR,
                                        "GET_PROPOSAL_INFO_ERROR",
                                        HTLCErrorCode.GET_PROPOSAL_INFO_ERROR,
                                        "proposal not found"),
                                false);
                        return;
                    }

                    try {
                        // decode proposal
                        proposal = htlc.decodeProposal(info.split(RoutineDefault.SPLIT_REGEX));
                    } catch (WeCrossException e) {
                        callback.onReturn(e, false);
                        return;
                    }

                    logger.trace(
                            "get proposal info successfully, path: {}, hash: {}, info: {}",
                            selfResource.getSelfPath(),
                            hash,
                            proposal);

                    // check if the transfer is succeeded
                    if (proposal.isSelfUnlocked() && proposal.isCounterpartyUnlocked()) {
                        deleteProposal(selfResource, hash, true, callback);
                        return;
                    }

                    checkSelfRollback(
                            selfResource,
                            hash,
                            (exception1, selfRolledback) -> {
                                if (exception1 != null) {
                                    callback.onReturn(exception1, false);
                                    return;
                                }

                                if (selfRolledback) {
                                    checkCounterpartyRollback(
                                            selfResource,
                                            hash,
                                            (exception2, counterpartyRolledback) -> {
                                                if (exception2 != null) {
                                                    callback.onReturn(exception2, false);
                                                    return;
                                                }

                                                if (counterpartyRolledback) {
                                                    // both parties are rolledback
                                                    deleteProposal(
                                                            selfResource, hash, false, callback);
                                                } else {
                                                    // continue
                                                    execProposal(htlcResourcePair, hash, callback);
                                                }
                                            });
                                } else {
                                    checkCounterpartyRollback(
                                            selfResource,
                                            hash,
                                            (exception2, counterpartyRolledback) -> {
                                                if (exception2 != null) {
                                                    callback.onReturn(exception2, false);
                                                } else {
                                                    // continue
                                                    execProposal(htlcResourcePair, hash, callback);
                                                }
                                            });
                                }
                            });
                });
    }

    public void checkSelfRollback(HTLCResource htlcResource, String hash, Callback callback) {
        if (proposal.isSelfRolledback()) {
            callback.onReturn(null, true);
            return;
        }

        BigInteger now = BigInteger.valueOf(System.currentTimeMillis() / 1000);
        if (now.compareTo(proposal.getSelfTimelock()) >= 0) {
            htlc.rollback(
                    htlcResource,
                    hash,
                    (exception, result) -> {
                        if (exception != null) {
                            callback.onReturn(exception, false);
                            return;
                        }

                        if (RoutineDefault.SUCCESS_FLAG.equals(result)) {
                            callback.onReturn(null, true);
                            return;
                        }

                        if (RoutineDefault.NOT_YET.equals(result)) {
                            callback.onReturn(null, false);
                            return;
                        }

                        logger.trace(
                                "no need to rollback, reason: {}, hash: {}, path: {}",
                                hash,
                                result,
                                htlcResource.getSelfPath());
                        callback.onReturn(null, true);
                    });
        } else {
            callback.onReturn(null, false);
        }
    }

    public void checkCounterpartyRollback(
            HTLCResource htlcResource, String hash, Callback callback) {
        if (proposal.isCounterpartyRolledback()) {
            callback.onReturn(null, true);
            return;
        }

        BigInteger now = BigInteger.valueOf(System.currentTimeMillis() / 1000);
        if (now.compareTo(proposal.getCounterpartyTimelock()) >= 0) {
            htlc.setCounterpartyRollbackState(
                    htlcResource,
                    hash,
                    (exception, result) -> {
                        if (exception != null) {
                            callback.onReturn(exception, false);
                        } else {
                            callback.onReturn(null, true);
                        }
                    });
        } else {
            callback.onReturn(null, false);
        }
    }

    private void execProposal(HTLCResourcePair htlcResourcePair, String hash, Callback callback) {
        HTLCResource selfResource = htlcResourcePair.getSelfHTLCResource();
        HTLCResource counterpartyResource = htlcResourcePair.getCounterpartyHTLCResource();

        if (counterpartyResource.getSelfResource() == null) {
            callback.onReturn(
                    new WeCrossException(
                            ErrorCode.HTLC_ERROR,
                            "SCHEDULE_ERROR",
                            HTLCErrorCode.NO_COUNTERPARTY_RESOURCE,
                            "counterparty resource: "
                                    + counterpartyResource.getSelfPath().toString()
                                    + " not found"),
                    false);
            return;
        }

        // check if the proposal data in two chains is consistent
        checkProposalConsistency(
                selfResource,
                counterpartyResource,
                hash,
                (exception, isConsistent) -> {
                    if (exception != null) {
                        callback.onReturn(exception, false);
                        return;
                    }

                    if (!isConsistent) {
                        logger.error(
                                "proposal data is inconsistent, delete current proposal: {}, path: {}",
                                hash,
                                selfResource.getSelfPath());
                        deleteProposal(selfResource, hash, false, callback);
                        return;
                    }

                    if (proposal.isInitiator()) {
                        execInitiatorProcess(
                                selfResource,
                                counterpartyResource,
                                hash,
                                (exception1, succeeded) -> {
                                    if (exception1 != null) {
                                        callback.onReturn(exception1, false);
                                        return;
                                    }

                                    if (succeeded) {
                                        // finished
                                        deleteProposal(selfResource, hash, true, callback);
                                    } else {
                                        callback.onReturn(null, false);
                                    }
                                });
                    } else {
                        // participant does nothing
                        callback.onReturn(null, false);
                    }
                });
    }

    private void checkProposalConsistency(
            HTLCResource selfResource,
            HTLCResource counterpartyResource,
            String hash,
            Callback callback) {
        htlc.getNewProposalTxInfo(
                selfResource,
                hash,
                (exception, selfTxInfo) -> {
                    if (exception != null) {
                        callback.onReturn(exception, false);
                        return;
                    }

                    if (selfTxInfo == null || RoutineDefault.NULL_FLAG.equals(selfTxInfo)) {
                        callback.onReturn(
                                new WeCrossException(
                                        ErrorCode.HTLC_ERROR,
                                        "GET_NEW_PROPOSAL_TX_INFO_ERROR",
                                        HTLCErrorCode.GET_PROPOSAL_TX_INFO_ERROR,
                                        "self tx-info of proposal not found"),
                                false);
                        return;
                    }

                    String[] info0 = selfTxInfo.split(RoutineDefault.SPLIT_REGEX);
                    VerifiedTransaction verifiedTransaction0 =
                            getVerifiedTransaction(
                                    selfResource, info0[0], Long.parseLong(info0[1]));

                    if (verifiedTransaction0 == null) {
                        logger.error(
                                "self verified transaction not found, hash: {}, path: {}",
                                hash,
                                selfResource.getSelfPath());
                        callback.onReturn(
                                new WeCrossException(
                                        ErrorCode.HTLC_ERROR,
                                        "GET_NEW_PROPOSAL_TX_INFO_ERROR",
                                        HTLCErrorCode.GET_PROPOSAL_TX_INFO_ERROR,
                                        "self verified transaction not found"),
                                false);
                        return;
                    }

                    logger.trace(
                            "verifiedTransaction for self transfer contract: {}",
                            verifiedTransaction0);

                    // construct new args, args: [hash] , [role] ...
                    String[] args = verifiedTransaction0.getTransactionRequest().getArgs();
                    if (RoutineDefault.TRUE_FLAG.equals(args[1])) {
                        args[1] = RoutineDefault.FALSE_FLAG;
                    } else {
                        args[1] = RoutineDefault.TRUE_FLAG;
                    }

                    // construct new outputs
                    String[] output = verifiedTransaction0.getTransactionResponse().getResult();

                    htlc.getNewProposalTxInfo(
                            counterpartyResource,
                            hash,
                            (exception1, counterpartyTxInfo) -> {
                                if (exception1 != null) {
                                    callback.onReturn(exception1, false);
                                    return;
                                }

                                if (counterpartyTxInfo == null
                                        || RoutineDefault.NULL_FLAG.equals(counterpartyTxInfo)) {
                                    callback.onReturn(
                                            new WeCrossException(
                                                    ErrorCode.HTLC_ERROR,
                                                    "GET_NEW_PROPOSAL_TX_INFO_ERROR",
                                                    HTLCErrorCode.GET_PROPOSAL_TX_INFO_ERROR,
                                                    "counterparty tx-info of proposal not found"),
                                            false);
                                    return;
                                }

                                String[] info1 =
                                        counterpartyTxInfo.split(RoutineDefault.SPLIT_REGEX);

                                VerifyData verifyData =
                                        new VerifyData(
                                                Long.parseLong(info1[1]),
                                                info1[0],
                                                "newProposal",
                                                args,
                                                output);

                                VerifiedTransaction verifiedTransaction1 =
                                        getVerifiedTransaction(
                                                counterpartyResource,
                                                info1[0],
                                                Long.parseLong(info1[1]));

                                if (verifiedTransaction1 == null) {
                                    logger.error(
                                            "counterparty verified transaction not found, hash: {}, path: {}",
                                            hash,
                                            selfResource.getSelfPath());
                                    callback.onReturn(
                                            new WeCrossException(
                                                    ErrorCode.HTLC_ERROR,
                                                    "GET_NEW_PROPOSAL_TX_INFO_ERROR",
                                                    HTLCErrorCode.GET_PROPOSAL_TX_INFO_ERROR,
                                                    "counterparty verified transaction not found"),
                                            false);
                                    return;
                                }

                                logger.trace(
                                        "verifiedTransaction for counterparty transfer contract: {}",
                                        verifiedTransaction1);

                                callback.onReturn(null, verifyData.verify(verifiedTransaction1));
                            });
                });
    }

    private VerifiedTransaction getVerifiedTransaction(
            HTLCResource htlcResource, String txHash, long blockNum) {
        CompletableFuture<VerifiedTransaction> verifiedTransactionFuture =
                new CompletableFuture<>();

        htlcResource
                .getDriver()
                .asyncGetVerifiedTransaction(
                        htlcResource.getSelfPath(),
                        txHash,
                        blockNum,
                        htlcResource.getBlockHeaderManager(),
                        htlcResource.chooseConnection(),
                        (e, verifiedTransaction) -> {
                            if (e != null) {
                                logger.error("get verifiedTransaction0Future exception: " + e);
                                verifiedTransactionFuture.complete(null);
                            } else {

                                verifiedTransactionFuture.complete(verifiedTransaction);
                            }
                        });

        VerifiedTransaction verifiedTransaction = null;
        try {
            verifiedTransaction = verifiedTransactionFuture.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("get verifiedTransaction0Future timeout: " + e);
        }

        return verifiedTransaction;
    }

    private void execInitiatorProcess(
            HTLCResource selfResource,
            HTLCResource counterpartyResource,
            String hash,
            Callback callback) {
        if (!proposal.isSelfUnlocked()) {
            // lock self
            handleSelfLock(
                    selfResource,
                    hash,
                    (exception, selfLocked) -> {
                        if (exception != null) {
                            callback.onReturn(exception, false);
                            return;
                        }

                        // lock counterparty
                        handleCounterpartyLock(
                                selfResource,
                                counterpartyResource,
                                hash,
                                (exception1, counterpartyLocked) -> {
                                    if (exception1 != null) {
                                        callback.onReturn(exception1, false);
                                    } else {
                                        // unlock counterparty
                                        handleCounterpartyUnlock(
                                                selfResource, counterpartyResource, hash, callback);
                                    }
                                });
                    });

        } else {
            // unlock counterparty
            handleCounterpartyUnlock(selfResource, counterpartyResource, hash, callback);
        }
    }

    private void handleSelfLock(HTLCResource htlcResource, String hash, Callback callback) {
        if (!proposal.isSelfLocked()) {
            htlc.lockSelf(
                    htlcResource,
                    hash,
                    (exception, result) -> {
                        if (exception != null) {
                            callback.onReturn(exception, false);
                        } else {
                            logger.trace(
                                    "lock self successfully: {}, path: {}",
                                    hash,
                                    htlcResource.getSelfPath());
                            callback.onReturn(null, true);
                        }
                    });
        } else {
            callback.onReturn(null, true);
        }
    }

    private void handleCounterpartyLock(
            HTLCResource selfResource,
            HTLCResource counterpartyResource,
            String hash,
            Callback callback) {
        if (!proposal.isCounterpartyLocked()) {
            htlc.lockCounterparty(
                    counterpartyResource,
                    hash,
                    (exception, result) -> {
                        if (exception != null) {
                            callback.onReturn(exception, false);
                            return;
                        }

                        htlc.setCounterpartyLockState(
                                selfResource,
                                hash,
                                (exception1, result1) -> {
                                    if (exception1 != null) {
                                        callback.onReturn(exception1, false);
                                    } else {
                                        logger.trace(
                                                "lock counterparty successfully: {}, path: {}",
                                                hash,
                                                selfResource.getSelfPath());
                                        callback.onReturn(null, true);
                                    }
                                });
                    });
        } else {
            callback.onReturn(null, true);
        }
    }

    private void handleCounterpartyUnlock(
            HTLCResource selfResource,
            HTLCResource counterpartyResource,
            String hash,
            Callback callback) {
        htlc.getCounterpartyHtlcAddress(
                counterpartyResource.getSelfResource(),
                counterpartyResource.getAccount1(),
                (exception, address) -> {
                    if (exception != null) {
                        callback.onReturn(exception, false);
                        return;
                    }

                    if (address == null || RoutineDefault.NULL_FLAG.equals(address)) {
                        callback.onReturn(
                                new WeCrossException(
                                        ErrorCode.HTLC_ERROR,
                                        "GET_COUNTERPARTY_HTLC_ADDRESS_ERROR",
                                        HTLCErrorCode.NONE_RETURN,
                                        "counterparty htlc address has not set"),
                                false);
                        return;
                    }

                    counterpartyResource.setCounterpartyAddress(address);

                    htlc.unlockCounterparty(
                            counterpartyResource,
                            hash,
                            proposal.getSecret(),
                            (exception1, result1) -> {
                                if (exception1 != null) {
                                    callback.onReturn(exception1, false);
                                    return;
                                }

                                htlc.setCounterpartyUnlockState(
                                        selfResource,
                                        selfResource.getAccount1(),
                                        hash,
                                        (exception2, result2) -> {
                                            if (exception2 != null) {
                                                callback.onReturn(exception2, false);
                                            } else {
                                                logger.trace(
                                                        "unlock counterparty successfully: {}, path: {}",
                                                        hash,
                                                        selfResource.getSelfPath());
                                                callback.onReturn(null, true);
                                            }
                                        });
                            });
                });
    }

    private void deleteProposal(
            HTLCResource htlcResource, String hash, boolean state, Callback callback) {
        htlc.deleteProposalID(
                htlcResource,
                hash,
                (exception, result) -> {
                    if (exception != null) {
                        callback.onReturn(exception, false);
                        return;
                    }

                    if (state) {
                        logger.info(
                                "Current proposal succeeded: {}, path: {}",
                                hash,
                                htlcResource.getSelfPath());
                    } else {
                        logger.info(
                                "Current proposal failed: {}, path: {}",
                                hash,
                                htlcResource.getSelfPath());
                    }
                    callback.onReturn(null, true);
                });
    }
}
