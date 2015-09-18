package com.tchip.speech;

import java.io.File;

import android.app.Application;
import android.os.Environment;
import com.iflytek.cloud.SpeechUtility;
import com.tchip.aispeech.util.SpeechConfig;

public class MyApplication extends Application {

	@Override
	public void onCreate() {

		// 应用程序入口处调用,避免手机内存过小,杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
		// 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
		// 参数间使用“,”分隔。
		SpeechUtility.createUtility(this, "appid=" + SpeechConfig.XUNFEI_APP_ID);
		super.onCreate();
	}
}
