package com.webank.wecross.stub.bcos;

import com.webank.wecross.proof.ProofTools;
import org.fisco.bcos.web3j.crypto.HashInterface;
import org.fisco.bcos.web3j.crypto.gm.sm3.SM3Digest;

public class BCOSGuomiProofTools implements ProofTools {
    // private static SignInterface signInterface = new SM2Sign();
    private static HashInterface hashInterface = new SM3Digest();

    @Override
    public String hash(String input) {
        return hashInterface.hash(input);
    }
}
