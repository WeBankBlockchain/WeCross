package com.webank.wecross.stub.bcos;

import com.webank.wecross.proof.ProofTools;
import org.fisco.bcos.web3j.crypto.HashInterface;
import org.fisco.bcos.web3j.crypto.SHA3Digest;

public class BCOSProofTools implements ProofTools {
    // private static SignInterface signInterface = new ECDSASign();
    private static HashInterface hashInterface = new SHA3Digest();

    @Override
    public String hash(String input) {
        return hashInterface.hash(input);
    }
}
