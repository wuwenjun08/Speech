package com.tchip.speech;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.tchip.aispeech.util.SpeechConfig;
import com.tchip.carlauncher.util.NetworkUtil;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


/**
 * 当系统启动后系统speech服务
 * 
 * @author wu
 *
 */
public class SystemBootCompletedReceiver extends BroadcastReceiver {
	private String TAG = "SystemBootCompletedReceiver";
	private Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
		Log.d("wwj_test", "action : " + intent.getAction());
		//Toast.makeText(context, intent.getAction(), Toast.LENGTH_LONG).show();
		/*SharedPreferences preferences = context.getSharedPreferences(SpeechConfig.SHARED_PREFERENCES_SPEECH_NAME, Context.MODE_PRIVATE);
		if(!preferences.getBoolean("speech_init", false) && NetworkUtil.isNetworkConnected(context)){
        	startMyService(context);
        }else if(preferences.getBoolean("speech_init", false)){
        	startMyService(context);
        }*/
    }
    
	
	/**
	 *  启动思必驰语音服务
	 * @param context
	 */
	private void startMyService(Context context){
		Intent intent = new Intent(context, SpeechService.class);
		context.startService(intent);
	}
}
