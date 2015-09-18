package com.tchip.speech.json;

import java.io.Serializable;

/**
 * 
 * @author wu
 *
 */
public class CloudResultInfo implements Serializable {
	private static final long serialVersionUID = -3289600909128531512L;
	private String semantics;	
	private String input;

	
	public void setSemantics(String semantics) {
		this.semantics = semantics;
	}
	public String getSemantics() {
		return semantics;
	}
	
	public void setInput(String input) {
		this.input = input;
	}
	public String getInput() {
		return input;
	}
}