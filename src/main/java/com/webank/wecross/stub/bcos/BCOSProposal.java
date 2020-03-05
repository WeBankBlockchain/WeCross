package com.webank.wecross.stub.bcos;

import com.webank.wecross.proposal.Proposal;
import com.webank.wecross.restserver.request.ProposalRequest;
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
    private ExtendedRawTransaction innerBCOSTransaction;
    private byte[] proposalBytes;

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

    public ExtendedRawTransaction getInnerBCOSTransaction() {
        return this.innerBCOSTransaction;
    }

    @Override
    public void sendSignedPayload(byte[] signBytes) throws Exception {}

    @Override
    public void loadBytes(byte[] proposalBytes) throws Exception {
        this.proposalBytes = proposalBytes;
        innerBCOSTransaction = ExtendedTransactionDecoderV2.decode(proposalBytes);
    }

    @Override
    public Boolean isEqualsRequest(TransactionRequest request) throws Exception {
        if (innerBCOSTransaction == null) {
            throw new Exception("BCOS proposal " + this.getSeq() + " has not been loaded");
        }

        String requestData = encodeRequestToInputData(request);
        String data = innerBCOSTransaction.getData();
        return requestData.equals(data);
    }

    @Override
    public Boolean isEqualsRequest(ProposalRequest request) throws Exception {
        if (innerBCOSTransaction == null) {
            throw new Exception("BCOS proposal " + this.getSeq() + " has not been loaded");
        }

        String requestData = encodeRequestToInputData(request);
        String data = innerBCOSTransaction.getData();
        return requestData.equals(data);
    }

    public void load(ExtendedRawTransaction innerBCOSTransaction) {
        this.innerBCOSTransaction = innerBCOSTransaction;
        this.proposalBytes = ExtendedTransactionEncoder.encode(innerBCOSTransaction);
    }

    public static String encodeRequestToInputData(TransactionRequest request) throws Exception {
        return encodeRequestToInputData(request.getMethod(), request.getArgs());
    }

    public static String encodeRequestToInputData(ProposalRequest request) throws Exception {
        return encodeRequestToInputData(request.getMethod(), request.getArgs());
    }

    public static String encodeRequestToInputData(String method, Object[] args) throws Exception {
        Type<?>[] typeArgs = BCOSContractResource.javaType2BCOSType(args);
        final Function function =
                new Function(
                        method,
                        Arrays.<Type>asList(typeArgs),
                        Collections.<TypeReference<?>>emptyList());

        String data = FunctionEncoder.encode(function);
        return Numeric.cleanHexPrefix(data);
    }
}
