package com.webank.wecross.bcp;

public class Event {
	private String type;
	private String from;
	private String to;
	private Object content[];
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public Object[] getContent() {
		return content;
	}
	public void setContent(Object[] content) {
		this.content = content;
	}
	
}
