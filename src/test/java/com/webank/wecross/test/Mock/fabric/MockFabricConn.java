package com.webank.wecross.test.Mock.fabric;

import com.webank.wecross.exception.ErrorCode;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.fabric.FabricConfig;
import com.webank.wecross.stub.fabric.FabricConn;
import com.webank.wecross.stub.fabric.FabricSdk;
import com.webank.wecross.stub.fabric.FabricSdkConfig;
import org.hyperledger.fabric.sdk.ChaincodeID;

public class MockFabricConn extends FabricConn {
    public MockFabricConn() throws Exception {
        FabricConfig fabricConfig = new MockFabricConfig();

        FabricSdkConfig fabricSdkConfig = new FabricSdkConfig();
        fabricSdkConfig.setFabricConfig(fabricConfig);
        FabricSdk fabricSdk = fabricSdkConfig.initFabricStub();

        this.setChannel(fabricSdk.getChannel());
        this.setHfClient(fabricSdk.getHfClient());

        this.setName("Mock resource HelloWorld");
        this.setType("FABRIC_CONTRACT");
        this.setChainCodeName("Mock mycc");
        this.setChainLanguage("go");
        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(this.getChainCodeName()).build();
        this.setChaincodeID(chaincodeID);

        if (this.getChainLanguage().toLowerCase().equals("go")) {
            this.setChainCodeType(org.hyperledger.fabric.sdk.TransactionRequest.Type.GO_LANG);
        } else if (this.getChainLanguage().toLowerCase().equals("java")) {
            this.setChainCodeType(org.hyperledger.fabric.sdk.TransactionRequest.Type.JAVA);
        } else if (this.getChainLanguage().toLowerCase().equals("node")) {
            this.setChainCodeType(org.hyperledger.fabric.sdk.TransactionRequest.Type.NODE);
        } else {
            String errorMessage =
                    "\"chainLanguage\" in [[resource]] not support chaincode language "
                            + this.getChainLanguage();
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }
    }
}
