package com.webank.wecross.bcp.bcos2;

import com.webank.wecross.bcp.BlockHeader;
import com.webank.wecross.bcp.Receipt;
import com.webank.wecross.bcp.Stub;
import com.webank.wecross.bcp.Transaction;

public class Bcos2Stub implements Stub{
	@Override
	public String getData(String table, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setData(String table, String key, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Receipt sendTransaction(Transaction transaction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BlockHeader getBlockHeader(Integer number) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
