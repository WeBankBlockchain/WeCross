package com.webank.wecross.stub.bcos;

import com.webank.wecross.proposal.ProposalFactory;
import com.webank.wecross.restserver.request.ProposalRequest;
import java.math.BigInteger;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.TransactionManager;

public class BCOSProposalFactory implements ProposalFactory {
    private static final BigInteger gasPrice = new BigInteger("3000000000");
    private static final BigInteger gasLimit = new BigInteger("3000000000");
    private static final BigInteger value = BigInteger.ZERO;

    private String contractAddress;
    private TransactionManager transactionManager;

    public BCOSProposalFactory(String contractAddress, Web3j web3j, Credentials credentials) {
        this.contractAddress = contractAddress;
        this.transactionManager = Contract.getTheTransactionManager(web3j, credentials);
    }

    @Override
    public BCOSProposal build(ProposalRequest request) throws Exception {
        String data = BCOSProposal.encodeRequestToInputData(request);
        BCOSProposal proposal = new BCOSProposal(request.getSeq());
        ExtendedRawTransaction rawTransaction =
                transactionManager.createTransaction(
                        gasPrice, gasLimit, contractAddress, data, value, null);
        proposal.load(rawTransaction);
        return proposal;
    }

    @Override
    public BCOSProposal buildFromBytes(byte[] proposalBytes) throws Exception {
        BCOSProposal proposal = new BCOSProposal(0);
        proposal.loadBytes(proposalBytes);
        return proposal;
    }
}
