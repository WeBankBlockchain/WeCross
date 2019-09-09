package com.webank.wecross.bcp;

public class BlockHeader {
	private Integer number;
	private String prevHash;
	private String hash;
	private String transactionRoot;
	private String receiptRoot;
	private String stateRoot;
	
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	public String getPrevHash() {
		return prevHash;
	}
	public void setPrevHash(String prevHash) {
		this.prevHash = prevHash;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getTransactionRoot() {
		return transactionRoot;
	}
	public void setTransactionRoot(String transactionRoot) {
		this.transactionRoot = transactionRoot;
	}
	public String getReceiptRoot() {
		return receiptRoot;
	}
	public void setReceiptRoot(String receiptRoot) {
		this.receiptRoot = receiptRoot;
	}
	public String getStateRoot() {
		return stateRoot;
	}
	public void setStateRoot(String stateRoot) {
		this.stateRoot = stateRoot;
	}
}
