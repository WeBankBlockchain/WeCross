package com.webank.wecross.test.Mock.fabric;

import com.webank.wecross.stub.fabric.config.FabricConfig;
import com.webank.wecross.stub.fabric.config.FabricUser;
import java.security.PrivateKey;
import org.hyperledger.fabric.sdk.Enrollment;

public class MockFabricUser extends FabricUser {

    public MockFabricUser(FabricConfig fabricConfig) {
        super(fabricConfig);
    }

    @Override
    public Enrollment getEnrollment() {
        return new Enrollment() {
            @Override
            public PrivateKey getKey() {
                return null;
            }

            @Override
            public String getCert() {
                return null;
            }
        };
    }
}
