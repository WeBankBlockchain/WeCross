package com.webank.wecross.chain;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.exception.ErrorCode;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.bcos.BCOSStub;
import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.fabric.FabricStub;
import com.webank.wecross.stub.fabric.FabricStubFactory;
import com.webank.wecross.stub.jdchain.JDChainStub;
import com.webank.wecross.stub.jdchain.JDChainStubFactory;
import com.webank.wecross.utils.ConfigUtils;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainFactory {

    private static Logger logger = LoggerFactory.getLogger(ChainFactory.class);

    public static Map<String, Chain> getStubs(String network, Map<String, String> stubsDir)
            throws WeCrossException {
        Map<String, Chain> stubMap = new HashMap<>();

        for (String stub : stubsDir.keySet()) {
            String stubPath = stubsDir.get(stub);
            Toml stubToml;
            try {
                stubToml = ConfigUtils.getToml(stubPath);
            } catch (WeCrossException e) {
                String errorMessage = "Parse " + stubPath + " failed";
                throw new WeCrossException(ErrorCode.UNEXPECTED_CONFIG, errorMessage);
            }

            String stubName = stubToml.getString("common.stub");
            if (stubName == null) {
                String errorMessage =
                        "\"stub\" in [common] item  not found, please check " + stubPath;
                throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
            }

            if (!stub.equals(stubName)) {
                String errorMessage =
                        "\"stub\" in [common] item  must be same with directory name, please check "
                                + stubPath;
                throw new WeCrossException(ErrorCode.UNEXPECTED_CONFIG, errorMessage);
            }

            String type = stubToml.getString("common.type");
            if (type == null) {
                String errorMessage =
                        "\"type\" in [common] item  not found, please check " + stubPath;
                throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
            }

            Map<String, Object> stubConfig = stubToml.toMap();

            switch (type) {
                case WeCrossType.STUB_TYPE_BCOS:
                    {
                        BCOSStub bcosStub =
                                BCOSStubFactory.getBcosStub(network, stub, stubPath, stubConfig);
                        stubMap.put(stub, bcosStub);
                        break;
                    }
                case WeCrossType.STUB_TYPE_JDCHAIN:
                    {
                        JDChainStub jdChainStub =
                                JDChainStubFactory.getJDChainStub(
                                        network, stub, stubPath, stubConfig);
                        stubMap.put(stub, jdChainStub);
                        break;
                    }
                case WeCrossType.STUB_TYPE_FABRIC:
                    {
                        FabricStub fabricStub =
                                FabricStubFactory.getFabricStub(
                                        network, stub, stubPath, stubConfig);
                        stubMap.put(stub, fabricStub);
                        break;
                    }
                default:
                    {
                        String errorMessage = "Undefined stub type: " + type;
                        throw new WeCrossException(ErrorCode.UNEXPECTED_CONFIG, errorMessage);
                    }
            }
        }

        return stubMap;
    }
}
