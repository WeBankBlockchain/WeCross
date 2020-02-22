package com.webank.wecross.resource;

public class ReferenceCount {
	private int count = 1;
	
	public void increase() {
		++count;
	}
	
	public void decrease() {
		--count;
	}
	
	public boolean isDestoryed() {
		return count == 0;
	}
}
