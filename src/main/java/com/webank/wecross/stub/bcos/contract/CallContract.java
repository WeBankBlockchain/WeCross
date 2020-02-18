package com.webank.wecross.stub.bcos.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.Utils;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.StaticArray;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Int256;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint160;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameterName;
import org.fisco.bcos.web3j.protocol.core.methods.request.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.utils.Numeric;

public class CallContract {

    private static final BigInteger gasPrice = new BigInteger("3000000000");
    private static final BigInteger gasLimit = new BigInteger("3000000000");

    private Credentials credentials;
    private Web3j web3j;

    public CallContract(Credentials credentials, Web3j web3j) {
        this.credentials = credentials;
        this.web3j = web3j;
    }

    public CallResult call(String contractAddress, String funcName, Type... args) {
        return call(contractAddress, credentials.getAddress(), funcName, args);
    }

    public CallResult call(String contractAddress, String account, String funcName, Type... args) {
        final Function function =
                new Function(
                        funcName,
                        Arrays.<Type>asList(args),
                        Collections.<TypeReference<?>>emptyList());
        String data = FunctionEncoder.encode(function);
        Call ethCall;
        try {
            ethCall =
                    web3j.call(
                                    Transaction.createEthCallTransaction(
                                            account, contractAddress, data),
                                    DefaultBlockParameterName.LATEST)
                            .send();
        } catch (Exception e) {
            return new CallResult(StatusCode.ExceptionCatched, e.getMessage(), "0x");
        }

        Call.CallOutput callOutput = ethCall.getValue();
        if (callOutput != null) {
            return new CallResult(
                    callOutput.getStatus(),
                    StatusCode.getStatusMessage(callOutput.getStatus()),
                    callOutput.getOutput());
        } else {
            return new CallResult(
                    StatusCode.ErrorInRPC,
                    StatusCode.getStatusMessage(StatusCode.ErrorInRPC),
                    "0x");
        }
    }

    public TransactionReceipt sendTransaction(
            String contractAddress, String funcName, Type... args) {
        final Function function =
                new Function(
                        funcName,
                        Arrays.<Type>asList(args),
                        Collections.<TypeReference<?>>emptyList());

        TransactionReceipt transactionReceipt = new TransactionReceipt();
        try {
            ExecuteTransaction executeTransaction =
                    new ExecuteTransaction(contractAddress, web3j, credentials, gasPrice, gasLimit);

            transactionReceipt = executeTransaction.send(function);
            String status = transactionReceipt.getStatus();
            transactionReceipt.setMessage(StatusCode.getStatusMessage(status));

        } catch (Exception e) {
            transactionReceipt.setStatus(StatusCode.ExceptionCatched);
            transactionReceipt.setMessage(e.getMessage());
            transactionReceipt.setOutput("0x");
        }
        return transactionReceipt;
    }

    public TransactionReceipt sendTransaction(
            BigInteger gasPrice,
            BigInteger gasLimit,
            String contractAddress,
            String funcName,
            Type... args) {
        final Function function =
                new Function(
                        funcName,
                        Arrays.<Type>asList(args),
                        Collections.<TypeReference<?>>emptyList());
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        try {
            ExecuteTransaction executeTransaction =
                    new ExecuteTransaction(contractAddress, web3j, credentials, gasPrice, gasLimit);
            transactionReceipt = executeTransaction.send(function);

            String status = transactionReceipt.getStatus();
            transactionReceipt.setMessage(StatusCode.getStatusMessage(status));

        } catch (Exception e) {
            transactionReceipt.setStatus(StatusCode.ExceptionCatched);
            transactionReceipt.setMessage(e.getMessage());
            transactionReceipt.setOutput("0x");
        }
        return transactionReceipt;
    }

    public TransactionReceipt sendTransaction(
            String contractAddress, byte[] signedTransactionBytes) {
        String signedTransaction = Numeric.toHexString(signedTransactionBytes);
        ExecuteTransaction executeTransaction =
                new ExecuteTransaction(contractAddress, web3j, credentials, gasPrice, gasLimit);
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        try {
            transactionReceipt = executeTransaction.sendSignedTransaction(signedTransaction);
            String status = transactionReceipt.getStatus();
            transactionReceipt.setMessage(StatusCode.getStatusMessage(status));

        } catch (Exception e) {
            transactionReceipt.setStatus(StatusCode.ExceptionCatched);
            transactionReceipt.setMessage(e.getMessage());
            transactionReceipt.setOutput("0x");
        }
        return transactionReceipt;
    }

