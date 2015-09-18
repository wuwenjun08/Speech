package com.tchip.speech;

import com.aispeech.AIError;
import com.aispeech.common.AIConstant;
import com.aispeech.common.Util;
import com.aispeech.export.engines.AICloudTTSEngine;
import com.aispeech.export.engines.AILocalTTSEngine;
import com.aispeech.export.listeners.AITTSListener;
import com.aispeech.speech.AIAuthEngine;
import com.tchip.aispeech.util.SpeechConfig;
import com.tchip.aispeech.util.Tools;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;


/**
 * 
 * 思必驰语音，在此类中，会把语音合成的功能打包，对位提供接口
 * @author wwj
 *
 */

public class SpeechCloudTTS{
	
	interface OnSpeechCloudTTSCompleteListener{
		void complete();
		void error();
	}
	OnSpeechCloudTTSCompleteListener listener;
	public void setOnSpeechCloudTTSCompleteListener(OnSpeechCloudTTSCompleteListener listener){
		this.listener = listener;
	}
	
	private static String TAG = "SpeechEngine";
	
	private Context context;
    
    //云端语音合成
    private AICloudTTSEngine cloudEngine;
    private boolean cloudTTSEngineInit = false;
	
	public SpeechCloudTTS(Context context){
		this.context = context;		
		// 创建云端合成播放器
		cloudEngine = AICloudTTSEngine.createInstance();
		cloudEngine.init(context, new AITTSListenerImpl(), AppKey.APPKEY, AppKey.SECRETKEY);
        // 指定默认中文合成
		cloudEngine.setLanguage(AIConstant.CN_TTS);

        // 默认中文，女声
		cloudEngine.setRes(SpeechConfig.ttsSound);
		cloudEngine.setDeviceId("aA-sS_dD");
	}	
	
	/**
	 * 云端语音合成
	 * @param text
	 * @return
	 */
	public boolean cloudTTS(String text){
		if(cloudTTSEngineInit){
			cloudEngine.setLanguage(AIConstant.CN_TTS);
	        //cloudEngine.setSavePath("/sdcard/sibichi/"+System.currentTimeMillis() + ".mp3");
	        cloudEngine.speak(text, "1024");	
		}
		return cloudTTSEngineInit;
	}
	/*
	 * 云端合成
	 */
    private class AITTSListenerImpl implements AITTSListener {

        @Override
        public void onInit(int status) {
            if (status == AIConstant.OPT_SUCCESS) {
            	cloudTTSEngineInit = true;
            } else {
            	cloudTTSEngineInit = false;
            }
            Log.d("wwj_test", "cloudTTSEngineInit : " + cloudTTSEngineInit);
        }


        @Override
        public void onProgress(int currentTime, int totalTime, boolean isRefTextTTSFinished) {
        	
        }

        @Override
        public void onError(String utteranceId, AIError error) {
        	listener.error();
        }

        @Override
        public void onReady(String utteranceId) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onCompletion(String utteranceId) {
        	listener.complete();
        }

    }
}