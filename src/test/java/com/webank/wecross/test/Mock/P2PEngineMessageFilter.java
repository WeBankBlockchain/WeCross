package com.webank.wecross.test.Mock;

import com.webank.wecross.network.NetworkMessage;
import com.webank.wecross.network.NetworkResponse;
import org.junit.Assert;

public abstract class P2PEngineMessageFilter {
    public <T> NetworkResponse checkAndResponse(NetworkMessage<T> msg) {
        Assert.assertNotEquals(null, msg);
        String method = msg.getMethod();

        String r[] = method.split("/");

        if (r.length == 1) {
            return handle1(msg);

        } else if (r.length == 4) {
            return handle4(msg);
        }

        Assert.assertTrue("Invalid method: " + method, false);
        return null;
    }

    public abstract NetworkResponse handle1(NetworkMessage msg);

    public abstract NetworkResponse handle4(NetworkMessage msg);
}
