package com.webank.wecross.test.Mock.web3j;

import com.webank.wecross.test.Mock.web3j.Response.MockNodeVersion;
import java.io.IOException;
import org.fisco.bcos.web3j.protocol.core.Request;
import org.fisco.bcos.web3j.protocol.core.Response;

public class MockWeb3jRequest<S, T extends Response> extends Request {

    @Override
    public T send() throws IOException {
        return (T) new MockNodeVersion();
    }
}
