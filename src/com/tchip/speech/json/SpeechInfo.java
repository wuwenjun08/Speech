package com.tchip.speech.json;

import java.io.Serializable;

/**
 * 
 * @author wu
 *
 */
public class SpeechInfo implements Serializable {
	private static final long serialVersionUID = 3262893612531184703L;
	
	private String applicationId; 
	private String recordId;
	private String luabin;					
	private Integer eof;						
	private String src;				
	private String version;								
	
	private String result;

	
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	public String getApplicationId() {
		return applicationId;
	}
	
	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}
	public String getRecordId() {
		return recordId;
	}
	
	public void setLuabin(String luabin) {
		this.luabin = luabin;
	}
	public String getLuabin() {
		return luabin;
	}
	
	public void setCarNo(int eof) {
		this.eof = eof;
	}
	public int getCarNo() {
		return eof;
	}
	
	public void setSrc(String src) {
		this.src = src;
	}
	public String getSrc() {
		return src;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	public String getVersion() {
		return version;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	public String getResult() {
		return result;
	}
}