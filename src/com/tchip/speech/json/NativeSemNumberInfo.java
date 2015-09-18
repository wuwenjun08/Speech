package com.tchip.speech.json;

import java.io.Serializable;

/**
 * 
 * @author wu
 *
 */
public class NativeSemNumberInfo implements Serializable {
	private static final long serialVersionUID = 3414101383662292231L;
	private String action;	
	private String domain;	
	private String number;	

	
	public void setAction(String action) {
		this.action = action;
	}
	public String getAction() {
		return action;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getDomain() {
		return domain;
	}

	public void setNumber(String number) {
		this.number = number;
	}
	public String getNumber() {
		return number;
	}
}