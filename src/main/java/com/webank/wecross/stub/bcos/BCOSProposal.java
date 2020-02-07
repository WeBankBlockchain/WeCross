package com.webank.wecross.stub.bcos;

import com.webank.wecross.proposal.Proposal;
import com.webank.wecross.restserver.request.TransactionRequest;
import java.util.Arrays;
import java.util.Collections;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.ExtendedTransactionDecoder;
import org.fisco.bcos.web3j.utils.Numeric;

public class BCOSProposal extends Proposal {
    private ExtendedRawTransaction proposalTransaction;
    private byte[] proposalBytes;

    public BCOSProposal(int seq, byte[] proposalBytes) throws Exception {
        super(seq);
        loadBytes(proposalBytes);
    }

    @Override
    public byte[] getBytesToSign() {
        return proposalBytes; // sha3 is in WeCross SDK
    }

    @Override
    public void sendSignedPayload(byte[] signBytes) throws Exception {}

    @Override
    public void loadBytes(byte[] proposalBytes) throws Exception {
        this.proposalBytes = proposalBytes;
        proposalTransaction = ExtendedTransactionDecoder.decode(Numeric.toHexString(proposalBytes));
    }

    @Override
    public Boolean isEqualsRequest(TransactionRequest request) throws Exception {
        if (proposalTransaction == null) {
            throw new Exception("BCOS proposal " + this.getSeq() + " has not been loaded");
        }

        String functionName = request.getMethod();
        Type<?>[] args = BCOSContractResource.javaType2BCOSType(request.getArgs());
        final Function function =
                new Function(
                        functionName,
                        Arrays.<Type>asList(args),
                        Collections.<TypeReference<?>>emptyList());

        String data = FunctionEncoder.encode(function);

        return data.equals(proposalTransaction.getData());
    }
}
