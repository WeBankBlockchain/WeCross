package com.webank.wecross.routine.htlc;

import com.webank.wecross.account.UniversalAccount;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.TransactionValidator;

public interface HTLC {
    interface Callback {
        void onReturn(WeCrossException exception, String result);
    }

    interface VerifyCallback {
        void onReturn(WeCrossException exception, boolean result);
    }

    /**
     * lock self asset
     *
     * @param htlcResource self htlc resource
     * @param hash hash of secret, also the proposal id
     * @param callback a callback interface
     */
    void lockSelf(HTLCResource htlcResource, String hash, Callback callback);

    /**
     * lock counterpart asset with transaction verification
     *
     * @param htlcResource counterpart htlc resource
     * @param hash hash of secret, also the proposal id
     * @param callback a callback interface
     */
    void lockCounterparty(HTLCResource htlcResource, String hash, Callback callback);

    /**
     * unlock counterpart asset with transaction verification
     *
     * @param htlcResource counterpart htlc resource
     * @param hash hash of secret, also the proposal id
     * @param secret secret
     * @param callback a callback interface
     */
    void unlockCounterparty(
            HTLCResource htlcResource, String hash, String secret, Callback callback);

    /**
     * rollback self asset
     *
     * @param htlcResource self htlc resource
     * @param hash hash of secret, also the proposal id
     * @param callback a callback interface
     */
    void rollback(HTLCResource htlcResource, String hash, Callback callback);

    /**
     * verify transaction
     *
     * @param counterpartyResource counterparty resource
     * @param transactionValidator expected transaction data
     */
    void verifyHtlcTransaction(
            Resource counterpartyResource,
            TransactionValidator transactionValidator,
            VerifyCallback callback);

    /**
     * get all proposal ids
     *
     * @param htlcResource self htlc resource
     * @param callback a callback interface
     */
    void getProposalIDs(HTLCResource htlcResource, Callback callback);

    /**
     * get proposal info by id
     *
     * @param resource self htlc resource
     * @param ua account to sign
     * @param hash hash of secret, also the proposal id
     * @param callback a callback interface
     */
    void getProposalInfo(Resource resource, UniversalAccount ua, String hash, Callback callback);

    /**
     * decode proposal with string array inputs
     *
     * @param info fields of proposal
     * @return proposal
     * @throws WeCrossException decode failed
     */
    HTLCProposal decodeProposal(String[] info) throws WeCrossException;

    /**
     * delete proposal
     *
     * @param htlcResource self htlc resource
     * @param hash hash of secret, also the proposal id
     * @param callback a callback interface
     */
    void deleteProposalID(HTLCResource htlcResource, String hash, Callback callback);

    /**
     * get transaction info of new proposal
     *
     * @param htlcResource self htlc resource
     * @param hash hash of secret, also the proposal id
     * @param callback a callback interface
     */
    void getNewProposalTxInfo(HTLCResource htlcResource, String hash, Callback callback);

    /**
     * set lock state of counterparty
     *
     * @param htlcResource self htlc resource
     * @param hash hash of secret, also the proposal id
     * @param callback a callback interface
     */
    void setCounterpartyLockState(HTLCResource htlcResource, String hash, Callback callback);

    /**
     * set unlock state of counterparty
     *
     * @param resource self htlc resource
     * @param ua account to sign
     * @param hash hash of secret, also the proposal id
     * @param callback a callback interface
     */
    void setCounterpartyUnlockState(
            Resource resource, UniversalAccount ua, String hash, Callback callback);

    /**
     * set rollback state of counterparty
     *
     * @param htlcResource self htlc resource
     * @param hash hash of secret, also the proposal id
     * @param callback a callback interface
     */
    void setCounterpartyRollbackState(HTLCResource htlcResource, String hash, Callback callback);
}
