package com.tchip.speech.json;

import java.io.Serializable;

/**
 * 
 * @author wu
 *
 */
public class CloudSemanticsInfo implements Serializable {
	private static final long serialVersionUID = 4470006247204371183L;
	private String request;	
	private Integer slotcount;

	
	public void setRequest(String request) {
		this.request = request;
	}
	public String getRequest() {
		return request;
	}
	
	public void setSlotcount(int slotcount) {
		this.slotcount = slotcount;
	}
	public int getSlotcount() {
		return slotcount;
	}
}