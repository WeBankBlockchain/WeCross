package com.webank.wecross.test.Mock.web3j;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.List;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.fisco.bcos.web3j.protocol.core.JsonRpc2_0Web3j;
import org.fisco.bcos.web3j.protocol.core.Request;
import org.fisco.bcos.web3j.protocol.core.methods.request.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosFilter;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosLog;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosTransaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosTransactionReceipt;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockHash;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.Code;
import org.fisco.bcos.web3j.protocol.core.methods.response.ConsensusStatus;
import org.fisco.bcos.web3j.protocol.core.methods.response.GenerateGroup;
import org.fisco.bcos.web3j.protocol.core.methods.response.GroupList;
import org.fisco.bcos.web3j.protocol.core.methods.response.GroupPeers;
import org.fisco.bcos.web3j.protocol.core.methods.response.Log;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeIDList;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion;
import org.fisco.bcos.web3j.protocol.core.methods.response.ObserverList;
import org.fisco.bcos.web3j.protocol.core.methods.response.PbftView;
import org.fisco.bcos.web3j.protocol.core.methods.response.Peers;
import org.fisco.bcos.web3j.protocol.core.methods.response.PendingTransactions;
import org.fisco.bcos.web3j.protocol.core.methods.response.PendingTxSize;
import org.fisco.bcos.web3j.protocol.core.methods.response.SealerList;
import org.fisco.bcos.web3j.protocol.core.methods.response.SendTransaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.StartGroup;
import org.fisco.bcos.web3j.protocol.core.methods.response.SyncStatus;
import org.fisco.bcos.web3j.protocol.core.methods.response.SystemConfig;
import org.fisco.bcos.web3j.protocol.core.methods.response.TotalTransactionCount;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceiptWithProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionWithProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.UninstallFilter;
import org.fisco.bcos.web3j.protocol.websocket.events.LogNotification;
import org.fisco.bcos.web3j.protocol.websocket.events.NewHeadsNotification;

public class MockWeb3j extends JsonRpc2_0Web3j {
    public MockWeb3j() {
        super(null);
    }

    @Override
    public Request<?, NodeVersion> getNodeVersion() {
        return new MockWeb3jRequest<>();
    }

    @Override
    public Request<?, BlockNumber> getBlockNumber() {
        return null;
    }

    @Override
    public Request<?, PbftView> getPbftView() {
        return null;
    }

    @Override
    public Request<?, SealerList> getSealerList() {
        return null;
    }

    @Override
    public Request<?, ObserverList> getObserverList() {
        return null;
    }

    @Override
    public Request<?, NodeIDList> getNodeIDList() {
        return null;
    }

    @Override
    public Request<?, GroupList> getGroupList() {
        return null;
    }

    @Override
    public Request<?, GroupPeers> getGroupPeers() {
        return null;
    }

    @Override
    public Request<?, Peers> getPeers() {
        return null;
    }

    @Override
    public Request<?, ConsensusStatus> getConsensusStatus() {
        return null;
    }

    @Override
    public Request<?, SyncStatus> getSyncStatus() {
        return null;
    }

    @Override
    public Request<?, SystemConfig> getSystemConfigByKey(String key) {
        return null;
    }

    @Override
    public Request<?, Code> getCode(String address, DefaultBlockParameter defaultBlockParameter) {
        return null;
    }

    @Override
    public Request<?, TotalTransactionCount> getTotalTransactionCount() {
        return null;
    }

    @Override
    public Request<?, BcosBlock> getBlockByHash(
            String blockHash, boolean returnFullTransactionObjects) {
        return null;
    }

    @Override
    public Request<?, BcosBlock> getBlockByNumber(
            DefaultBlockParameter defaultBlockParameter, boolean returnFullTransactionObjects) {
        return null;
    }

    @Override
    public Request<?, BlockHash> getBlockHashByNumber(DefaultBlockParameter defaultBlockParameter) {
        return null;
    }

