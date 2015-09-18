package com.tchip.speech.json;

import java.io.Serializable;

/**
 * 
 * @author wu
 *
 */
public class NativeResultInfo implements Serializable {
	private static final long serialVersionUID = -8963811573939168722L;
	private Integer systime;
	private Integer sestime;
	private String res;	
	private Integer rectime;
	private Integer wavetime;
	private Integer prutime;
	private float conf;
	private String version;
	private Integer delayframe;
	private Integer vite_vad;
	private Integer delaytime;
	private Integer vadtime;
	private Integer eof;
	
	private String post;	
	private String rec;
	
	public void setPost(String post) {
		this.post = post;
	}
	public String getPost() {
		return post;
	}
	
	public void setRec(String rec) {
		this.rec = rec;
	}
	public String getRec() {
		return rec;
	}

	public void setEof(int eof) {
		this.eof = eof;
	}
	public int getEof() {
		return eof;
	}

	public void setVadtime(int vadtime) {
		this.vadtime = vadtime;
	}
	public int getVadtime() {
		return vadtime;
	}

	public void setDelaytime(int delaytime) {
		this.delaytime = delaytime;
	}
	public int getDelaytime() {
		return delaytime;
	}

	public void setVite_vad(int vite_vad) {
		this.vite_vad = vite_vad;
	}
	public int getVite_vad() {
		return vite_vad;
	}

	public void setDelayframe(int delayframe) {
		this.delayframe = delayframe;
	}
	public int getDelayframe() {
		return delayframe;
	}

	public void setSystime(int systime) {
		this.systime = systime;
	}
	public int getSystime() {
		return systime;
	}

	public void setSestime(int sestime) {
		this.sestime = sestime;
	}
	public int getSestime() {
		return sestime;
	}
	
	public void setRes(String res) {
		this.res = res;
	}
	public String getRes() {
		return res;
	}


	public void setRectime(int rectime) {
		this.rectime = rectime;
	}
	public int getRectime() {
		return rectime;
	}

	public void setWavetime(int wavetime) {
		this.wavetime = wavetime;
	}
	public int getWavetime() {
		return wavetime;
	}

	public void setPrutime(int prutime) {
		this.prutime = prutime;
	}
	public int getPrutime() {
		return prutime;
	}

	public void setConf(float conf) {
		this.conf = conf;
	}
	public float getConf() {
		return conf;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	public String getVersion() {
		return version;
	}
}