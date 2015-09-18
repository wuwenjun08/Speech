package com.tchip.speech;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.json.JSONException;
import org.json.JSONObject;

import com.aispeech.AIError;
import com.aispeech.AIResult;
import com.aispeech.IMergeRule;
import com.aispeech.common.AIConstant;
import com.aispeech.export.engines.AILocalWakeupEngine;
import com.aispeech.export.engines.AIMixASREngine;
import com.aispeech.export.listeners.AIASRListener;
import com.aispeech.export.listeners.AILocalWakeupListener;
import com.tchip.aispeech.util.BeepPlayer;
import com.tchip.aispeech.util.SpeechConfig;
import com.tchip.carlauncher.util.NetworkUtil;
import com.tchip.carlauncher.util.ProgressAnimationUtil;
import com.tchip.iflytek.SpeakService;
import com.tchip.speech.LocalGrammar.OnLocalGrammarInitListener;
import com.tchip.speech.SpeechCloudTTS.OnSpeechCloudTTSCompleteListener;
import com.tchip.speech.SpeechLocalTTS.OnSpeechLocalTTSCompleteListener;
import com.tchip.speech.WakeUpCloudAsr.MessageReceiver;
import com.tchip.speech.json.AnalysisCloudMessage;
import com.tchip.speech.json.AnalysisNativeMessage;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;


/**
 * 
 * 语音后台服务，用于监听呼叫，启动语音助手界面
 * 语音唤醒词“小智”
 * @author wwj
 *
 */
public class SpeechService extends Service {
	private static String Tag = "SpeechService";
	LocalGrammar mLocalGrammar;
    AILocalWakeupEngine mWakeupEngine;
    AIMixASREngine mAsrEngine;
    //初始化本地引擎次数计数
    public static int initIndex = 0;

    SpeechCloudTTS sctts;
    SpeechLocalTTS sltts;
    
    
    BeepPlayer startBeep;
    private CircularFifoQueue<byte[]> mFifoQueue = null;
    String recResult;
    Toast mToast;
    
    LinearLayout chartMsgPanel;
    ScrollView chartMsgScroll;
	private LayoutInflater inflater;
	
	//声明键盘管理器
	KeyguardManager km = null; 
	//声明键盘锁
	private KeyguardLock kl = null; 
	//声明电源管理器
	private PowerManager pm;
	private PowerManager.WakeLock wl;
	private AudioManager mAudioManager;
	

