/*******************************************************************************
 * Copyright 2014 AISpeech
 ******************************************************************************/
package com.tchip.aispeech.util;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;

import com.aispeech.common.AIConstant;

public class Tools {

	public static String getSystemTime() {
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		return df.format(date);
	}
	
	/**
	 * 过滤掉非英文字符
	 * @param refText
	 * @return
	 */
	public static String enFormat(String refText) {
		if(refText != null){
			refText = refText.replaceAll("[^\u0020-\u007e]", "");
		}
		return refText;
	}
	
	/**
	 * 打开系统语音识别设置面板
	 * @param context
	 */
	public static void openSystemASRSettingPannel(final Context context){
		Intent intent = new Intent();
		try{
		if(isSDKVersionOver13()){
			intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$InputMethodAndLanguageSettingsActivity"));
		}else{
			intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.VoiceInputOutputSettings"));
		}
		context.startActivity(intent);
		}catch (Exception e) {
			e.printStackTrace();
			try{
				Intent intentb = new Intent();
				intentb.setAction("com.android.settings.VOICE_INPUT_OUTPUT_SETTINGS");
				context.startActivity(intentb);
			}catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 是否SDK版本大于13
	 * @return
	 */
	public static boolean isSDKVersionOver13() {
		return Build.VERSION.SDK_INT > 13;
	}
	
	/**
	 * 打开系统语音合成面板
	 * @param context
	 */
	public static void openSystemTTSSettingPannel(final Context context){
		try{
			Intent intent = new Intent();
			intent.setAction("com.android.settings.TTS_SETTINGS");
			context.startActivity(intent);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 返回是否将�?�必驰语音识别作为默认的识别引擎
	 * @param context
	 * @return
	 */
	public static boolean isAISpeechASRIsDefaultASREngine(final Context context){
		try{
			return  Settings.Secure.getString(context.getContentResolver(), "voice_recognition_service").contains(AIConstant.AISERVER_PACKAGE);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 返回是否将�?�必驰语音合成作为默认的合成引擎
	 * @param context
	 * @return
	 */
	public static boolean isAISpeechTTSIsDefaultTTSEngine(final Context context, final TextToSpeech tts){
		try{
			return  context.getPackageName().equals(tts.getDefaultEngine());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
    /**
     * 返回当前Wifi是否连接�?
     * @param context
     * @return true 已连�?
     */
    public static boolean isWifiConnected(Context context){
    	ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = conMan.getActiveNetworkInfo();
		if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI){
			return true;
		}
		return false;
    }


}
