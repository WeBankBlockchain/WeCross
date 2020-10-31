package com.webank.wecross.account.uaproof;

import com.webank.wecross.account.UniversalAccount;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stubmanager.StubManager;

public class UAProofGenerator {
    private StubManager stubManager;

    public void setStubManager(StubManager stubManager) {
        this.stubManager = stubManager;
    }

    public UAProof generate(UniversalAccount ua, Account ca) throws WeCrossException {
        String timestamp = String.valueOf(System.currentTimeMillis());

        UAProofSign ua2ca = generateUa2Ca(ua, ca, timestamp);
        UAProofSign ca2ua = generateCa2Ua(ua, ca, timestamp);

        UAProof uaProof = new UAProof();
        uaProof.setType(ca.getType());
        uaProof.setUa2ca(ua2ca);
        uaProof.setCa2ua(ca2ua);
        return uaProof;
    }

    private UAProofSign generateUa2Ca(UniversalAccount ua, Account ca, String timestamp)
            throws WeCrossException {
        UAProofSign ua2ca = new UAProofSign();
        ua2ca.setTimestamp(timestamp);
        ua2ca.setSigner(ua.getPub());
        ua2ca.setSignee(ca.getIdentity());

        byte[] signBytes = ua.sign(ua2ca.getMessage());
        ua2ca.setSignBytes(signBytes);
        return ua2ca;
    }

    private UAProofSign generateCa2Ua(UniversalAccount ua, Account ca, String timestamp)
            throws WeCrossException {
        UAProofSign ca2ua = new UAProofSign();
        ca2ua.setTimestamp(timestamp);
        ca2ua.setSigner(ca.getIdentity());
        ca2ua.setSignee(ua.getPub());

        String type = ca.getType();

        Driver driver = stubManager.getStubDriver(type);

        byte[] signBytes = driver.accountSign(ca, ca2ua.getMessage());
        ca2ua.setSignBytes(signBytes);
        return ca2ua;
    }
}
