package com.tchip.speech.json;

import java.io.Serializable;

/**
 * 
 * @author wu
 *
 */
public class NativePostInfo implements Serializable {
	private static final long serialVersionUID = 568144887563370276L;
	private String sem;	

	
	public void setSem(String sem) {
		this.sem = sem;
	}
	public String getSem() {
		return sem;
	}
}