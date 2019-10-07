package com.webank.wecross.stub.bcos;

import com.webank.wecross.bcp.TransactionResponse;

public class BCOSResponse extends TransactionResponse {
	private String resultString;

	public String getResultString() {
		return resultString;
	}

	public void setResultString(String resultString) {
		this.resultString = resultString;
	}
}
