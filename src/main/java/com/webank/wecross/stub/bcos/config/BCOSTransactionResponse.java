package com.webank.wecross.stub.bcos.config;

import com.webank.wecross.proof.BlockHeaderProof;
import com.webank.wecross.proof.MerkleProof;
import com.webank.wecross.restserver.response.TransactionResponse;

public class BCOSTransactionResponse extends TransactionResponse {
    private BlockHeaderProof blockHeader;
    private MerkleProof[] proofs;

    @Override
    public BlockHeaderProof getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(BlockHeaderProof blockHeader) {
        this.blockHeader = blockHeader;
    }

    @Override
    public MerkleProof[] getProofs() {
        return proofs;
    }

    public void setProofs(MerkleProof[] proofs) {
        this.proofs = proofs;
    }
}
