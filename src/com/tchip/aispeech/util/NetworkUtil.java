package com.tchip.aispeech.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

    /**
     * 返回网络状�??
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) { 
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context 
                .getSystemService(Context.CONNECTIVITY_SERVICE); 
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo(); 
            if (mNetworkInfo != null) { 
                return mNetworkInfo.isAvailable(); 
            } 
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
