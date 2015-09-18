/*******************************************************************************
 * Copyright 2014 AISpeech
 ******************************************************************************/
package com.tchip.speech;

import java.io.File;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.aispeech.AIError;
import com.aispeech.common.AIConstant;
import com.aispeech.export.engines.AILocalTTSEngine;
import com.aispeech.export.listeners.AITTSListener;
import com.tchip.iflytek.SpeakService;

public class LocalTTSService extends Activity implements OnClickListener, OnCheckedChangeListener {

	final static String CN_PREVIEW = "苏州今天天气3℃到9℃ 阴，偏北风3到5级，明天5℃到11℃ 多云转晴，后天8℃到15℃";

	final String Tag = this.getClass().getName();

	static final String model_f_xiaoliu = "anonyf_female.bin";
	static final String model_f_zhiling = "zhilingf_female.bin";
	static final String zipFileName = "tts.zip.imy";

	TextView tip;
	EditText content;
	Button btnStart, btnPlayerPause, btnPlayerResume, btnPlayerStop;
	RadioGroup resSelect;
	String resDir;
	

	AILocalTTSEngine mEngine;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_tts);

		tip = (TextView) findViewById(R.id.tip);
		content = (EditText) findViewById(R.id.content);
		btnStart = (Button) findViewById(R.id.btn_start);
		btnPlayerPause = (Button) findViewById(R.id.btn_pause);
		btnPlayerResume = (Button) findViewById(R.id.btn_resume);
		btnPlayerStop = (Button) findViewById(R.id.btn_stop);
		resSelect = (RadioGroup) findViewById(R.id.resSelect);
		resSelect.setOnCheckedChangeListener(this);
		content.setText(CN_PREVIEW);

		btnStart.setEnabled(false);
		btnPlayerPause.setOnClickListener(this);
		btnPlayerResume.setOnClickListener(this);
		btnPlayerStop.setOnClickListener(this);
		btnStart.setOnClickListener(this);

		//mEngine = AILocalTTSEngine.getInstance();
		
		//initEngine(model_f_zhiling);
		
		Intent intent = new Intent(this, SpeechService.class);
		startService(intent);

//		Intent intentNavi = new Intent();
//		ComponentName comp = new ComponentName("com.tchip.carlauncher", "com.tchip.carlauncher.ui.activity.NavigationActivity");
//		intentNavi.setComponent(comp);
//		intentNavi.putExtra("destionation", "科技园");
//		intentNavi.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		startActivity(intentNavi);
//		startSpeak("你好，我是讯飞语音");
	}
	private void startSpeak(String msg) {
		Intent intent = new Intent(this, SpeakService.class);
		intent.putExtra("content", msg);
		startService(intent);
	}

	private void initEngine(String modelName) {
		if(mEngine != null){
			mEngine.setResource(zipFileName, modelName);
			//mEngine.setUseService(true);
			mEngine.init(this, new AILocalTTSListenerImpl() , AppKey.APPKEY, AppKey.SECRETKEY);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == btnStart) {
			String refText = content.getText().toString();

			String savePath = android.os.Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ File.separator
					+ System.currentTimeMillis() + ".wav";
			
			if (!TextUtils.isEmpty(refText)) {
				if (mEngine != null) {
					// 转存音频
					mEngine.setSavePath(savePath);
					mEngine.speak(refText, "1024");
				}
				tip.setText("正在合成...");
			} else {
				tip.setText("没有合法文本");
			}
		} else if (v == btnPlayerPause) {
			if (mEngine != null) {
				mEngine.pause();
			}

		} else if (v == btnPlayerResume) {
			if (mEngine != null) {
				mEngine.resume();
			}

		} else if (v == btnPlayerStop) {
			tip.setText("合成已停止");
			if (mEngine != null) {
				mEngine.stop();
			}
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		btnStart.setEnabled(false);
		if (checkedId == R.id.male) {
			initEngine(model_f_zhiling);
		} else if (checkedId == R.id.female) {
			initEngine(model_f_xiaoliu);
		}
	}

	

	private class AILocalTTSListenerImpl implements AITTSListener {


		public void onError(AIError error) {
			tip.setText("检测到错误");
			content.setText(content.getText() + "\nError:\n" + error.toString());
		}

		@Override
		public void onInit(int status) {
			Log.i(Tag, "初始化完成，返回值：" + status);
			if (status == AIConstant.OPT_SUCCESS) {
				tip.setText("初始化成功!");
				btnStart.setEnabled(true);
			} else {
				tip.setText("初始化失败!code:" + status);
			}
		}
		@Override
		public void onCompletion(String arg0) {
			// TODO Auto-generated method stub
			tip.setText("合成完成");
		}

		@Override
		public void onError(String arg0, AIError arg1) {
			// TODO Auto-generated method stub
			
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

	@Override
	public void onStop() {
		super.onStop();
		if (mEngine != null) {
			mEngine.stop();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mEngine != null) {
			Log.i(Tag,"release in LocalTTS");
			mEngine.destory();
			mEngine = null;
		}
	}

}
