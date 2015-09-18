package com.tchip.speech;

import com.aispeech.AIError;
import com.aispeech.AIResult;
import com.aispeech.common.AIConstant;
import com.aispeech.common.JSONResultParser;
import com.aispeech.export.engines.AICloudASREngine;
import com.aispeech.export.listeners.AIASRListener;

import android.content.Context;
import android.util.Log;


/**
 * 
 * 思必驰语音云端识别，只是别语音，不识别语义
 * @author wwj
 *
 */
public class SpeechCloudASRCar{
	
	private static String TAG = "SpeechCloudASRCar";
	private Context context;
	private AICloudASREngine mEngine;
	
	SpeechCASRDialog dialog;
	
	interface OnSpeechCloudASRCarListenering{
		void result(String result);
	}
	OnSpeechCloudASRCarListenering listener;
	public void setOnSpeechCloudASRCarListenering(OnSpeechCloudASRCarListenering listener){
		this.listener = listener;
	}
	
	public SpeechCloudASRCar(Context context){
		this.context = context;
		
		dialog = new SpeechCASRDialog(context);
	}
	
	public void startSpeechListen(){
		mEngine = AICloudASREngine.createInstance();
		mEngine.setVadResource("vad.aicar.0.0.3.bin");
		mEngine.setServer("ws://s-test.api.aispeech.com:10000"); 		
		mEngine.setRes("chezai");
		
		mEngine.init(context, new AICloudASRListenerImpl(), AppKey.APPKEY, AppKey.SECRETKEY);
		mEngine.setNBest(1);
		mEngine.setPauseTime(2000);
		mEngine.setAttachAudioUrl(true);
		mEngine.setDeviceId("aA-sS_dD");
		
		dialog.show();
		dialog.speechListen();
	}
	
	public void endSpeechListen(){
		dialog.dismiss();
		//mEngine.cancel();
	}
	

	private class AICloudASRListenerImpl implements AIASRListener {

		@Override
		public void onReadyForSpeech() {
			//resultText.setText("请说话...");
		}

		@Override
		public void onBeginningOfSpeech() {
			//resultText.setText("检测到说话");
			dialog.speechListening();
		}

		@Override
		public void onEndOfSpeech() {
			//resultText.setText("检测到语音停止，开始识别...\n");
			dialog.speechCASRing();
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			//showTip("RmsDB = " + rmsdB);
		}

		@Override
		public void onError(AIError error) {
			//Log.i(Tag, "error:" + error.toString());
			//resultText.setText(error.toString());
			mEngine.start();
			dialog.speechListen();
		}

		@Override
		public void onResults(AIResult results) {
			if (results.isLast()) {
				if (results.getResultType() == AIConstant.AIENGINE_MESSAGE_TYPE_JSON) {
					Log.i(TAG, "result JSON = " + results.getResultObject().toString());
					// 可以使用JSONResultParser来解析识别结果
					// 结果按概率由大到小排序
					JSONResultParser parser = new JSONResultParser(results.getResultObject().toString());
					for (String s : parser.getNBestRec()) {
						Log.d("wwj_test", "result : " + s);
						listener.result(s);
						mEngine.start();
						dialog.speechListen();
					}
				}else {
					mEngine.start();
					dialog.speechListen();
				}
			}
		}

		@Override
		public void onInit(int status) {
			Log.i(TAG, "Init result " + status);
			if (status == AIConstant.OPT_SUCCESS) {
				//初始化成功
				mEngine.start();
			} else {
				dialog.dismiss();
			}
		}
		@Override
		public void onRecorderReleased() {
			// TODO Auto-generated method stub
			
		}

	}
}