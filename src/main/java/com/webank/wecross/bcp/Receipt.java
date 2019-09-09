package com.webank.wecross.bcp;

public class Receipt {
	private String hash;
	private String result[];
	private String proof;
	
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String[] getResult() {
		return result;
	}
	public void setResult(String result[]) {
		this.result = result;
	}
	public String getProof() {
		return proof;
	}
	public void setProof(String proof) {
		this.proof = proof;
	}
}
