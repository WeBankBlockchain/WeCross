package com.webank.wecross.stub.bcos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecross.core.HashUtils;
import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.proof.BlockHeaderProof;
import com.webank.wecross.proof.MerkleProof;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import com.webank.wecross.stub.bcos.contract.CallContract;
import com.webank.wecross.stub.bcos.contract.CallResult;
import com.webank.wecross.utils.CommonUtils;
import com.webank.wecross.utils.WeCrossType;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.channel.client.ReceiptEncoder;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.client.TransactionResource;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Int256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
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

    private Type<?>[] javaType2BCOSType(Object[] args) throws WeCrossException {
        Type<?>[] data = new Type[args.length];

        int i = 0;
        for (Object obj : args) {
            switch (CommonUtils.getTypeEnum(obj)) {
                case Int:
                    {
                        data[i++] = new Int256((Integer) obj);
                        break;
                    }
                case String:
                    {
                        data[i++] = new Utf8String((String) obj);
                        break;
                    }
                case IntArray:
                    {
                        List<Int256> list = new ArrayList<>();
                        if (obj.getClass().equals(ArrayList.class)) {
                            for (int value : (List<Integer>) obj) {
                                list.add(new Int256(value));
                            }
                        } else {
                            for (int value : (int[]) obj) {
                                list.add(new Int256(value));
                            }
                        }
                        data[i++] = new DynamicArray<Int256>(list);
                        break;
                    }
                case StringArray:
                    {
                        List<Utf8String> list = new ArrayList<>();
                        if (obj.getClass().equals(ArrayList.class)) {
                            for (String value : (List<String>) obj) {
                                list.add(new Utf8String(value));
                            }
                        } else {
                            for (String value : (String[]) obj) {
                                list.add(new Utf8String(value));
                            }
                        }
                        data[i++] = new DynamicArray<Utf8String>(list);
                        break;
                    }
                default:
                    {
                        break;
                    }
            }
        }

        return data;
    }

    @Override
    public String getType() {
        return WeCrossType.RESOURCE_TYPE_BCOS_CONTRACT;
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
        BCOSTransactionResponse bcosTransactionResponse = newBCOSTransactionResponse();

        try {
            Type<?>[] args = javaType2BCOSType(request.getArgs());
            TransactionReceipt transactionReceipt =
                    callContract.sendTransaction(contractAddress, request.getMethod(), args);
            logger.debug("sendTransaction: {}, {}, {}", contractAddress, request.getMethod(), args);

            // status: 0x00 - 0x1a, errorCode: 0 - 26
            String status = transactionReceipt.getStatus();
            Integer errorCode = Integer.valueOf(status.substring(2), 16);
            if (errorCode == 0) {
                try {
                    bcosTransactionResponse =
                            generateResponseWithProof(
                                    transactionReceipt); // query proof, verify and set to response
                } catch (Exception e) {
                    throw new WeCrossException(
                            Status.INTERNAL_ERROR, "Error in Merkle proof: " + e.getMessage());
                }

                List<Object> result =
                        callContract.decode(transactionReceipt.getOutput(), request.getRetTypes());
                bcosTransactionResponse.setResult(result.toArray());
            }
            bcosTransactionResponse.setErrorCode(errorCode);
            bcosTransactionResponse.setErrorMessage(transactionReceipt.getMessage());

        } catch (WeCrossException e) {
            bcosTransactionResponse.setErrorCode(e.getErrorCode());
            bcosTransactionResponse.setErrorMessage(e.getMessage());
        } catch (Exception e) {
            bcosTransactionResponse.setErrorCode(Status.INTERNAL_ERROR);
            bcosTransactionResponse.setErrorMessage(e.getMessage());
        }

        return bcosTransactionResponse;
    }

    @Override
    public TransactionRequest createRequest() {
        return new BCOSRequest();
    }

    @Override
    public TransactionResponse call(TransactionRequest request) {
        BCOSTransactionResponse bcosTransactionResponse = newBCOSTransactionResponse();

        try {
            Type<?>[] args = javaType2BCOSType(request.getArgs());
            CallResult callResult = callContract.call(contractAddress, request.getMethod(), args);
            logger.debug("call: {}, {}, {}", contractAddress, request.getMethod(), args);
            String status = callResult.getStatus();
            Integer errorCode = Integer.valueOf(status.substring(2), 16);
            if (errorCode == 0) {
                List<Object> result =
                        callContract.decode(callResult.getOutput(), request.getRetTypes());
                bcosTransactionResponse.setResult(result.toArray());
            }
            bcosTransactionResponse.setErrorCode(errorCode);
            bcosTransactionResponse.setErrorMessage(callResult.getMessage());

        } catch (WeCrossException e) {
            bcosTransactionResponse.setErrorCode(e.getErrorCode());
            bcosTransactionResponse.setErrorMessage(e.getMessage());
        } catch (Exception e) {
            bcosTransactionResponse.setErrorCode(Status.INTERNAL_ERROR);
            bcosTransactionResponse.setErrorMessage(e.getMessage());
        }

        return bcosTransactionResponse;
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

    private BCOSTransactionResponse generateResponseWithProof(TransactionReceipt transactionReceipt)
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

        BCOSTransactionResponse bcosTransactionResponse = newBCOSTransactionResponse();
        bcosTransactionResponse.setBlockHeader(headerProof);
        bcosTransactionResponse.setProofs(new MerkleProof[] {txProof, receiptsProof});

        bcosTransactionResponse.setHash(transactionReceipt.getTransactionHash());

        String receiptRlp = ReceiptEncoder.encode(transactionReceipt);
        String receiptHash = Hash.sha3(receiptRlp);
        bcosTransactionResponse.addExtraHash(receiptHash);

        return bcosTransactionResponse;
    }

    public void setWeb3(Web3j web3) {
        this.web3 = web3;
    }

    private BCOSTransactionResponse newBCOSTransactionResponse() {
        BCOSTransactionResponse response = new BCOSTransactionResponse();
        boolean isGuomi = EncryptType.encryptType == 1; // get SDK global config, maybe modify later
        if (isGuomi) {
            response.setEncryptType(WeCrossType.ENCRYPT_TYPE_GUOMI);
        }
        return response;
    }
}
