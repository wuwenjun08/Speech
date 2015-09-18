/**
 * 本地语音合成
 * by wwj
 */
package com.tchip.speech;
import android.content.Context;
import android.util.Log;

import com.aispeech.AIError;
import com.aispeech.common.AIConstant;
import com.aispeech.export.engines.AILocalTTSEngine;
import com.aispeech.export.listeners.AITTSListener;

public class SpeechLocalTTS {
	
	interface OnSpeechLocalTTSCompleteListener{
		void complete();
		void error();
	}
	OnSpeechLocalTTSCompleteListener listener;
	public void setOnSpeechLocalTTSCompleteListener(OnSpeechLocalTTSCompleteListener listener){
		this.listener = listener;
	}

	final static String CN_PREVIEW = "苏州今天天气3℃到9℃ 阴，偏北风3到5级，明天5℃到11℃ 多云转晴，后天8℃到15℃";

	final String Tag = this.getClass().getName();

	static final String model_f_xiaoliu = "anonyf_female.bin";
	static final String model_f_zhiling = "zhilingf_female.bin";
	static final String zipFileName = "tts.zip.imy";

	String resDir;

	AILocalTTSEngine localEngine;
	private boolean localTTSEngineInit = false;
	private static String TAG = "SpeechLocalTTS";

	private Context context;

	public SpeechLocalTTS(Context context) {
		this.context = context;

		localEngine = AILocalTTSEngine.getInstance();
		initEngine(model_f_zhiling); // model_f_xiaoliu
	}

	/**
	 * 云端语音合成
	 * 
	 * @param text
	 * @return
	 */
	public boolean localTTS(String text) {
		if (localTTSEngineInit) {
			localEngine.setLanguage(AIConstant.CN_TTS);
			localEngine.speak(text, "1024");
		}
		return localTTSEngineInit;
	}

	private void initEngine(String modelName) {
		if (localEngine != null) {
			localEngine.setResource(zipFileName, modelName);
			//localEngine.setUseService(true);
			localEngine.init(context, new AILocalTTSListenerImpl(), AppKey.APPKEY, AppKey.SECRETKEY);
		}
	}

	private class AILocalTTSListenerImpl implements AITTSListener {

		public void onError(AIError error) {
			// tip.setText("检测到错误");
			// content.setText(content.getText() + "\nError:\n" +
			// error.toString());
		}

		@Override
		public void onInit(int status) {
			Log.i(Tag, "初始化完成，返回值：" + status);
			if (status == AIConstant.OPT_SUCCESS) {
				// tip.setText("初始化成功!");
				localTTSEngineInit = true;
			} else {
				// tip.setText("初始化失败!code:" + status);
				localTTSEngineInit = false;
			}
		}

		@Override
		public void onCompletion(String arg0) {
			// TODO Auto-generated method stub
			// tip.setText("合成完成");
        	listener.complete();
		}

		@Override
		public void onError(String arg0, AIError arg1) {
			// TODO Auto-generated method stub
			listener.error();
		}

		@Override
		public void onProgress(int arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReady(String arg0) {
			// TODO Auto-generated method stub

		}

	}

}
