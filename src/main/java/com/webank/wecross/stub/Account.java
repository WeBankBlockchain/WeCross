package com.webank.wecross.stub;

import java.util.Map;

public interface Account {
    String getName();

    String getType();

    /**
     * get the account's identity ( public key )
     *
     * @return
     */
    String getIdentity();

    int getKeyID();

    boolean isDefault();

    /**
     * generate UAProof of the account with given ua
     *
     * @param ua
     * @return ua proof
     */
    String generateUAProof(UniversalAccount ua);

    /**
     * Revocer identity from proof
     *
     * @param uaProof
     * @return chain account identity, uaid
     */
    Map.Entry<String, String> recoverProof(String uaProof, UniversalAccount ua);
}
