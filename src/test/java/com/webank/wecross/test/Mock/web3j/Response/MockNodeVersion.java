package com.webank.wecross.test.Mock.web3j.Response;

import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion;

public class MockNodeVersion extends NodeVersion {
    private final Version version = new MockVersion();

    public Version getNodeVersion() {

        return version;
    }

    public class MockVersion extends NodeVersion.Version {
        @Override
        public String getVersion() {
            // return EnumNodeVersion.BCOS_2_2_0.getVersion();
            return "latest"; // Unrecognized is latest
        }

        @Override
        public String getSupportedVersion() {
            // return EnumNodeVersion.BCOS_2_2_0.getVersion();
            return "latest"; // Unrecognized is latest
        }

        @Override
        public String getChainID() {
            return "1";
        }
    }
}
