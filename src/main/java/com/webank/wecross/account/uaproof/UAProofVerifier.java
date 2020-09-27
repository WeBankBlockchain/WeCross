package com.webank.wecross.account.uaproof;

import static com.webank.wecross.exception.WeCrossException.ErrorCode.UAPROOF_VERIFYIER_EXCEPTION;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stubmanager.StubManager;
import com.webank.wecross.utils.SM2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UAProofVerifier {
    private static Logger logger = LoggerFactory.getLogger(UAProofVerifier.class);

    private StubManager stubManager;

    public boolean verify(UAProof uaProof) {
        String type = uaProof.getType();
        try {

            if (!verifyUa2Ca(uaProof.getUa2ca())) {
                logger.warn("Verify ua2ca failed: " + uaProof);
                return false;
            }

            if (!verifyCa2Ua(type, uaProof.getCa2ua())) {
                logger.warn("Verify ua2ca failed: " + uaProof);
                return false;
            }

            if (!uaProof.getUa2ca().getSigner().equals(uaProof.getCa2ua().getSignee())) {
                logger.warn("ca has not sign ua");
                return false;
            }

            if (!uaProof.getCa2ua().getSigner().equals(uaProof.getUa2ca().getSignee())) {
                logger.warn("ua has not sign ca");
                return false;
            }

            logger.debug("Verify UAProof Success: {}", uaProof);

            return true; // ok

        } catch (WeCrossException e) {
            logger.error("UAProof verrify exception: ", e);
            return false;
        }
    }

    private boolean verifyUa2Ca(UAProofSign ua2ca) throws WeCrossException {
        try {
            if (!SM2.verify(ua2ca.getSignBytes(), ua2ca.getMessage())) {
                logger.warn("verifyUa2Ca failed: " + ua2ca);
                return false;
            }

            String uaPubInSign = SM2.SignatureData.parseFrom(ua2ca.getSignBytes()).getHexPub();

            if (!ua2ca.getSigner().equals(uaPubInSign)) {
                logger.warn("verifyUa2Ca signer failed: " + ua2ca);
                return false;
            }

            return true;
        } catch (Exception e) {
            throw new WeCrossException(
                    UAPROOF_VERIFYIER_EXCEPTION, "UA Proof verifier exceptio: ", e);
        }
    }

    private boolean verifyCa2Ua(String type, UAProofSign ca2ua) throws WeCrossException {
        Driver driver = stubManager.getStubFactory(type).newDriver();
        return driver.accountVerify(ca2ua.getSigner(), ca2ua.getSignBytes(), ca2ua.getMessage());
    }

    public void setStubManager(StubManager stubManager) {
        this.stubManager = stubManager;
    }
}
