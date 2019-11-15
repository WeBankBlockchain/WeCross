package com.webank.wecross.stub.bcos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecross.core.HashUtils;
import com.webank.wecross.exception.Status;
import com.webank.wecross.network.config.ConfigType;
import com.webank.wecross.proof.BlockHeaderProof;
import com.webank.wecross.proof.MerkleProof;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import org.fisco.bcos.channel.client.CallContract;
import org.fisco.bcos.channel.client.CallResult;
import org.fisco.bcos.channel.client.ReceiptEncoder;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.client.TransactionResource;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Int256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.Hash;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceiptWithProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionWithProof;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSContractResource extends BCOSResource {
    private Logger logger = LoggerFactory.getLogger(BCOSContractResource.class);

    private Boolean isInit = false;
    @JsonIgnore private Web3j web3;
    @JsonIgnore private String contractAddress;
    private CallContract callContract;
    private String checksum;

    public void init(Service service, Web3j web3j, Credentials credentials) {
        if (!isInit) {
            callContract = new CallContract(credentials, web3j);
            isInit = true;
        }
    }

    private Type<?>[] javaType2BCOSType(Object[] args) throws Exception {
        Type<?>[] data = new Type[args.length];

        int i = 0;
        for (Object obj : args) {
            if (obj instanceof String) {
                Utf8String utf8String = new Utf8String((String) obj);
                data[i++] = utf8String;
            } else if (obj instanceof Integer) {
                Int256 int256 = new Int256((Integer) obj);
                data[i++] = int256;
            } else {
                throw new Exception("Unspport type");
            }
        }

        return data;
    }

    @Override
    public String getType() {
        return ConfigType.RESOURCE_TYPE_BCOS_CONTRACT;
    }

    @Override
    public GetDataResponse getData(GetDataRequest request) {
        GetDataResponse getDataResponse = new GetDataResponse();
        getDataResponse.setErrorCode(Status.NONSENSE_CALL);
        getDataResponse.setErrorMessage("Not supported by BCOS_CONTRACT");
        return getDataResponse;
    }

    @Override
    public SetDataResponse setData(SetDataRequest request) {
        SetDataResponse setDataResponse = new SetDataResponse();
        setDataResponse.setErrorCode(Status.NONSENSE_CALL);
        setDataResponse.setErrorMessage("Not supported by BCOS_CONTRACT");
        return setDataResponse;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {
        BCOSResponse bcosResponse = new BCOSResponse();

        try {
            TransactionReceipt transactionReceipt =
                    callContract.sendTransaction(
                            contractAddress,
                            request.getMethod(),
                            javaType2BCOSType(request.getArgs()));

            if (transactionReceipt == null) {
                bcosResponse.setErrorCode(Status.RPC_ERROR);
                bcosResponse.setErrorMessage("error in RPC");
            } else {

                // status: 0x00 - 0x1a, errorCode: 0 - 26
                String status = transactionReceipt.getStatus();
                Integer errorCode = Integer.valueOf(status.substring(2), 16);
                if (errorCode == 0) {
                    bcosResponse =
                            generateResponseWithProof(
                                    transactionReceipt); // query proof, verify and set to response
                    bcosResponse.setResult(new Object[] {transactionReceipt.getOutput()});
                }
                bcosResponse.setErrorCode(errorCode);
                bcosResponse.setErrorMessage(transactionReceipt.getMessage());
            }
        } catch (Exception e) {
            bcosResponse.setErrorCode(Status.INTERNAL_ERROR);
            bcosResponse.setErrorMessage(e.getMessage());
        }

        return bcosResponse;
    }

    @Override
    public TransactionRequest createRequest() {
        return new BCOSRequest();
    }

    @Override
    public TransactionResponse call(TransactionRequest request) {
        BCOSResponse bcosResponse = new BCOSResponse();

        try {
            CallResult callResult =
                    callContract.call(
                            contractAddress,
                            request.getMethod(),
                            javaType2BCOSType(request.getArgs()));

            String status = callResult.getStatus();
            Integer errorCode = Integer.valueOf(status.substring(2), 16);
            if (errorCode == 0) {
                bcosResponse.setResult(new Object[] {callResult.getOutput()});
            }
            bcosResponse.setErrorCode(errorCode);
            bcosResponse.setErrorMessage(callResult.getMessage());

        } catch (Exception e) {
            bcosResponse.setErrorCode(Status.INTERNAL_ERROR);
            bcosResponse.setErrorMessage(e.getMessage());
        }

        return bcosResponse;
    }

    @Override
    public void registerEventHandler(EventCallback callback) {}

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    @Override
    public String getChecksum() {
        try {
            if (checksum == null || checksum.equals("")) {
                checksum = HashUtils.sha256String(contractAddress);
            }
            return checksum;

        } catch (Exception e) {
            logger.error("Caculate checksum exception: " + e);
        }
        return null;
    }

    private MerkleProof getTransactionProof(String transactionHash, String txRoot)
            throws Exception {
        /// *
        TransactionResource transactionResource = new TransactionResource(web3);
        TransactionWithProof proofResponse =
                transactionResource.getTransactionWithProof(transactionHash, txRoot);
        /*/
        TransactionWithProof proofResponse =
                web3.getTransactionByHashWithProof(transactionHash).send();
        //*/
        if (proofResponse == null || proofResponse.getTransactionWithProof() == null) {
            throw new Exception(
                    "Verify transaction proof failed. txHash:"
                            + transactionHash
                            + " txRoot:"
                            + txRoot);
        }

        BCOSTransactionProof leafProof =
                new BCOSTransactionProof(proofResponse.getTransactionWithProof().getTransaction());
        MerkleProof proof = new MerkleProof();
        proof.setRoot(txRoot);
        proof.setPath(proofResponse.getTransactionWithProof().getTxProof());
        proof.setLeaf(leafProof);
        return proof;
    }

    private MerkleProof getTransactionReceiptProof(String transactionHash, String receiptRoot)
            throws Exception {
        /// *
        TransactionResource transactionResource = new TransactionResource(web3);
        TransactionReceiptWithProof proofResponse =
                transactionResource.getTransactionReceiptWithProof(transactionHash, receiptRoot);
        /*/
        TransactionReceiptWithProof proofResponse =
                web3.getTransactionReceiptByHashWithProof(transactionHash).send();
        //*/
        if (proofResponse == null || proofResponse.getTransactionReceiptWithProof() == null) {
            throw new Exception(
                    "Verify transaction receipt proof failed. transactionHash:"
                            + transactionHash
                            + " receiptRoot:"
                            + receiptRoot);
        }

        BCOSReceiptProof leafProof =
                new BCOSReceiptProof(
                        proofResponse.getTransactionReceiptWithProof().getTransactionReceipt());
        MerkleProof proof = new MerkleProof();
        proof.setRoot(receiptRoot);
        proof.setPath(proofResponse.getTransactionReceiptWithProof().getReceiptProof());
        proof.setLeaf(leafProof);
        return proof;
    }

    private BCOSResponse generateResponseWithProof(TransactionReceipt transactionReceipt)
            throws Exception {
        // Get header proof

        BcosBlock.Block block =
                web3.getBlockByNumber(
                                DefaultBlockParameter.valueOf(transactionReceipt.getBlockNumber()),
                                false)
                        .send()
                        .getBlock();
        BlockHeaderProof headerProof = new BlockHeaderProof();
        headerProof.setBlockNumber(block.getNumber());
        headerProof.setHash(block.getHash());
        headerProof.addRoot(block.getStateRoot());
        headerProof.addRoot(block.getTransactionsRoot());
        headerProof.addRoot(block.getReceiptsRoot());

        // Get leaf
        MerkleProof txProof =
                getTransactionProof(
                        transactionReceipt.getTransactionHash(), block.getTransactionsRoot());
        MerkleProof receiptsProof =
                getTransactionReceiptProof(
                        transactionReceipt.getTransactionHash(), block.getReceiptsRoot());

        BCOSResponse bcosResponse = new BCOSResponse();
        bcosResponse.setBlockHeader(headerProof);
        bcosResponse.setProofs(new MerkleProof[] {txProof, receiptsProof});

        bcosResponse.setHash(transactionReceipt.getTransactionHash());

        String receiptRlp = ReceiptEncoder.encode(transactionReceipt);
        String receiptHash = Hash.sha3(receiptRlp);
        bcosResponse.addExtraHash(receiptHash);

        return bcosResponse;
    }

    public void setWeb3(Web3j web3) {
        this.web3 = web3;
    }
}
