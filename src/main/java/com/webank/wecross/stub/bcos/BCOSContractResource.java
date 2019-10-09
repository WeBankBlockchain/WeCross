package com.webank.wecross.stub.bcos;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.GetDataRequest;
import com.webank.wecross.resource.GetDataResponse;
import com.webank.wecross.resource.SetDataRequest;
import com.webank.wecross.resource.SetDataResponse;
import com.webank.wecross.resource.TransactionRequest;
import com.webank.wecross.resource.TransactionResponse;
import org.fisco.bcos.channel.client.CallContract;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Int256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;

public class BCOSContractResource extends BCOSResource {
    private Boolean isInit = false;
    private String contractAddress;
    private CallContract callContract;

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
            if (obj instanceof String) {
                Utf8String utf8String = new Utf8String((String) obj);
                data[i++] = utf8String;
            } else if (obj instanceof Integer) {
                Int256 int256 = new Int256((Integer) obj);
                data[i++] = int256;
            } else {
                throw new WeCrossException(-5, "Unspport type");
            }
        }

        return data;
    }

    @Override
    public GetDataResponse getData(GetDataRequest request) {
        return null;
    }

    @Override
    public SetDataResponse setData(SetDataRequest request) {
        return null;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {
        BCOSResponse bcosResponse = new BCOSResponse();

        try {
            String result =
                    callContract.sendTransaction(
                            contractAddress,
                            request.getMethod(),
                            javaType2BCOSType(request.getArgs()));

            if (result.isEmpty()) {
                bcosResponse.setErrorCode(1);
                bcosResponse.setErrorMessage(
                        "Result is empty, please check contract address and arguments");
            } else {
                bcosResponse.setErrorCode(0);
                bcosResponse.setErrorMessage("");
                bcosResponse.setResult(new Object[] {result});
            }
        } catch (Exception e) {
            bcosResponse.setErrorCode(2);
            bcosResponse.setErrorMessage("Unexpected error: " + e.getMessage());
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
            String result =
                    callContract.call(
                            contractAddress,
                            request.getMethod(),
                            javaType2BCOSType(request.getArgs()));

            if (result.isEmpty()) {
                bcosResponse.setErrorCode(1);
                bcosResponse.setErrorMessage(
                        "Result is empty, please check contract address and arguments");
            } else {
                bcosResponse.setErrorCode(0);
                bcosResponse.setErrorMessage("");
                bcosResponse.setResult(new Object[] {result});
            }
        } catch (Exception e) {
            bcosResponse.setErrorCode(2);
            bcosResponse.setErrorMessage("Unexpected error: " + e.getMessage());
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
}
