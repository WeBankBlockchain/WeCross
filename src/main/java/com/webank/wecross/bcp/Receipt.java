package com.webank.wecross.bcp;

public class Receipt {
	private String hash;
	private Object result[];
	
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
}
