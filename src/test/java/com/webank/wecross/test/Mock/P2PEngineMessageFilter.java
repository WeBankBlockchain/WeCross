package com.webank.wecross.test.Mock;

import com.webank.wecross.p2p.P2PMessage;
import org.junit.Assert;

public abstract class P2PEngineMessageFilter {
    public <T> P2PMessage checkAndResponse(P2PMessage<T> msg) {
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

    public abstract P2PMessage handle1(P2PMessage msg);

    public abstract P2PMessage handle4(P2PMessage msg);
}
