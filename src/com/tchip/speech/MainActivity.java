package com.tchip.speech;

import com.tchip.speech.SpeechCloudASRCar.OnSpeechCloudASRCarListenering;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	//final static String CN_PREVIEW = "路径规划成功，导航开始，前方300米右转至新平街，随后30米左转，前方430米有违章摄像头， 请保持直行，前方300米至仁爱路，右转65米，到达目的地苏州独墅湖图书馆，导航结束。";
	final static String CN_PREVIEW = "大家好，我叫林志玲，请大家支持我哦。";
	SpeechAuth sa;
	SpeechCloudTTS sctts;
	SpeechCloudASRCar scasrc;

	Button speak;
	TextView resultTV;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sa = new SpeechAuth(this);
		sa.auth();
		sctts = new SpeechCloudTTS(this);
		


		scasrc = new SpeechCloudASRCar(MainActivity.this);
		scasrc.setOnSpeechCloudASRCarListenering(new OnSpeechCloudASRCarListenering() {
			
			@Override
			public void result(String result) {
				// TODO Auto-generated method stub
				resultTV.setText(result);
			}
		});
		
		resultTV = (TextView) findViewById(R.id.result);
		speak = (Button) findViewById(R.id.speak);		
		speak.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//sctts.cloudTTS(CN_PREVIEW);
				scasrc.startSpeechListen();
				//Log.d("wwj_test", "cloudTTS " + se.cloudTTS(CN_PREVIEW));
			}
		});
		Intent intent = new Intent(this, SpeechService.class);
		startService(intent);
	}	

}
