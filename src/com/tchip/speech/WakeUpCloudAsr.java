package com.tchip.speech;

import java.util.prefs.Preferences;

import android.animation.Animator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tchip.aispeech.util.SpeechConfig;
import com.tchip.carlauncher.util.NetworkUtil;
import com.tchip.carlauncher.util.ProgressAnimationUtil;
import com.tchip.carlauncher.view.CircularProgressDrawable;
import com.tchip.carlauncher.view.ResideMenu;
import com.tchip.iflytek.SpeakService;

/**
 * 
 * 语音界面
 * @author wwj
 *
 */
public class WakeUpCloudAsr extends Activity implements View.OnClickListener {

    final String Tag = this.getClass().getName();
    
    LinearLayout chartMsgPanel;
    ScrollView chartMsgScroll;
	private LayoutInflater inflater;
	private ImageView imageAnim;
	private ImageView imageVoice;
	private Animator currentAnimation;
	private CircularProgressDrawable drawable;


	private RelativeLayout layoutBack; // 返回
	private Button btnToMultimedia;
	private LinearLayout layoutHelp; // 帮助
	private Button btnHelp;
	private LinearLayout linear;

	// 左侧帮助侧边栏
	private ResideMenu resideMenu;
	/*
	 * 接受speech service发送的消息广播
	 */
    public class MessageReceiver extends BroadcastReceiver {    	
        @Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
        	if (SpeechConfig.machineMessage.equals(action)){
        		//机器消息
        		machineQuery(intent.getStringExtra("value"));
        	} else if (SpeechConfig.userMesage.equals(action)) {
        		//用户语音识别
        		userQuery(intent.getStringExtra("value"));
        	} else if (SpeechConfig.uiMesage.equals(action)){
        		//界面UI更新
        		if(currentAnimation != null)
        			currentAnimation.cancel();
        		String value = intent.getStringExtra("value");
        		if("speech_start".equals(value)) {
        			imageVoice.setVisibility(View.VISIBLE);
        			imageAnim.setVisibility(View.INVISIBLE);
        			imageVoice.setImageResource(R.drawable.speech_voice);
        		} else if ("speech_end_user".equals(value)) {
        			imageVoice.setVisibility(View.INVISIBLE);
        			imageAnim.setVisibility(View.VISIBLE);
        			currentAnimation = ProgressAnimationUtil.prepareStyle1Animation(drawable);
        			currentAnimation.start();
        		} else if ("speech_end".equals(value)) {
        			imageVoice.setVisibility(View.VISIBLE);
        			imageAnim.setVisibility(View.INVISIBLE);
        			imageVoice.setImageResource(R.drawable.speech_voice_default);
        		}
        	}
        }
    }
    MessageReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asr);
        
        inflater = getLayoutInflater();
		chartMsgPanel = (LinearLayout) findViewById(R.id.chart_msg_panel);
		chartMsgScroll = (ScrollView) findViewById(R.id.chart_msg_scroll);
		
		imageAnim = (ImageView) findViewById(R.id.imageAnim);
		imageVoice = (ImageView) findViewById(R.id.imageVoice);
		//imageAnim.setOnClickListener(new MyOnClickListener());
		drawable = new CircularProgressDrawable.Builder()
				.setRingWidth(
						getResources().getDimensionPixelSize(
								R.dimen.drawable_ring_size))
				.setOutlineColor(getResources().getColor(R.color.white))
				.setRingColor(
						getResources().getColor(R.color.ui_chat_voice_orange))
				.setCenterColor(
						getResources().getColor(R.color.ui_chat_voice_orange))
				.create();
		imageAnim.setImageDrawable(drawable);
		imageAnim.setVisibility(View.INVISIBLE);
		//currentAnimation = ProgressAnimationUtil.prepareStyle1Animation(drawable);
		//currentAnimation.start();

        receiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SpeechConfig.machineMessage);
        filter.addAction(SpeechConfig.userMesage);
        filter.addAction(SpeechConfig.uiMesage);
        registerReceiver(receiver, filter);


		// 返回
		layoutBack = (RelativeLayout) findViewById(R.id.layoutBack);
		btnToMultimedia = (Button) findViewById(R.id.btnToMultimedia);
		layoutBack.setOnClickListener(this);
		btnToMultimedia.setOnClickListener(this);

		// 帮助侧边栏
		layoutHelp = (LinearLayout) findViewById(R.id.layoutHelp);
		btnHelp = (Button) findViewById(R.id.btnHelp);
		layoutHelp.setOnClickListener(this);
		btnHelp.setOnClickListener(this);
		
		linear = (LinearLayout) findViewById(R.id.linear);
		linear.setOnClickListener(this);

		// attach to current activity;
		resideMenu = new ResideMenu(this);
		resideMenu.setBackground(R.color.grey_dark_light);
		resideMenu.attachToActivity(this);
		resideMenu.setMenuListener(menuListener);
		// valid scale factor is between 0.0f and 1.0f. leftmenu'width is
		// 150dip.
		resideMenu.setScaleValue(0.6f);
		// 禁止使用右侧菜单
		resideMenu.setDirectionDisable(ResideMenu.DIRECTION_RIGHT);		
		

        pm=(PowerManager) getSystemService(Context.POWER_SERVICE);  
        wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright"); 
    }
	private PowerManager pm;
	private PowerManager.WakeLock wl;
    
    public void onResume(){
    	super.onResume();
    	SpeechConfig.speechUIShowing = true;
    	wl.acquire();
		
		//第一次初始化不联网，推出
		SharedPreferences preferences = getSharedPreferences(SpeechConfig.SHARED_PREFERENCES_SPEECH_NAME, Context.MODE_PRIVATE);
		if(!preferences.getBoolean("speech_init", false) && !NetworkUtil.isNetworkConnected(getApplicationContext())){
			Toast.makeText(WakeUpCloudAsr.this, "引擎初始化需要连接网络！ ", Toast.LENGTH_LONG).show();
				//WakeUpCloudAsr.this.finish();
			startSpeak("引擎初始化需要连接网络！ ", false);
		}else{
	    	Intent intent = new Intent(this, SpeechService.class);
	    	intent.putExtra("start", getIntent().getBooleanExtra("start", false));
	    	//Toast.makeText(WakeUpCloudAsr.this, "" + getIntent().getBooleanExtra("start", false), Toast.LENGTH_LONG).show();
			startService(intent);
		}
    }
    
    public void onPause(){
    	super.onPause();
    	wl.release();
    	wl.acquire(15000);
    	SpeechConfig.speechUIShowing = false;
    	//Intent intent = new Intent(this, SpeechService.class);
		//startService(intent);
    	
    }
    
    /*
     * 播放语音
     */
	private void startSpeak(String msg, boolean back) {
		Intent intent = new Intent(this, SpeakService.class);
		intent.putExtra("content", msg);
		intent.putExtra("back", back);
		startService(intent);
	}

	/**
	 * 侧边栏打开关闭监听
	 */
	private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
		@Override
		public void openMenu() {
			//isResideMenuClose = false;
			layoutHelp.setVisibility(View.GONE);
		}

		@Override
		public void closeMenu() {
			//isResideMenuClose = true;
			layoutHelp.setVisibility(View.VISIBLE);
		}
	};
	
    /**
	 * 用户发言
	 * @param query
	 */
	public void userQuery(String query)
	{
		View view = inflater.inflate(R.layout.send_msg_layout, null);
		TextView smcView = (TextView)view.findViewById(R.id.send_msg_content);
		smcView.setText(query);
		chartMsgPanel.addView(view);
		scrollHandler.post(scrollRunnable);
	}
	/**
	 * 机器发言
	 * @param query
	 */
	public void machineQuery(String query)
	{
		View view = inflater.inflate(R.layout.received_msg_layout, null);
		TextView smcView = (TextView)view.findViewById(R.id.received_msg_content);
		smcView.setText(query);
		chartMsgPanel.addView(view);
		scrollHandler.post(scrollRunnable);
	}
	// 滚动屏幕
	private final Handler scrollHandler = new Handler();
    private Runnable scrollRunnable= new Runnable() {
	    @Override
	    public void run() {
	    	chartMsgScroll.fullScroll(View.FOCUS_DOWN);   //滚动到底部
	    }
    };

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		
			case R.id.layoutBack:
			case R.id.btnToMultimedia:
				//返回
				WakeUpCloudAsr.this.finish();
				break;
			case R.id.layoutHelp:
			case R.id.btnHelp:
			case R.id.linear:
				//显示menu
				resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
				break;
			default:
				break;
		}
	}
}