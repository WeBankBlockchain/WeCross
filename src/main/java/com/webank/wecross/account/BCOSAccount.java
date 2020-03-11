package com.webank.wecross.account;

import com.webank.wecross.common.WeCrossType;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECDSASign;
import org.fisco.bcos.web3j.crypto.Sign;
import org.fisco.bcos.web3j.crypto.SignInterface;

public class BCOSAccount implements Account {
    private org.fisco.bcos.web3j.crypto.Credentials innerBCOSCredentials;
    private String name;

    protected SignInterface signer;

    public BCOSAccount(String name, org.fisco.bcos.web3j.crypto.Credentials innerBCOSCredentials) {
        this.setInnerBCOSCredentials(innerBCOSCredentials);
        this.setName(name);

        // ECDSA secp256k1
        signer = new ECDSASign();
    }

    @Override
    public byte[] reassembleProposal(byte[] proposalBytes, String proposalType) throws Exception {
        // BCOS no needs to set account's identity in the before signing
        return proposalBytes;
    }

    @Override
    public Boolean isProposalReady(String proposalType) {
        return true;
    }

    @Override
    public byte[] sign(byte[] message) throws Exception {
        Sign.SignatureData signData =
                signer.signMessage(message, innerBCOSCredentials.getEcKeyPair());
        BCOSSignatureEncoder bcosSignatureEncoder = new BCOSSignatureEncoder();
        byte[] encodedBytes = bcosSignatureEncoder.encode(signData);

        return encodedBytes;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getAddress() {
        return innerBCOSCredentials.getAddress();
    }

    @Override
    public String getSignCryptoSuite() {
        return WeCrossType.CRYPTO_SUITE_BCOS_SHA3_256_SECP256K1;
    }

    public void setInnerBCOSCredentials(Credentials innerBCOSCredentials) {
        this.innerBCOSCredentials = innerBCOSCredentials;
    }

    public org.fisco.bcos.web3j.crypto.Credentials getInnerBCOSCredentials() {
        return this.innerBCOSCredentials;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SignInterface getSigner() {
        return signer;
    }
}