	/*
	 * 接受weather发送的消息广播
	 */
    private WeatherReceiver weatherReceiver;
    public class WeatherReceiver extends BroadcastReceiver {    	
        @Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
        	if(SpeechConfig.weatherMesage.equals(action)){
        		String value = intent.getStringExtra("value");
                sendMachineMessage(value);
                if(SpeechConfig.SIBICHI_CLOUD_TTS){
                	sctts.cloudTTS(value);
                }else{
                	startSpeak(value);
                }
        	}else if(SpeechConfig.iflytekTTSMesage.equals(action)){
        		reStartSpeech();
        	}else if(action.equals("com.tchip.SLEEP_ON")){
        		SpeechService.this.stopSelf();
        	}
        }
    }
	private SharedPreferences preferences;
	private Editor editor;
	@Override
	public void onCreate(){
		super.onCreate();
		
		initIndex = 0;

		//判断是否为初次启动
		//是：检测联网情况，connected 初始化本地引擎 unconnected 关闭service
		//否：跳过
		preferences = getSharedPreferences(SpeechConfig.SHARED_PREFERENCES_SPEECH_NAME, Context.MODE_PRIVATE);
		//Toast.makeText(SpeechService.this, "SpeechService : " + preferences.getBoolean("speech_init", false), Toast.LENGTH_LONG).show();
		if(!preferences.getBoolean("speech_init", false)){
			if(!NetworkUtil.isNetworkConnected(getApplicationContext())){
				SpeechService.this.stopSelf();
			}
		}
		
		if(mLocalGrammar != null)
			return;
        mLocalGrammar = new LocalGrammar(this);
        mLocalGrammar.setOnLocalGrammarInitListener(new OnLocalGrammarInitListener() {
			
			@Override
			public void onExist() {
				// TODO Auto-generated method stub
				//本地引擎存在
				//Toast.makeText(SpeechService.this, "本地引擎存在 ", Toast.LENGTH_LONG).show();
				initSpeechEngine();
				
				//初始化成功，设置参数保存
				editor = preferences.edit();
				editor.putBoolean("speech_init", true);
				editor.commit();
			}
			
			@Override
			public void onError() {
				// TODO Auto-generated method stub
				//引擎初始化错误
				initIndex ++;
				if(initIndex < 6){
					//延时5秒再次初始化，允许失败5次
					//Toast.makeText(SpeechService.this, "引擎初始化错误 " + initIndex, Toast.LENGTH_LONG).show();
					new Handler().postDelayed(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mLocalGrammar.initLocalGrammar();
						}
						
					}, 5000);
				}else{
					//关闭speechService
					SpeechService.this.stopSelf();
				}
			}
			
			@Override
			public void onCompleted() {
				// TODO Auto-generated method stub
				//引擎初始化成功
				Toast.makeText(SpeechService.this, "引擎初始化成功 ", Toast.LENGTH_LONG).show();
				initSpeechEngine();
				
				//初始化成功，设置参数保存
				editor = preferences.edit();
				editor.putBoolean("speech_init", true);
				editor.commit();
			}
		});
        mLocalGrammar.initLocalGrammar();
	}
	
	@Override
	public void onStart(Intent intent, int startId){
		super.onStart(intent, startId);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		/*boolean startAIASR = intent.getBooleanExtra("start", false);
		if(!startAIASR){
        	sendMachineMessage("唤醒成功");
			sendMachineMessage("你好，有什么可以帮您。");
			speakResult("你好，有什么可以帮您。");
			needAIASRAgain = true;
		}else{
			reStartSpeech();
		}*/
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public boolean onUnbind(Intent arg0){
		return super.onUnbind(arg0);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(mWakeupEngine != null)
			mWakeupEngine.destroy();
		if(mAsrEngine != null)
			mAsrEngine.destroy();
		if(weatherReceiver != null)
			unregisterReceiver(weatherReceiver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 初始化TTS语音
	 */
	private void initSpeechTTS(){
        sctts = new SpeechCloudTTS(this);
        sctts.setOnSpeechCloudTTSCompleteListener(new OnSpeechCloudTTSCompleteListener() {
			
			@Override
			public void complete() {
				// TODO Auto-generated method stub
				reStartSpeech();
			}

			@Override
			public void error() {
				// TODO Auto-generated method stub
				reStartSpeech();
			}
		});
        sltts = new SpeechLocalTTS(this);
        sltts.setOnSpeechLocalTTSCompleteListener(new OnSpeechLocalTTSCompleteListener() {
			
			@Override
			public void complete() {
				// TODO Auto-generated method stub
				reStartSpeech();
			}

			@Override
			public void error() {
				// TODO Auto-generated method stub
				reStartSpeech();
			}
		});
	}
	
	//初始化speech本地和云端语音
	private void initSpeechEngine(){
        mWakeupEngine = AILocalWakeupEngine.createInstance();

        mWakeupEngine.setNetBin("net.bin.imy");
        mWakeupEngine.setResBin("res.bin.imy");
        mWakeupEngine.setDBable("i am a partner of aispeech");
        mWakeupEngine.setWakeupRetMode(AIConstant.WAKEUP_RET_MODE_1);
        mWakeupEngine.setStopOnWakeupSuccess(true);
        
        mWakeupEngine.init(this, new AISpeechListenerImpl(), AppKey.APPKEY, AppKey.SECRETKEY);
        mWakeupEngine.setDeviceId("aA-sS_dD");
        mAsrEngine = AIMixASREngine.createInstance();
        mAsrEngine.setDBable("i am a partner of aispeech");
        mAsrEngine.setNetBin("local.net.bin");
        mAsrEngine.setResBin("ebnfr.aicar.0.0.2.bin");
        mAsrEngine.setVadResource("vad.aicar.0.0.3.bin");
        mAsrEngine.setUseCloud(true);
        mAsrEngine.setServer("ws://s.api.aispeech.com");
        mAsrEngine.setRes("chezai");
        mAsrEngine.setWaitCloudTimeout(2000);
        mAsrEngine.setUseConf(true);
        mAsrEngine.setUseXbnfRec(true);
 		
//	        mAsrEngine.setSaveAudioPath("/storage/sdcard0/speech.mp3/");
        // 设置本地置信度阈值
        mAsrEngine.setAthThreshold(0.5f);
        mAsrEngine.setIsRelyOnLocalConf(true);
    	// 自行设置合并规则:
 		// 1. 如果无云端结果,则直接返回本地结果
 		// 2. 如果有云端结果,当本地结果置信度大于阈值时,返回本地结果,否则返回云端结果
 		mAsrEngine.setMergeRule(new IMergeRule() {
 			
             @Override
             public AIResult mergeResult(AIResult localResult, AIResult cloudResult) {
             
                 AIResult result = null;
                 recResult = "";
                 try {
                	// Log.i(TAG, "local: " + localResult + " cloud: " + cloudResult);
                     if (cloudResult == null) {
                         // 为结果增加标记,以标示来源于云端还是本地
                         JSONObject localJsonObject = new JSONObject(localResult.getResultObject()
                                 .toString());
                         JSONObject locRes = localJsonObject.getJSONObject("result");
                         if(locRes != null){
                        	 recResult = locRes.getString("rec");                             
                         }                             
                         localJsonObject.put("src", "native");

                         localResult.setResultObject(localJsonObject);
                         result = localResult;
                     } else {
                    	 int selLocFlag = 0;
                    	 if(localResult != null){
                             JSONObject localJsonObject = new JSONObject(localResult.getResultObject()
                                     .toString());
                             JSONObject locRes = localJsonObject.getJSONObject("result");
                             if(locRes != null){
                            	 recResult = locRes.getString("rec");
                            	 Double confVal = Double.valueOf(locRes.getString("conf"));
                            	// Log.i(TAG, "Conf: " + confVal);
                            	 if(confVal > 0.4){
                            		 selLocFlag = 1;
                            	 }
                             }
                             localJsonObject.put("src", "native");

                             localResult.setResultObject(localJsonObject);
                             result = localResult;                                 
                    	 }                        
                    	 if(selLocFlag != 1){
                             JSONObject cloudJsonObject = new JSONObject(cloudResult.getResultObject()
                                     .toString());
                             JSONObject cloudRes = cloudJsonObject.getJSONObject("result");
                             if(cloudRes != null){
                            	 recResult = cloudRes.getString("input");
                             }
                            	 
                             cloudJsonObject.put("src", "cloud");
                             cloudResult.setResultObject(cloudJsonObject);
                             result = cloudResult;
                    	 }
                     }
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 return result;
            	 
             }
         });
        
        mAsrEngine.init(this, new AIASRListenerImpl(), AppKey.APPKEY, AppKey.SECRETKEY);
        mAsrEngine.setDeviceId("aA-sS_dD");
        startBeep = new BeepPlayer(this, R.raw.open);
        startBeep.setOnCompletionTask(startBeep.new OnCompletionTask(mHandler, MSG_START_ASR));

        mFifoQueue = new CircularFifoQueue<byte[]>(600);	        
        
        mWakeupEngine.start();
        
        initSpeechTTS();

        
        //初始化解析类
        acm = new AnalysisCloudMessage(getApplicationContext());
        anm = new AnalysisNativeMessage(getApplicationContext());
        
        km= (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);  
        kl = km.newKeyguardLock("");   
        pm=(PowerManager) getSystemService(Context.POWER_SERVICE);  
        //wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright"); 
        
        //Toast.makeText(getApplicationContext(), "speech Service is started..", Toast.LENGTH_LONG).show();    
        //音量控制,初始化定义  
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);   
        

        weatherReceiver = new WeatherReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SpeechConfig.weatherMesage);
        filter.addAction(SpeechConfig.iflytekTTSMesage);
        filter.addAction("com.tchip.SLEEP_ON");
        registerReceiver(weatherReceiver, filter);
	}
	

    private static final int MSG_START_ASR = 1;
    Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
            case MSG_START_ASR:
                mAsrEngine.start();
                break;
            default:
                break;
            }
        }
    };
    

    /**
     * 
     * 思必驰唤醒监听
     *
     */
    private class AISpeechListenerImpl implements AILocalWakeupListener {

        @Override
        public void onBeginningOfSpeech() {
        	//检测到开始说话
            sendUIMessage("speech_start");
        }

        @Override
        public void onError(AIError error) {
            //引擎错误，以重新初始化

            /*if(SpeechConfig.SIBICHI_CLOUD_TTS){
            	sctts.cloudTTS("引擎错误，以重新初始化");
            }else{
            	startSpeak("引擎错误，以重新初始化");
            }*/
        	Toast.makeText(SpeechService.this, "引擎错误，以重新初始化", Toast.LENGTH_LONG).show();
            sendUIMessage("speech_end");
            //Toast.makeText(SpeechService.this, "22"+error.getError(), Toast.LENGTH_LONG).show();
            
    		mAsrEngine.stopRecording();
            mWakeupEngine.stop();
        	try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	mWakeupEngine.start();
        }

        @Override
        public void onEndOfSpeech() {
        	//检测到语音停止
            sendUIMessage("speech_end");
        }

        @Override
        public void onInit(int status) {
            Log.i(Tag, "Init result " + status);
            if (status == AIConstant.OPT_SUCCESS) {
                //初始化成功
            } else {
                //初始化失败
            	sendMachineMessage("初始化失败");
            }
            sendUIMessage("speech_end");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // TODO Auto-generated method stub
        	//sendMachineMessage("" + rmsdB);
        }

        @Override
        public void onWakeup(String recordId, int wakeupValue, String wakeupWord, boolean isLast) {
            sendUIMessage("speech_end");
            if (wakeupValue >= 4) {
            	if(!SpeechConfig.speechUIShowing){
            		//当语音助手在后台是启动语音助手，如果screen off 先  亮屏解锁
            		if(!pm.isScreenOn()){ 
            	        //解锁  
            	        kl.disableKeyguard();  
            		}
            		SpeechConfig.speechUIShowing = true;
            		Intent intent = new Intent(SpeechService.this, WakeUpCloudAsr.class);
            		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            		intent.putExtra("start", true);
            		startActivity(intent);
            	}
                //点亮屏幕  
                //wl.acquire();  
                //释放  
                //wl.release(); 
                
            	sendMachineMessage("唤醒成功");
            	
                startBeep.playBeep();
                mWakeupEngine.stop();
                
                try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
                //mAsrEngine.start();            	
            } else {
                //唤醒失败
            	sendMachineMessage("唤醒失败");
            }
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            mFifoQueue.add(buffer);
        }

        @Override
        public void onReadyForSpeech() {
            //说小智以唤醒
        	sendMachineMessage("说小智以唤醒");
            mFifoQueue.clear();
        }

        @Override
        public void onRecorderReleased() {
            // TODO Auto-generated method stub
            sendUIMessage("speech_end");
        }

    }

    /**
     * 
     * 思必驰语义识别监听
     *
     */
    private class AIASRListenerImpl implements AIASRListener {

        @Override
        public void onBeginningOfSpeech() {
            sendUIMessage("speech_start");
        }

        @Override
        public void onEndOfSpeech() {
            // TODO Auto-generated method stub
            sendUIMessage("speech_end_user");
        }

        @Override
        public void onError(AIError error) {
            //引擎错误，以重新初始化
        	//Toast.makeText(SpeechService.this, "11"+error.getError(), Toast.LENGTH_LONG).show();
        	//reStartSpeech();

    		mAsrEngine.stopRecording();
            mWakeupEngine.stop();
        	try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	mAsrEngine.start();
        }

        @Override
        public void onInit(int arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onReadyForSpeech() {
            //可以开始说话了
            sendUIMessage("speech_end");
        }

        @Override
        public void onResults(AIResult result) {
        	//语义识别监听收到结果后，先engine stopRecording
        	mAsrEngine.stopRecording();
            Log.d(Tag, "onReuslt:" + result.getResultObject().toString());
            JSONObject object;
            try {
                object = new JSONObject(result.getResultObject().toString());
                String mResult = object.toString(4);
                //sendUserMessage(mResult);
                String data = analysisSpeechMessage(mResult);
                aiAsrResult(data);
                
                //mWakeupEngine.start();
                //Log.i("WakeupEngine", "唤醒引擎开启了");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                reStartSpeech();
            }

            sendUIMessage("speech_end");
        }

        @Override
        public void onRmsChanged(float arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onRecorderReleased() {
            // TODO Auto-generated method stub
        }

    }
    
    /*
     * 启动语音监听
     * 界面显示，不启动语音唤醒，启动语音监听
     * 不现实界面，启动语音唤醒
     */
    private void reStartSpeech(){
		if(mAsrEngine == null && mWakeupEngine == null)
			return;
    	if(needAIASRAgain && (aiAsrIndex < 2)){
    		aiAsrIndex ++;
            mWakeupEngine.stop();
    		mAsrEngine.stopRecording();
        	try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		mAsrEngine.start();
    	}else{
    		aiAsrIndex = 0;
    		mAsrEngine.stopRecording();
        	try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            mWakeupEngine.start();
    	}
    }
    
    /**
     * 处理语音识别返回的结果
     * @param data
     * @return
     */
    private boolean aiAsrResult(String data){
    	needAIASRAgain = false;

        if(data == null){
        	//不对语言进行识别, 重新语音启动识别
        	//reStartSpeech();
            sendMachineMessage("您没有说话");
        	speakResult("您没有说话");
        } else {
        	if(data.equals(SpeechConfig.screenOff)){
        		//发送power按键模拟，关闭屏幕
        		sendBroadcast(new Intent("com.tchip.powerKey").putExtra("value", "power"));
                sendMachineMessage(SpeechConfig.screenOffing);
                
                speakResult(SpeechConfig.screenOffing);
        	}
        	
        	//音量调节
        	else if(data.equals(SpeechConfig.upVolume)){
        		//发送volume up按键模拟，增大音量
        		//sendBroadcast(new Intent("com.tchip.powerKey").putExtra("value", "volume_up"));
        		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC , false);
        		mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,    
                        AudioManager.FX_FOCUS_NAVIGATION_UP);   
                reStartSpeech();
        	}else if(data.equals(SpeechConfig.downVolume)){
        		//发送volume down按键模拟，减小音量
        		//sendBroadcast(new Intent("com.tchip.powerKey").putExtra("value", "volume_down"));
        		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC , false);
        		mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,    
                        AudioManager.FX_FOCUS_NAVIGATION_UP);  
                reStartSpeech();
        	}else if(data.equals(SpeechConfig.muteOnVolume)){
        		//静音
        		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC , true);
    			sendMachineMessage("已静音");
                speakResult("已静音");
        	}else if(data.equals(SpeechConfig.minVolume)){
        		//最小音量
        		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC , false);
        		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0); 
    			sendMachineMessage("音量已最小");
                speakResult("音量已最小");
        	}else if(data.equals(SpeechConfig.maxVolume)){
        		//最大音量
        		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC , false);
        		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 15, 0); 
    			sendMachineMessage("音量已最大");
                speakResult("音量已最大");
        	}
        	
        	//亮度调节
        	else if(data.equals(SpeechConfig.upBrightness)){
        		//增大亮度
        		try {
					int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) + 10;
					if(brightness > 255) brightness = 255;
	        		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness); 
				} catch (SettingNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			sendMachineMessage("亮度已增大");
                speakResult("亮度已增大");
        	}else if(data.equals(SpeechConfig.downBrightness)){
        		//减小亮度 
        		try {
					int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) - 10;
					if(brightness < 0) brightness = 0;
	        		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness); 
				} catch (SettingNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			sendMachineMessage("亮度已减小");
                speakResult("亮度已减小");
        	}else if(data.equals(SpeechConfig.minBrightness)){
        		//最小亮度
        		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0); 
    			sendMachineMessage("亮度已最小");
                speakResult("亮度已最小");
        	}else if(data.equals(SpeechConfig.maxBrightness)){
        		//最大亮度
        		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255); 
    			sendMachineMessage("亮度已最大");
                speakResult("亮度已最大");
        	}else if(SpeechConfig.weather.equals(data)){
        		//天气查询，要等待讯飞接口的返回
        		//暂时什么也不做，等待消息返回后重新启动识别
        		
        	}/*else if(SpeechConfig.mapNav.equals(data)){
        		//导航
        		Toast.makeText(SpeechService.this, "导航", Toast.LENGTH_LONG).show();

    			sendMachineMessage(data);
                //if(SpeechConfig.SIBICHI_CLOUD_TTS){
                //	sctts.cloudTTS(data);
                //}else{
                //	startSpeak(data, false);
                //}
        		mAsrEngine.start();
        	}else if(SpeechConfig.mapNavChoice.equals(data)){
        		//导航
    			//sendMachineMessage(data);
//                reStartSpeech();
        	}*/
        	else if(SpeechConfig.goToMusic.equals(data)){
        		//打开酷我，酷狗，不播报tts
        		reStartSpeech();
        	}else{
        		//其他操作
        		if(data.equals(SpeechConfig.unKnown)){
        			sendMachineMessage(data);
        			speakResult(data);
                    //Toast.makeText(SpeechService.this, "result :" + data, Toast.LENGTH_SHORT).show();
                	needAIASRAgain = true;
        		}else if(data.length() > 0){ 
        			//当字符串为""时不显示,不朗读
        			sendMachineMessage(data);
        			speakResult(data);
        		}else{
                    sendMachineMessage("无效指令");
                	speakResult("无效指令");
                	needAIASRAgain = true;
        		}
        	}
        	
        }
        return false;
    }
    
    private boolean needAIASRAgain = false;
    private int aiAsrIndex = 0;
    
    /**
     * tts朗读
     * @param result
     */
    private void speakResult(String result){

        if(SpeechConfig.SIBICHI_CLOUD_TTS){
        	sctts.cloudTTS(result);
        }else{
        	startSpeak(result);
        }
    }
    
    
    /**
     * 启动讯飞语音本地TTS
     * @param msg
     */
    private void startSpeak(String msg){
    	startSpeak(msg, true);
    }
	private void startSpeak(String msg, boolean back) {
		Intent intent = new Intent(this, SpeakService.class);
		intent.putExtra("content", msg);
		intent.putExtra("back", back);
		startService(intent);
	}
    
    /*
     * service 发送机器消息
     */
    private void sendMachineMessage(String msg){
    	Intent intent = new Intent(SpeechConfig.machineMessage);
    	intent.putExtra("value", msg);
    	sendBroadcast(intent);
    }
    /*
     * service 发送用户消息
     */
    private void sendUserMessage(String msg){
    	Intent intent = new Intent(SpeechConfig.userMesage);
    	intent.putExtra("value", msg);
    	sendBroadcast(intent);
    }
    /*
     * service 发送界面消息
     */
    private void sendUIMessage(String msg){
    	Intent intent = new Intent(SpeechConfig.uiMesage);
    	intent.putExtra("value", msg);
    	sendBroadcast(intent);
    }
    
    
    /*
     * 返回语音数据处理
     * @param msg
     */
    AnalysisCloudMessage acm;
    AnalysisNativeMessage anm;
    private String analysisSpeechMessage(String msg){
    	if (msg == null)
    		return null;
    	
    	if (msg.contains("cloud")) {
    		return acm.analysis(msg);
    	} else if (msg.contains("native")) {
    		return anm.analysis(msg);
    	} else {
    		return null;
    	}
    }
}

