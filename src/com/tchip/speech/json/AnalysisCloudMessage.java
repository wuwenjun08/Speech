package com.tchip.speech.json;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.tchip.aispeech.util.SpeechConfig;
import com.tchip.speech.SpeechService;
import com.tchip.speech.WeatherInfo;

/**
 * 
 * 处理服务器云端返回数据
 * 
 * @author wwj
 *
 */
public class AnalysisCloudMessage{
    private Context context;
	
    public AnalysisCloudMessage(Context context) {
		// TODO Auto-generated constructor stub
    	this.context = context;
		
		weatherInfo = new WeatherInfo(context);
	}

	/**
     * 解析数据
     * @param msg
     */
    public String analysis(String msg) {    	
    	SpeechInfo scInfo=JSON.parseObject(msg, SpeechInfo.class);
		String result = scInfo.getResult();
		Log.d("wwj_test", "result : " + result);
		if(!(result == null)){
			CloudResultInfo crInfo = JSON.parseObject(result, CloudResultInfo.class);
			String semantics = crInfo.getSemantics();
			String input = crInfo.getInput();
			Log.d("wwj_test", "semantics : " + semantics);
			Log.d("wwj_test", "input : " + input);
			sendUserMessage(input);
			
			//处理一些日常用语
			if(SpeechConfig.hello.equals(input)){
				//hello
				return SpeechConfig.helloAck;
			}else if(input.contains(SpeechConfig.userDo) && input.contains(SpeechConfig.me)){
				//我可以说什么
				return SpeechConfig.userDoAck;
			}else if(SpeechConfig.goBack.equals(input)){
				//返回桌面
				Intent intentLauncher = new Intent();
				ComponentName comp = new ComponentName("com.tchip.carlauncher", "com.tchip.carlauncher.MainActivity");
				intentLauncher.setComponent(comp);
				intentLauncher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intentLauncher);
    			return SpeechConfig.goCarLaunchering;
			}else if(input.contains("第") && input.contains("个")){
				//启动到导航后，用户选择导航目的地
        		//Toast.makeText(context, input, Toast.LENGTH_LONG).show();
				return SpeechConfig.mapNavChoice;
			}else if(input.contains("第") && input.contains("个")){
				//启动到导航后，用户选择导航目的地
        		//Toast.makeText(context, input, Toast.LENGTH_LONG).show();
				return SpeechConfig.mapNavChoice;
			}else{
				if(semantics == null){
					return SpeechConfig.unKnown;
				}
			}
			
			if(!(semantics == null)){
				CloudSemanticsInfo csInfo = JSON.parseObject(semantics, CloudSemanticsInfo.class);
				String request = csInfo.getRequest();
				if(!(request == null)){
					CloudRequestInfo cqInfo = JSON.parseObject(request, CloudRequestInfo.class);
					String domain = cqInfo.getDomain();
					String action = cqInfo.getAction();
					String param = cqInfo.getParam();

					Log.d("wwj_test", "domain : " + domain);
					Log.d("wwj_test", "action : " + action);
					Log.d("wwj_test", "param : " + param);
					return actionDo(domain, action, param);
				}
			}
			return SpeechConfig.unKnown;
		}else{
			return null;
		}
    }
    
    /**
     * 给界面发送广播数据
     * @param msg
     */
    private void sendUserMessage(String msg){
    	Intent intent = new Intent(SpeechConfig.userMesage);
    	intent.putExtra("value", msg);
    	context.sendBroadcast(intent);
    }
    
    /**
     * 返回参数处理
     * @param domain
     * @param action
     * @param param
     */
    private String actionDo(String domain, String action, String param){
    	if(SpeechConfig.calendar.equals(domain)/* && calendar.equals(action)*/){
    		//时间
    		//if(param != null && param.contains(SpeechConfig.time)){
    			Date d = new Date();
    			return d.toLocaleString();
    		//}
    	}else if(SpeechConfig.map.equals(domain)){
    		//导航
    		if(param != null){
    			if(param.contains("取消导航")){
        			context.sendBroadcast(new Intent("com.tchip.KILL_APP").putExtra("value", "map"));
        			return SpeechConfig.closeNaving;
        		}
    			// 跳转到自写导航界面，不使用GeoCoder
    			int start = param.indexOf("终点名称") + 7;
    			param = param.substring(start);
    			param = param.substring(0, param.indexOf("\""));
    			try{
					Intent intentNavi = new Intent();
					ComponentName comp = new ComponentName("com.tchip.baidunavi", "com.tchip.baidunavi.ui.activity.MainActivity");
					intentNavi.setComponent(comp);
					intentNavi.putExtra("destionation", param);
					intentNavi.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);					
					context.startActivity(intentNavi);
					context.sendBroadcast(new Intent("com.tchip.BAIDU_NAVI_POS").putExtra("destionation", "param"));
				}catch(Exception e){
					//没有发现carlauncher
				}
    			return SpeechConfig.mapNav;
    		}
    	}else if(SpeechConfig.weather.equals(domain)){
    		//天气
    		if(param != null){
    			//获取城市
    			int start = param.indexOf("城市") + 5;
    			String city = null;
    			if(start != 4){ //返回没有城市
    				city = param.substring(start);
	    			city = city.substring(0, city.indexOf("\""));
    			}
    			if(city == null){
    				return null;
    			}

    			//获取日期
    			start = param.indexOf("日期") + 5;
    			String date = null;
    			if(start != 4){ //返回没有日期
	    			date = param.substring(start);
	    			date = date.substring(0, date.indexOf("\""));
    			}
    			if(date == null){
    				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    				date = sdf.format(new Date());
    			}
    			
    			//在存储中获取本地天气数据
    			/*String storeAddr = preferences.getString("addrStr", null);
    			if(storeAddr != null && storeAddr.contains(city)){
    				//存储中有当前城市
    				String storeDate = null;
    				for(int i=0;i<6;i++){
    					storeDate = preferences.getString("day" + i + "date", null);
    					if(storeDate != null){
    						storeDate = storeDate.replace("-", "");
    						if(storeDate.equals(date)){
    							return preferences.getString("day" + i + "weather", null) + "，" +
    									preferences.getString("day" + i + "wind", null) + "，最高气温" + 
    									preferences.getString("day" + i + "tmpHigh", null) +  "，最低气温" + 
    									preferences.getString("day" + i + "tmpLow", null);
    						}
    					}
    				}
    				//天气不是最新的
    				getWeather(city, date);
    				return SpeechConfig.weather;
    			}else{
    				//不是本地天气
    				getWeather(city, date);
    				return SpeechConfig.weather;
    			}*/
				getWeather(city, date);
				return SpeechConfig.weather;
    		}
    	}
    	return SpeechConfig.unKnown;
    }
    
    
    /*
     * 天气，从讯飞接口获取
     */
    WeatherInfo weatherInfo;
    private void getWeather(String city, String date){
    	weatherInfo.getWeather(city, date);
    }
}