package com.webank.wecross.proof;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Proof {
    private ProofTools proofTools;

    @JsonIgnore
    public ProofTools getProofTools() {
        return proofTools;
    }

    public void setProofTools(ProofTools proofTools) {
        this.proofTools = proofTools;
    }
}
