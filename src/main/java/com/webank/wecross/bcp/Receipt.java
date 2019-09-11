package com.webank.wecross.bcp;

public class Receipt {
	private String hash;
	private Object result[];
	private String proof;
	
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public Object[] getResult() {
		return result;
	}
	public void setResult(Object result[]) {
		this.result = result;
	}
	public String getProof() {
		return proof;
	}
	public void setProof(String proof) {
		this.proof = proof;
	}
}