    public void asyncSendTransaction(
            TransactionSucCallback callback,
            String contractAddress,
            String funcName,
            Type... args) {
        final Function function =
                new Function(
                        funcName,
                        Arrays.<Type>asList(args),
                        Collections.<TypeReference<?>>emptyList());

        ExecuteTransaction executeTransaction =
                new ExecuteTransaction(contractAddress, web3j, credentials, gasPrice, gasLimit);

        executeTransaction.asyncSend(function, callback);
    }

    public void asyncSendTransaction(
            TransactionSucCallback callback,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String contractAddress,
            String funcName,
            Type... args) {
        final Function function =
                new Function(
                        funcName,
                        Arrays.<Type>asList(args),
                        Collections.<TypeReference<?>>emptyList());

        ExecuteTransaction executeTransaction =
                new ExecuteTransaction(contractAddress, web3j, credentials, gasPrice, gasLimit);

        executeTransaction.asyncSend(function, callback);
    }

    public List<Type> decode(String data, TypeReference<?>... typeReferences) {
        if (data.isEmpty() || data.equals("0x")) {
            return null;
        }
        List<TypeReference<?>> typeReferencesList = Arrays.<TypeReference<?>>asList(typeReferences);
        return FunctionReturnDecoder.decode(data, Utils.convert(typeReferencesList));
    }

    public List<Object> decode(String data, String retTypes[]) throws Exception {
        List<Object> result = new ArrayList<>();
        if (retTypes != null && retTypes.length != 0) {
            List<TypeReference<?>> references = getTypeReferenceList(retTypes);
            List<Type> returns = FunctionReturnDecoder.decode(data, Utils.convert(references));
            result = types2Objects(returns, retTypes);
        }
        return result;
    }

    private List<TypeReference<?>> getTypeReferenceList(String types[]) throws Exception {
        List<TypeReference<?>> result = new ArrayList<>();
        for (String type : types) {
            switch (type.trim()) {
                case "":
                    {
                        break;
                    }
                case "Int":
                    {
                        result.add(new TypeReference<Int256>() {});
                        break;
                    }
                case "Address":
                    {
                        result.add(new TypeReference<Uint160>() {});
                        break;
                    }
                case "String":
                    {
                        result.add(new TypeReference<Utf8String>() {});
                        break;
                    }
                case "IntArray":
                    {
                        result.add(new TypeReference<DynamicArray<Int256>>() {});
                        break;
                    }
                case "StringArray":
                    {
                        result.add(new TypeReference<DynamicArray<Utf8String>>() {});
                        break;
                    }
                default:
                    {
                        throw new Exception("Unsupported type :" + type);
                    }
            }
        }
        return result;
    }

    public List<Object> types2Objects(List<Type> datas, String javaTypes[]) throws Exception {
        if (datas.size() != javaTypes.length) {
            throw new Exception("The number of data and types is different.");
        }

        List<Object> result = new ArrayList<>();
        for (int i = 0; i < datas.size(); ++i) {
            result.add(type2Object(datas.get(i), javaTypes[i]));
        }

        return result;
    }

    public Object type2Object(Type data, String javaType) throws Exception {
        switch (javaType.trim()) {
            case "":
                {
                    return null;
                }
            case "Int":
                {
                    return ((BigInteger) data.getValue()).intValue();
                }
            case "Address":
                {
                    return ((BigInteger) data.getValue()).toString(16);
                }
            case "String":
                {
                    return (String) data.getValue();
                }
            case "IntArray":
                {
                    List<BigInteger> bigIntegers = convertList((List<Int256>) data.getValue());
                    return bigIntegerstoIntegers(bigIntegers);
                }
            case "StringArray":
                {
                    return convertList((List<Utf8String>) data.getValue());
                }
            default:
                {
                    throw new Exception("Unsupported type :" + javaType);
                }
        }
    }

    public List<Integer> bigIntegerstoIntegers(List<BigInteger> bigIntegers) {
        List<Integer> integers = new ArrayList<>();
        for (BigInteger bigInteger : bigIntegers) {
            integers.add(bigInteger.intValue());
        }
        return integers;
    }

    @SuppressWarnings("unchecked")
    public <S extends Type, T> List<T> convertList(List<S> arr) {
        List<T> out = new ArrayList<T>();
        for (Iterator<S> it = arr.iterator(); it.hasNext(); ) {
            out.add((T) it.next().getValue());
        }
        return out;
    }

    public <S extends Type, T> List<List<T>> convertListList(List<StaticArray<S>> arrs) {
        List<List<T>> out = new ArrayList<List<T>>();
        for (StaticArray<S> arr : arrs) {
            List<T> temp = convertList(arr.getValue());
            out.add(temp);
        }
        return out;
    }
}
