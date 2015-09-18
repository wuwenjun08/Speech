package com.tchip.iflytek;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.tchip.aispeech.util.SpeechConfig;

/**
 * Created by AlexZhou on 2015/4/21. 9:45
 */
public class SpeakService extends Service {

	private static String TAG = SpeakService.class.getSimpleName();
	private SpeechSynthesizer mTts; // 语音合成对象
	private String voicer = "xiaoyan"; // 默认发音人
	// private String[] cloudVoicersEntries;
	// private String[] cloudVoicersValue;
	private int mPercentForBuffering = 0; // 缓冲进度
	private int mPercentForPlaying = 0; // 播放进度
	// private RadioGroup mRadioGroup; // 云端/本地单选按钮
	// 引擎类型： TYPE_CLOUD TYPE_LOCAL
	private String mEngineType = SpeechConstant.TYPE_LOCAL;

	private SharedPreferences mSharedPreferences;
	private String content = "";
	
	private boolean back = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		try {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				content = extras.getString("content");
				back = extras.getBoolean("back");
			}
		} catch (Exception e) {
			//
		}
		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
		mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME,
				Context.MODE_PRIVATE);

		// mInstaller = new ApkInstaller(SpeakService.this);

		// 设置参数
		setParam();
		int code = mTts.startSpeaking(content, mTtsListener);
		if (code != ErrorCode.SUCCESS) {
			if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
				// 未安装则跳转到提示安装页面 mInstaller.install();
			} else {
				// 语音合成失败,错误码:code
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 初始化监听。
	 */
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				// 初始化失败,错误码： code
				if(back)
					SpeakService.this.sendBroadcast(new Intent(SpeechConfig.iflytekTTSMesage));
			} else {
				// 初始化成功，之后可以调用startSpeaking方法
				// 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
				// 正确的做法是将onCreate中的startSpeaking调用移至这里
				mTts.startSpeaking(content, mTtsListener);
			}
		}
	};

	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		@Override
		public void onSpeakBegin() {
			// 开始播放
		}

		@Override
		public void onSpeakPaused() {
			// 暂停播放
		}

		@Override
		public void onSpeakResumed() {
			// 继续播放
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			// 合成进度
			mPercentForBuffering = percent;
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// 播放进度
			mPercentForPlaying = percent;
		}

		@Override
		public void onCompleted(SpeechError error) {
			//发送completed消息
			if(back)
				SpeakService.this.sendBroadcast(new Intent(SpeechConfig.iflytekTTSMesage));
			
			if (error == null) {
				// 播放完成
				content = "";
				stopSelf();
			} else if (error != null) {
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

		}
	};

	private void setParam() {
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// 设置合成
		if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE,
					SpeechConstant.TYPE_CLOUD);
			// 设置发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);

		} else {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE,
					SpeechConstant.TYPE_LOCAL);
			// 设置发音人 voicer为空默认通过语音+界面指定发音人。
			mTts.setParameter(SpeechConstant.VOICE_NAME, "");

		}
		// 设置语速
		mTts.setParameter(SpeechConstant.SPEED,
				mSharedPreferences.getString("speed_preference", "50"));
		// 设置音调
		mTts.setParameter(SpeechConstant.PITCH,
				mSharedPreferences.getString("pitch_preference", "50"));
		// 设置音量
		mTts.setParameter(SpeechConstant.VOLUME,
				mSharedPreferences.getString("volume_preference", "50"));
		// 设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE,
				mSharedPreferences.getString("stream_preference", "3"));
	}
}