    @Override
    public Request<?, BcosTransaction> getTransactionByHash(String transactionHash) {
        return null;
    }

    @Override
    public Request<?, TransactionWithProof> getTransactionByHashWithProof(String transactionHash) {
        return null;
    }

    @Override
    public Request<?, BcosTransaction> getTransactionByBlockHashAndIndex(
            String blockHash, BigInteger transactionIndex) {
        return null;
    }

    @Override
    public Request<?, BcosTransaction> getTransactionByBlockNumberAndIndex(
            DefaultBlockParameter defaultBlockParameter, BigInteger transactionIndex) {
        return null;
    }

    @Override
    public Request<?, BcosTransactionReceipt> getTransactionReceipt(String transactionHash) {
        return null;
    }

    @Override
    public Request<?, TransactionReceiptWithProof> getTransactionReceiptByHashWithProof(
            String transactionHash) {
        return null;
    }

    @Override
    public Request<?, PendingTransactions> getPendingTransaction() {
        return null;
    }

    @Override
    public BigInteger getBlockNumberCache() {
        return BigInteger.valueOf(0);
    }

    @Override
    public Request<?, PendingTxSize> getPendingTxSize() {
        return null;
    }

    @Override
    public Request<?, Call> call(
            Transaction transaction, DefaultBlockParameter defaultBlockParameter) {
        return null;
    }

    @Override
    public Request<?, SendTransaction> sendRawTransaction(String signedTransactionData) {
        return null;
    }

    @Override
    public Request<?, GenerateGroup> generateGroup(
            int groupId, int timestamp, List<String> nodeList) {
        return null;
    }

    @Override
    public Request<?, StartGroup> startGroup(int groupId) {
        return null;
    }

    @Override
    public Request<?, BcosFilter> newPendingTransactionFilter() {
        return null;
    }

    @Override
    public Request<?, BcosFilter> newBlockFilter() {
        return null;
    }

    @Override
    public Request<?, BcosLog> getFilterChanges(BigInteger filterId) {
        return null;
    }

    @Override
    public Request<?, UninstallFilter> getUninstallFilter(BigInteger filterId) {
        return null;
    }

    @Override
    public Request<?, BcosFilter> newFilter(
            org.fisco.bcos.web3j.protocol.core.methods.request.BcosFilter ethFilter) {
        return null;
    }

    @Override
    public Flowable<Log> logFlowable(
            org.fisco.bcos.web3j.protocol.core.methods.request.BcosFilter filter) {
        return null;
    }

    @Override
    public Flowable<String> blockHashFlowable() {
        return null;
    }

    @Override
    public Flowable<String> pendingTransactionHashFlowable() {
        return null;
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
            transactionFlowable() {
        return null;
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
            pendingTransactionFlowable() {
        return null;
    }

    @Override
    public Flowable<BcosBlock> blockFlowable(boolean fullTransactionObjects) {
        return null;
    }

    @Override
    public Flowable<BcosBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return null;
    }

    @Override
    public Flowable<BcosBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects,
            boolean ascending) {
        return null;
    }

    @Override
    public Flowable<BcosBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            boolean fullTransactionObjects,
            Flowable<BcosBlock> onCompleteFlowable) {
        return null;
    }

    @Override
    public Flowable<BcosBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return null;
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
            replayPastTransactionsFlowable(
                    DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        return null;
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
            replayPastTransactionsFlowable(DefaultBlockParameter startBlock) {
        return null;
    }

    @Override
    public Flowable<BcosBlock> replayPastAndFutureBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return null;
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
            replayPastAndFutureTransactionsFlowable(DefaultBlockParameter startBlock) {
        return null;
    }

    @Override
    public Flowable<NewHeadsNotification> newHeadsNotifications() {
        return null;
    }

    @Override
    public Flowable<LogNotification> logsNotifications(
            List<String> addresses, List<String> topics) {
        return null;
    }
}
