package com.webank.wecross.account;

import com.google.protobuf.ByteString;
import com.webank.wecross.common.WeCrossType;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.protos.msp.Identities;
import org.hyperledger.fabric.protos.peer.FabricProposal;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.helper.Utils;
import org.hyperledger.fabric.sdk.identity.IdentityFactory;
import org.hyperledger.fabric.sdk.identity.SigningIdentity;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

public class FabricAccount implements Account {
    private User user;
    private SigningIdentity signer;

    public FabricAccount(User user) throws Exception {
        this.setUser(user);

        // ECDSA secp256r1
        this.signer =
                IdentityFactory.getSigningIdentity(CryptoSuite.Factory.getCryptoSuite(), user);
    }

    @Override
    public byte[] reassembleProposal(byte[] proposalBytes, String proposalType) throws Exception {
        if (proposalType == null) {
            return proposalBytes;
        }

        switch (proposalType) {
            case WeCrossType.PROPOSAL_TYPE_PEER_PAYLODAD:
            case WeCrossType.PROPOSAL_TYPE_ENDORSER_PAYLODAD:
                // Fabric needs to set account's identity in the proposal before signing
                return refactFabricProposalIdentity(proposalBytes);
            default:
                return proposalBytes;
        }
    }

    @Override
    public Boolean isProposalReady(String proposalType) {
        switch (proposalType) {
            case WeCrossType.PROPOSAL_TYPE_ENDORSER_PAYLODAD:
                // endorser payload need propose again
                return false;
            default:
                return true;
        }
    }

    @Override
    public byte[] sign(byte[] message) throws Exception {
        return signer.sign(message);
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public String getAddress() {
        return user.getAccount();
    }

    @Override
    public String getSignCryptoSuite() {
        return WeCrossType.CRYPTO_SUITE_FABRIC_BC_SECP256R1;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SigningIdentity getSigner() {
        return signer;
    }

    private byte[] refactFabricProposalIdentity(byte[] proposalBytes) throws Exception {
        FabricProposal.Proposal innerFabricProposal;
        innerFabricProposal = FabricProposal.Proposal.parseFrom(proposalBytes);
        Common.Header header = Common.Header.parseFrom(innerFabricProposal.getHeader());

        Common.ChannelHeader channelHeader =
                Common.ChannelHeader.parseFrom(header.getChannelHeader());

        Identities.SerializedIdentity refactedSerializedIdentity =
                signer.createSerializedIdentity();

        Common.SignatureHeader refactedSignatureHeader =
                Common.SignatureHeader.newBuilder()
                        .setCreator(refactedSerializedIdentity.toByteString())
                        .setNonce(ByteString.copyFrom(Utils.generateNonce()))
                        .build();

        Common.ChannelHeader refactedChannelHeader =
                Common.ChannelHeader.newBuilder()
                        .setType(channelHeader.getType())
                        .setVersion(channelHeader.getVersion())
                        .setTxId(
                                calcRefactedTxID(
                                        refactedSignatureHeader, refactedSerializedIdentity))
                        .setChannelId(channelHeader.getChannelId())
                        .setTimestamp(channelHeader.getTimestamp())
                        .setEpoch(channelHeader.getEpoch())
                        .setExtension(channelHeader.getExtension())
                        .build();

        Common.Header refactedHeader =
                Common.Header.newBuilder()
                        .setSignatureHeader(refactedSignatureHeader.toByteString())
                        .setChannelHeader(refactedChannelHeader.toByteString())
                        .build();

        FabricProposal.Proposal refactedProposal =
                FabricProposal.Proposal.newBuilder()
                        .setHeader(refactedHeader.toByteString())
                        .setPayload(innerFabricProposal.getPayload())
                        .build();
        byte[] refactedProposalBytes = refactedProposal.toByteArray();

        // System.out.println(Arrays.toString(proposalBytes));
        // System.out.println(Arrays.toString(refactedProposalBytes));

        return refactedProposalBytes;
    }

    private String calcRefactedTxID(
            Common.SignatureHeader refactedSignatureHeader,
            Identities.SerializedIdentity refactedSerializedIdentity)
            throws Exception {
        ByteString no = refactedSignatureHeader.getNonce();

        ByteString comp = no.concat(refactedSerializedIdentity.toByteString());

        byte[] txh = CryptoSuite.Factory.getCryptoSuite().hash(comp.toByteArray());

        //    txID = Hex.encodeHexString(txh);
        String txID = new String(Utils.toHexString(txh));
        return txID;
    }
}
