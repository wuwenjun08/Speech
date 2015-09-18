package com.tchip.speech.json;

import java.io.Serializable;

/**
 * 
 * @author wu
 *
 */
public class CloudRequestInfo implements Serializable {
	private static final long serialVersionUID = 4108255981635447591L;
	private String domain;	
	private String action;
	private String param;

	
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getDomain() {
		return domain;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
	public String getAction() {
		return action;
	}
	
	public void setParam(String param) {
		this.param = param;
	}
	public String getParam() {
		return param;
	}
}