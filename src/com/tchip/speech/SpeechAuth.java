package com.tchip.speech;

import com.aispeech.speech.AIAuthEngine;

import android.content.Context;

/**
 * 
 * 思必驰语音服务授权
 * @author wwj
 *
 */
public class SpeechAuth{
	private Context context;
	
	//授权
    private AIAuthEngine mEngine;
    private boolean isAuthed = false;
    
	public SpeechAuth(Context context){
		this.context = context;
	}
	
	/**
	 * 授权
	 * 验证授权
	 * @return
	 */
	public boolean auth(){
		mEngine = AIAuthEngine.getInstance(context);
		//mEngine.init(AppKey.APPKEY, AppKey.SECRETKEY,Util.getDid(context));// TODO 换成您的s/n码
        mEngine.init(AppKey.APPKEY, AppKey.SECRETKEY,"aA-sS_dD");
		isAuthed = mEngine.isAuthed();
		//当没有授权时，执行一次授权
		if(!isAuthed){
			isAuthed = mEngine.doAuth();        
		}
		return isAuthed;
	}
}