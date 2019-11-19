package com.webank.wecross.stub.jdchain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jd.blockchain.contract.Contract;
import com.jd.blockchain.contract.ContractEvent;
import com.jd.blockchain.ledger.BlockchainKeypair;
import com.jd.blockchain.ledger.KVDataEntry;
import com.jd.blockchain.ledger.OperationResult;
import com.jd.blockchain.ledger.PreparedTransaction;
import com.jd.blockchain.ledger.TransactionTemplate;
import com.jd.blockchain.sdk.BlockchainService;
import com.webank.wecross.config.ConfigInfo;
import com.webank.wecross.core.HashUtils;
import com.webank.wecross.exception.Status;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainContractResource extends JDChainResource {

    private Logger logger = LoggerFactory.getLogger(JDChainContractResource.class);
    private Boolean isInit = false;
    @JsonIgnore private String contractAddress;
    private String checksum;

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    @Override
    public String getType() {
        return ConfigInfo.RESOURCE_TYPE_JDCHAIN_CONTRACT;
    }

    @Override
    public GetDataResponse getData(GetDataRequest request) {
        GetDataResponse response = new GetDataResponse();
        String[] splitKey = request.getKey().split("\\|");
        if (splitKey.length != 2) {
            logger.error(
                    "input {} is invalidate should have two elements split by |", request.getKey());
            response.setErrorCode(Status.JDCHAIN_PARAMETER_INVALIDATE);
            response.setErrorMessage(
                    "input:"
                            + request.getKey()
                            + " is invalidate should have two elements split by |");
            return response;
        }
        String address = splitKey[0];
        String userID = splitKey[1];
        logger.debug(
                "get data request key:{} address:{} userID:{}", request.getKey(), address, userID);
        int channelCount = blockchainService.size();
        if (channelCount == 0) {
            logger.error("input {} block chain service count is zero", request.getKey());
            response.setErrorCode(Status.JDCHAIN_CONNECTION_COUNRT_ERROR);
            response.setErrorMessage(
                    "input:" + request.getKey() + "blockk chain service count is zero");
            return response;
        }
        SecureRandom secureRandom = new SecureRandom();
        Integer randNum = secureRandom.nextInt(channelCount);
        for (int index = 0; index < channelCount; ++index) {
            Integer useIndex = (randNum + index) % channelCount;
            BlockchainService service = blockchainService.get(useIndex);
            KVDataEntry[] kvDataEntries = null;
            try {
                kvDataEntries = service.getDataEntries(ledgerHash, address, userID);
            } catch (Exception e) {
                logger.error("getDataEntries failed:{}", e.toString());
                continue;
            }
            if (kvDataEntries == null || kvDataEntries.length == 0) {
                logger.error("get data error userID:{} address:{}", userID, address);
                response.setErrorCode(Status.JDCHAIN_GETDATA_ERROR);
                response.setErrorMessage("get data failed key:" + request.getKey());
                return response;
            }
            KVDataEntry kvDataEntry = kvDataEntries[0];
            if (kvDataEntry.getVersion() == -1) {
                response.setErrorCode(0);
                response.setErrorMessage("");
                response.setValue("");
                return response;
            }

            response.setErrorCode(0);
            response.setErrorMessage("");
            response.setValue(String.valueOf(kvDataEntry.getValue()));
            return response;
        }

        logger.error("get data error userID:{} address:{}", userID, address);
        response.setErrorCode(Status.JDCHAIN_GETDATA_ERROR);
        response.setErrorMessage("get data failed key:" + request.getKey());
        return response;
    }

    @Override
    public SetDataResponse setData(SetDataRequest request) {
        SetDataResponse setDataResponse = new SetDataResponse();
        setDataResponse.setErrorCode(Status.NONSENSE_CALL);
        setDataResponse.setErrorMessage("Not supported by JDCHAIN_CONTRACT");
        return setDataResponse;
    }

    public com.jd.blockchain.ledger.TransactionResponse commit(
            TransactionTemplate txTpl, Integer index) {
        PreparedTransaction ptx = txTpl.prepare();
        BlockchainKeypair blockchainKeypair = adminKey.get(index);
        ptx.sign(blockchainKeypair);
        return ptx.commit();
    }

    private CtClass dynamicGenerateClass(TransactionRequest request) {
        ClassPool pool = ClassPool.getDefault();
        String classname =
                "com.jd.chain.contract.Class" + UUID.randomUUID().toString().replaceAll("-", "");
        CtClass ctClass = pool.makeInterface(classname);
        CtClass[] parameterType = null;
        if (request.getArgs().length == 0) {
            parameterType = new CtClass[] {};
        } else {
            parameterType = new CtClass[request.getArgs().length];

            for (int i = 0; i < request.getArgs().length; i++) {

                String name = request.getArgs()[i].getClass().getName();
                try {
                    parameterType[i] = pool.getCtClass(name);
                } catch (NotFoundException e) {
                    logger.error("get class exception:{}", e);
                    return null;
                }
            }
        }
        String returnType = "java.lang.String";
        CtMethod ctMethod = null;
        try {
            ctMethod =
                    new CtMethod(
                            pool.getCtClass(returnType),
                            request.getMethod(),
                            parameterType,
                            ctClass);
        } catch (NotFoundException e) {
            logger.error("create method:{} failed:{}", request.getMethod(), e);
            return null;
        }
        ClassFile ccFile = ctClass.getClassFile();
        ConstPool constpool = ccFile.getConstPool();
        AnnotationsAttribute attrForClass =
                new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation annotForClass = new Annotation(Contract.class.getName(), constpool);
        attrForClass.addAnnotation(annotForClass);
        ccFile.addAttribute(attrForClass);

        AnnotationsAttribute attrForMethod =
                new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation annotForMethod = new Annotation(ContractEvent.class.getName(), constpool);
        annotForMethod.addMemberValue(
                "name", new StringMemberValue(request.getMethod(), constpool));
        attrForMethod.addAnnotation(annotForMethod);
        ctMethod.getMethodInfo().addAttribute(attrForMethod);
        try {
            ctClass.addMethod(ctMethod);
        } catch (CannotCompileException e) {
            logger.error("add method:{} failed:{}", request.getMethod(), e);
            return null;
        }
        return ctClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {

        logger.debug(
                "request parameter:{} now connection to gateway size:{}",
                request.toString(),
                blockchainService.size());
        JDChainResponse response = new JDChainResponse();
        int channelCount = blockchainService.size();
        if (channelCount == 0) {
            response.setErrorCode(Status.JDCHAIN_CONNECTION_COUNRT_ERROR);
            response.setErrorMessage("has no gate way to connect");
            return response;
        }
        logger.debug("request method:{} dynamic generate class success ", request.getMethod());
        SecureRandom secureRandom = new SecureRandom();
        Integer randNum = secureRandom.nextInt(channelCount);
        for (int index = 0; index < channelCount; ++index) {

            CtClass ctClass = dynamicGenerateClass(request);
            if (ctClass == null) {
                response.setErrorCode(Status.JDCHAIN_GENERATE_CLASS_ERROR);
                response.setErrorMessage("generate class failed");
                return response;
            }
            Integer useIndex = (randNum + index) % channelCount;
            BlockchainService blockChainService = blockchainService.get(useIndex);
            logger.debug(
                    "request method:{} get block chanin service id:{} success ",
                    request.getMethod(),
                    useIndex);
            TransactionTemplate txTpl = blockChainService.newTransaction(ledgerHash);
            logger.debug(
                    "request method:{}  new transaction  success ledgerhash:{}",
                    request.getMethod(),
                    ledgerHash);
            Object contractObject;
            try {
                contractObject = txTpl.contract(contractAddress, ctClass.toClass());
                Method[] methods = contractObject.getClass().getMethods();
                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    if (method != null && request.getMethod().equals(method.getName())) {
                        try {
                            logger.debug("invoke  method:{}", request.getMethod());
                            method.invoke(contractObject, request.getArgs());

                        } catch (IllegalAccessException e) {
                            response.setErrorCode(Status.JDCHAIN_INVOKE_METHOD_ERROR);
                            logger.error("invoke method failed:{}", request.getMethod());
                            response.setErrorMessage("invoke method failed");
                            return response;

                        } catch (IllegalArgumentException e) {
                            response.setErrorCode(Status.JDCHAIN_INVOKE_METHOD_ERROR);
                            logger.error("invoke method failed:{}", request.getMethod());
                            response.setErrorMessage("invoke method failed");
                            return response;
                        } catch (InvocationTargetException e) {
                            response.setErrorCode(Status.JDCHAIN_INVOKE_METHOD_ERROR);
                            logger.error("invoke method failed:{}", request.getMethod());
                            response.setErrorMessage("invoke method failed");
                            return response;
                        }

                        com.jd.blockchain.ledger.TransactionResponse txResponse = null;
                        try {
                            txResponse = commit(txTpl, useIndex);
                        } catch (Exception e) {
                            logger.error(
                                    "execute method:{} commit failed:{}",
                                    request.getMethod(),
                                    e.toString());
                            break;
                        }
                        if (txResponse.isSuccess()) {

                            logger.debug(
                                    "request method:{} parameter:{} invoke success hash:{} result size:{}",
                                    request.getMethod(),
                                    request.getArgs().toString(),
                                    txResponse.getBlockHash().toBase58(),
                                    txResponse.getOperationResults().length);
                            response.setErrorCode(0);
                            response.setErrorMessage("");
                            response.setHash(txResponse.getBlockHash().toBase58());
                            List<Object> resultList = new ArrayList<Object>();
                            OperationResult[] operationResult = txResponse.getOperationResults();
                            for (int j = 0; j < operationResult.length; j++) {
                                OperationResult result = operationResult[j];
                                byte[] value = result.getResult().getValue().toBytes();
                                resultList.add(new String(value));
                            }
                            response.setResult(resultList.toArray());

                            logger.debug(
                                    "request parameter:{} response:{}",
                                    request.toString(),
                                    response.toString());

                            return response;
                        } else {
                            response.setErrorCode(Status.JDCHAIN_COMMIT_ERROR);
                            logger.error("invoke method failed:{}", request.getMethod());
                            response.setErrorMessage("invoke method failed");
                            return response;
                        }
                    }
                }
            } catch (CannotCompileException e) {
                response.setErrorCode(Status.JDCHAIN_GENERATE_COMPILE_ERROR);
                response.setErrorMessage("can not cpmpile");
                return response;
            }
        }
        return response;
    }

    @Override
    public TransactionResponse call(TransactionRequest request) {
        JDChainResponse response = new JDChainResponse();
        response.setErrorCode(Status.JDCHAIN_METHOD_NOTSUPPORT);
        response.setErrorMessage("call method not supported");
        return response;
    }

    @Override
    public void registerEventHandler(EventCallback callback) {}

    @Override
    public TransactionRequest createRequest() {
        return new JDChainRequest();
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
}
