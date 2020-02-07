package com.webank.wecross.stub.bcos;

import com.webank.wecross.proposal.Proposal;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.utils.ExtendedTransactionDecoderV2;
import java.util.Arrays;
import java.util.Collections;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.ExtendedTransactionEncoder;
import org.fisco.bcos.web3j.utils.Numeric;

public class BCOSProposal extends Proposal {
    private ExtendedRawTransaction proposalTransaction;
    private byte[] proposalBytes;

    public BCOSProposal(int seq, byte[] proposalBytes) throws Exception {
        super(seq);
        loadBytes(proposalBytes);
    }

    public BCOSProposal(int seq) {
        super(seq);
    }

    @Override
    public byte[] getBytesToSign() {
        return proposalBytes; // sha3 is in WeCross SDK
    }

    @Override
    public byte[] getBytes() {
        return proposalBytes;
    }

    @Override
    public void sendSignedPayload(byte[] signBytes) throws Exception {}

    @Override
    public void loadBytes(byte[] proposalBytes) throws Exception {
        this.proposalBytes = proposalBytes;
        proposalTransaction = ExtendedTransactionDecoderV2.decode(proposalBytes);
    }

    @Override
    public Boolean isEqualsRequest(TransactionRequest request) throws Exception {
        if (proposalTransaction == null) {
            throw new Exception("BCOS proposal " + this.getSeq() + " has not been loaded");
        }

        String requestData = encodeRequest(request);
        String data = proposalTransaction.getData();
        return requestData.equals(data);
    }

    public void setProposalTransaction(ExtendedRawTransaction proposalTransaction) {
        this.proposalTransaction = proposalTransaction;
        this.proposalBytes = ExtendedTransactionEncoder.encode(proposalTransaction);
    }

    public static String encodeRequest(TransactionRequest request) throws Exception {
        String functionName = request.getMethod();
        Type<?>[] args = BCOSContractResource.javaType2BCOSType(request.getArgs());
        final Function function =
                new Function(
                        functionName,
                        Arrays.<Type>asList(args),
                        Collections.<TypeReference<?>>emptyList());

        String data = FunctionEncoder.encode(function);
        return Numeric.cleanHexPrefix(data);
    }
}
