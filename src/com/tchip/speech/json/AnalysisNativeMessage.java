package com.tchip.speech.json;

import java.lang.reflect.Method;
import java.util.List;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.tchip.aispeech.util.SpeechConfig;

/**
 * 
 * 处理服务器云端返回数据
 * 
 * @author wwj
 *
 */
public class AnalysisNativeMessage{
    private Context context;
    public AnalysisNativeMessage(Context context) {
		// TODO Auto-generated constructor stub
    	this.context = context;
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
			NativeResultInfo nrInfo = JSON.parseObject(result, NativeResultInfo.class);
			String post = nrInfo.getPost();
			String rec = nrInfo.getRec();
			float conf = nrInfo.getConf();
			
			//Toast.makeText(context, "" + conf, Toast.LENGTH_LONG).show();
			Log.d("wwj_test", "post : " + post);
			Log.d("wwj_test", "rec : " + rec);

			
			sendUserMessage(rec);
			if(conf < 0.5f){				
				return "";
			}
			
			if(!(post == null)){
				NativePostInfo npInfo = JSON.parseObject(post, NativePostInfo.class);
				String sem = npInfo.getSem();
				if(!(sem == null)){
					String domain = null;
					String action = null;
					if(sem.contains(SpeechConfig.phone)){
						//电话
						if(sem.contains(SpeechConfig.number)){
							NativeSemNumberInfo nsInfo = JSON.parseObject(sem, NativeSemNumberInfo.class);
							domain = nsInfo.getDomain();
							action = nsInfo.getAction();
							String number = nsInfo.getNumber();
							return actionDo(domain, action, number, false);
						}else if(sem.contains(SpeechConfig.person)){
							NativeSemPersonInfo nsInfo = JSON.parseObject(sem, NativeSemPersonInfo.class);
							domain = nsInfo.getDomain();
							action = nsInfo.getAction();
							String person = nsInfo.getPerson();
							return actionDo(domain, action, person, true);
						}
					}else if(sem.contains(SpeechConfig.music)){
						//打开app，播放音乐
						NativeSemInfo nsInfo = JSON.parseObject(sem, NativeSemInfo.class);
						domain = nsInfo.getDomain();
						action = nsInfo.getAction();
						String appname = nsInfo.getAppname();
						return actionDo(domain, action, appname, false);
					}else if(sem.contains(SpeechConfig.t_chip)){
						//锁屏、返回桌面、打开设置、fm、地图
						NativeSemInfo nsInfo = JSON.parseObject(sem, NativeSemInfo.class);
						domain = nsInfo.getDomain();
						action = nsInfo.getAction();
						//String appname = nsInfo.getAppname();
						if(rec != null)
							rec = rec.replace(" ", ""); //去掉空格
						return actionDo(domain, action, rec, false);
					}else if(sem.contains("open") && sem.contains("app")){
						//打开app
						NativeSemInfo nsInfo = JSON.parseObject(sem, NativeSemInfo.class);
						domain = nsInfo.getDomain();
						action = nsInfo.getAction();
						String appname = nsInfo.getAppname();
	
						//Log.d("wwj_test", "domain : " + domain);
						//Log.d("wwj_test", "action : " + action);
						//Log.d("wwj_test", "appname : " + appname);
						return actionDo(domain, action, appname, false);
					}else if(sem.contains(SpeechConfig.volume)){
						//调整音量
						NativeSemInfo nsInfo = JSON.parseObject(sem, NativeSemInfo.class);
						domain = nsInfo.getDomain();
						action = nsInfo.getAction();
						return actionDo(domain, action, null, false);
					}else if(sem.contains(SpeechConfig.brightness)){
						//调整亮度
						NativeSemInfo nsInfo = JSON.parseObject(sem, NativeSemInfo.class);
						domain = nsInfo.getDomain();
						action = nsInfo.getAction();
						return actionDo(domain, action, null, false);
					}
				}
			}
			return "";
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
     * 
     * 打开app
     * @param domain
     * @param action
     * @param detail
     * @param person
     * @return
     */
    private String actionDo(String domain, String action, String detail, boolean person){
    	//先去掉空格
    	domain = domain.replace(" ", "");
    	if(SpeechConfig.app.equals(domain)){
    		//打开app
	    	if(SpeechConfig.baiduMap.equals(detail)){
	    		try{
					Intent intent = new Intent();
		    		//ComponentName comp = new ComponentName("com.baidu.BaiduMap", "com.baidu.baidumaps.WelcomeScreen");
		    		ComponentName comp = new ComponentName("com.baidu.navi.hd", "com.baidu.navi.NaviActivity");		    		
		    		intent.setComponent(comp);
		    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    		context.startActivity(intent);
		    		return "正在打开百度地图";
	    		}catch(Exception e){
		    		return "没有找到百度地图";
				}
	    	}else if(SpeechConfig.kuwoMusic.contains(detail) || SpeechConfig.kugouMusic.contains(detail)){
    			//打开酷我音乐盒或者酷狗音乐
	    		return openMusic();
	    	}
    	}else if(SpeechConfig.music.equals(domain)){
    		//音乐播放
    		if(SpeechConfig.musicAction[0].equals(action) || SpeechConfig.musicAction[1].equals(action) || SpeechConfig.musicAction[2].equals(action)){
				Log.d("wwj_test", "打开酷我音乐盒");
    			//打开酷我音乐盒
				return openMusic();
    		}
    	}else if(SpeechConfig.phone.equals(domain)){
    		//打电话
    		if(detail != null){
    			if(person){
    				detail = findNumberByName(detail);
    			}
    			Intent intent = new Intent(Intent.ACTION_CALL , Uri.parse("tel:" +  detail));  
    			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);  
	    		return "正在拨打" + detail;
    		}
    	}else if(SpeechConfig.t_chip.equals(domain)){
    		//自己定制的本地命令
    		if(SpeechConfig.screenOff.contains(detail)){
    			//关闭屏幕
    			return SpeechConfig.screenOff;
    		}else if(SpeechConfig.goCarLauncher.contains(detail)){
    			//返回桌面
    			try{
					Intent intentLauncher = new Intent();
					ComponentName comp = new ComponentName("com.tchip.carlauncher", "com.tchip.carlauncher.ui.activity.MainActivity");
					intentLauncher.setComponent(comp);
					intentLauncher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intentLauncher);
    			}catch(Exception e){
    				//没有发现carlauncher
        			return SpeechConfig.goFailed;
    			}
    			return SpeechConfig.goCarLaunchering;
    		}else if(SpeechConfig.goSettings.equals(detail)){
    			//打开设置
    			try{
					Intent intentLauncher = new Intent();
					ComponentName comp = new ComponentName("com.tchip.carlauncher", "com.tchip.carlauncher.ui.activity.SettingActivity");
					intentLauncher.setComponent(comp);
					intentLauncher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intentLauncher);
    			}catch(Exception e){
    				//没有发现设置
    				Log.d("wwj_test", "exception : " + e.getMessage());
        			return SpeechConfig.goFailed;
    			}
    			return SpeechConfig.goSettingsing;
    		}else if(SpeechConfig.goFM.equals(detail)){
    			//打开FM
    			try{
					Intent intentLauncher = new Intent();
					ComponentName comp = new ComponentName("com.tchip.carlauncher", "com.tchip.carlauncher.ui.activity.FmTransmitActivity");
					intentLauncher.setComponent(comp);
					intentLauncher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intentLauncher);
    			}catch(Exception e){
    				//没有发现FM
    				//Toast.makeText(context, "打开fm失败", Toast.LENGTH_LONG).show();
        			return SpeechConfig.goFailed;
    			}
    			return SpeechConfig.goFMing;
    		}else if(SpeechConfig.goMap.contains(detail)){
    			//打开地图
    			try{
					Intent intentLauncher = new Intent();
					ComponentName comp = new ComponentName("com.tchip.baidunavi", "com.tchip.baidunavi.ui.activity.MainActivity");
					intentLauncher.setComponent(comp);
					intentLauncher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intentLauncher);
    			}catch(Exception e){
    				//没有发现地图
        			return SpeechConfig.goFailed;
    			}
    			return SpeechConfig.goMaping;
    		}else if(SpeechConfig.goMusic.equals(detail)){
    			//打开音乐
    			return openMusic();
    		}else if(SpeechConfig.closeMusic.equals(detail)){
    			//关闭音乐
    			context.sendBroadcast(new Intent("com.tchip.KILL_APP").putExtra("value", "music"));
    			return SpeechConfig.closeMusicing;
    		}else if(SpeechConfig.closeMap.equals(detail)){
    			//关闭地图
    			context.sendBroadcast(new Intent("com.tchip.KILL_APP").putExtra("value", "map"));
    			return SpeechConfig.closeMaping;
    		}else if(SpeechConfig.closeNav.equals(detail)){
    			//关闭导航
    			context.sendBroadcast(new Intent("com.tchip.KILL_APP").putExtra("value", "map"));
    			return SpeechConfig.closeNaving;
    		}
    	}else if(SpeechConfig.volume.equals(domain)){
    		//调整音量
    		if(action.equals("up")){
    			return SpeechConfig.upVolume;
    		}else if(action.equals("down")){
    			return SpeechConfig.downVolume;
    		}else if(action.equals("mute_on")){
    			return SpeechConfig.muteOnVolume;
    		}else if(action.equals("min")){
    			return SpeechConfig.minVolume;
    		}else if(action.equals("max")){
    			return SpeechConfig.maxVolume;
    		}
    	}else if(SpeechConfig.brightness.equals(domain)){
    		//调整亮度
    		if(action.equals("up")){
    			return SpeechConfig.upBrightness;
    		}else if(action.equals("down")){
    			return SpeechConfig.downBrightness;
    		}else if(action.equals("min")){
    			return SpeechConfig.minBrightness;
    		}else if(action.equals("max")){
    			return SpeechConfig.maxBrightness;
    		}
    	}
    	
    	return "";
    }
    
    /*
     * 处理打开酷我音乐还是打开酷狗音乐
     */
    private String openMusic(){
    	ComponentName componentMusic;
		if(SpeechConfig.isOnlineMusicKuwo){
			componentMusic = new ComponentName("cn.kuwo.kwmusichd", "cn.kuwo.kwmusichd.WelcomeActivity");
		}else{
			componentMusic = new ComponentName("com.kugou.playerHD2", "com.kugou.playerHD.activity.SplashActivity");
		}
		try{
			//ComponentName componentMusic = new ComponentName("cn.kuwo.player", "cn.kuwo.player.activities.EntryActivity");
			Intent intentMusic = new Intent();
			intentMusic.setComponent(componentMusic);
			intentMusic.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intentMusic);
			sendMusicBroadcast();
			return SpeechConfig.goToMusic;
		}catch(Exception e){					
			if(SpeechConfig.isOnlineMusicKuwo)
				return "没有找到酷我音乐盒";
			else
				return "没有找到酷狗音乐";
		}
    }

    
    private static final String[] PHONES_PROJECTION = new String[] {
        Phone.DISPLAY_NAME, Phone.NUMBER};
    
    /**联系人显示名称**/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;
 
    /**电话号码**/
    private static final int PHONES_NUMBER_INDEX = 1;
	// 查询指定联系人的电话
	public String findNumberByName(String name) {
		String number = null;
		ContentResolver resolver = context.getContentResolver();
	      
	    // 获取手机联系人  
	    Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,PHONES_PROJECTION, null, null, null);  
	      
	      
	    if (phoneCursor != null) {  
	        while (phoneCursor.moveToNext()) {  

		        String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);  
		        if(name.equals(contactName)){
		        	number = phoneCursor.getString(PHONES_NUMBER_INDEX); 
		        	return number;
		        }
	        }  
	        phoneCursor.close();
	    }
		return number;
	}
	
	/**
	 * 发送音乐播放广播
	 */
	private void sendMusicBroadcast(){
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent("com.android.music.musicservicecommand");
		        intent.putExtra("command", "play");
		        context.sendBroadcast(intent);
			}
			
		}, 2000);
	}
}