package com.tchip.speech;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.TextUnderstander;
import com.iflytek.cloud.TextUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;
import com.tchip.aispeech.util.SpeechConfig;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * 
 * 用来给思必驰获取天气
 * @author wwj
 *
 */
public class WeatherInfo{
	private TextUnderstander mTextUnderstander;
	private Intent intent;
	private Context context;
	private String date;
	private String city;
	
	public WeatherInfo(Context context){
		this.context = context;
		mTextUnderstander = TextUnderstander.createTextUnderstander(context, textUnderstanderListener);

		intent = new Intent(SpeechConfig.weatherMesage);
	}
	

	private InitListener textUnderstanderListener = new InitListener() {

		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
				// 初始化失败,错误码：" + code
				//editor.putString("exception", "初始化失败,错误码：" + code);
			}
		}
	};

	private TextUnderstanderListener textListener = new TextUnderstanderListener() {

		@Override
		public void onResult(final UnderstanderResult result) {

			if (null != result) {
				// 获取结果
				String jsonString = result.getResultString();
				if (!TextUtils.isEmpty(jsonString)) {
					JSONObject jsonObject;
					try {
						jsonObject = new JSONObject(jsonString);
						JSONArray mJSONArray = jsonObject.getJSONObject("data")
								.getJSONArray("result");
						for (int i = 0; i < 7; i++) {
							JSONObject jsonDay = mJSONArray
									.getJSONObject(i + 1); // 跳过天气数组第一个数据
							String dateStr = jsonDay.getString("date");
							if(dateStr != null){
								dateStr = dateStr.replace("-", "");
								if(dateStr.equals(date)){
									String weatherStr = jsonDay.getString("weather");
									String windStr = jsonDay.getString("wind");
									if ("无持续风向微风".equals(windStr))
										windStr = "微风";
									
									String tempRange = jsonDay.getString("tempRange"); // 31℃~26℃
									String tempArray[] = tempRange.split("~");
									String highTempStr = "最高气温" + tempArray[0];
									String lowTempStr = "最低气温" + tempArray[1];
									
									intent.putExtra("value", city + "，" + weatherStr + "，" + windStr + "，" + highTempStr + "，" + lowTempStr);
									context.sendBroadcast(intent);
									return;
								}
							}
							/*
							String tempRange = jsonDay.getString("tempRange"); // 31℃~26℃
							String tempArray[] = tempRange.split("~");editor.putString("postTime",
									jsonDay.getString("lastUpdateTime"));

							editor.putString("day" + i + "weather",
									jsonDay.getString("weather"));
							editor.putString("day" + i + "tmpHigh",
									tempArray[0]);
							editor.putString("day" + i + "tmpLow", tempArray[1]);
							if (i == 0) {
								editor.putString("humidity",
										jsonDay.getString("humidity"));
								editor.putString("airQuality",
										jsonDay.getString("airQuality"));
							}

							String windDirection = jsonDay.getString("wind");
							if ("无持续风向微风".equals(windDirection))
								windDirection = "微风";
							editor.putString("day" + i + "wind", windDirection);
							// + jsonDay.getString("windLevel")
							editor.putString("day" + i + "date",
									jsonDay.getString("date"));

							editor.commit();*/
						}
					} catch (JSONException e) {
						e.printStackTrace();
					} 
				}
			}
			intent.putExtra("value", "获取天气错误");
			context.sendBroadcast(intent);
		}

		@Override
		public void onError(SpeechError error) {
			intent.putExtra("value", "获取天气错误");
			context.sendBroadcast(intent);
		}
	};

	public void getWeather(String cityStr, String date) {
		int ret = 0;// 函数调用返回值
		this.date = date;
		this.city = cityStr;

		String text = cityStr + "天气";

		if (mTextUnderstander.isUnderstanding()) {
			mTextUnderstander.cancel();
			// showTip("取消");
		}
		
		ret = mTextUnderstander.understandText(text, textListener);
		if (ret != 0) {
			// showTip("语义理解失败,错误码:" + ret);
			intent.putExtra("value", "获取天气错误");
			context.sendBroadcast(intent);
		}
	}

}
