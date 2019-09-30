package com.webank.wecross.stub.bcos;

import com.webank.wecross.bcp.Response;

public class BCOSResponse extends Response {
	private String resultString;

	public String getResultString() {
		return resultString;
	}

	public void setResultString(String resultString) {
		this.resultString = resultString;
	}
}
