package com.tchip.speech.json;

import java.io.Serializable;

/**
 * 
 * @author wu
 *
 */
public class NativeSemPersonInfo implements Serializable {
	private static final long serialVersionUID = 3414101383662292231L;
	private String action;	
	private String domain;	
	private String person;	

	
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

	public void setPerson(String person) {
		this.person = person;
	}
	public String getPerson() {
		return person;
	}
}