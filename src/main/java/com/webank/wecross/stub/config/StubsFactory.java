package com.webank.wecross.stub.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.config.ConfigInfo;
import com.webank.wecross.config.ConfigUtils;
import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.bcos.BCOSStub;
import com.webank.wecross.stub.bcos.config.BCOSStubFactory;
import com.webank.wecross.stub.jdchain.JDChainStub;
import com.webank.wecross.stub.jdchain.config.JDChainStubFactory;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubsFactory {

    private static Logger logger = LoggerFactory.getLogger(StubsFactory.class);

    public static Map<String, Stub> getStubs(String network, Map<String, String> stubsDir)
            throws WeCrossException {
        Map<String, Stub> stubMap = new HashMap<>();

        for (String stub : stubsDir.keySet()) {
            String stubPath = stubsDir.get(stub);
            Toml stubToml;
            try {
                stubToml = ConfigUtils.getToml(stubPath);
            } catch (WeCrossException e) {
                logger.warn(e.getMessage());
                continue;
            }

            String stubName = stubToml.getString("common.stub");
            if (stubName == null) {
                String errorMessage =
                        "\"stub\" in [common] item  not found, please check " + stubPath;
                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
            }

            if (!stub.equals(stubName)) {
                String errorMessage =
                        "\"stub\" in [common] item  must be same with directory name, please check "
                                + stubPath;
                throw new WeCrossException(Status.UNEXPECTED_CONFIG, errorMessage);
            }

            String type = stubToml.getString("common.type");
            if (type == null) {
                String errorMessage =
                        "\"type\" in [common] item  not found, please check " + stubPath;
                throw new WeCrossException(Status.FIELD_MISSING, errorMessage);
            }

            Map<String, Object> stubConfig = stubToml.toMap();

            switch (type) {
                case ConfigInfo.STUB_TYPE_BCOS:
                    {
                        BCOSStub bcosStub =
                                BCOSStubFactory.getBcosStub(network, stub, stubPath, stubConfig);
                        stubMap.put(stub, bcosStub);
                        break;
                    }
                case ConfigInfo.STUB_TYPE_JDCHAIN:
                    {
                        JDChainStub jdChainStub =
                                JDChainStubFactory.getJDChainStub(
                                        network, stub, stubPath, stubConfig);
                        stubMap.put(stub, jdChainStub);
                        break;
                    }
                default:
                    {
                        String errorMessage = "Undefined stub type: " + type;
                        throw new WeCrossException(Status.UNEXPECTED_CONFIG, errorMessage);
                    }
            }
        }

        return stubMap;
    }
}
