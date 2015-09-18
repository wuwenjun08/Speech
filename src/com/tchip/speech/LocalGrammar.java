/*******************************************************************************
 * Copyright 2014 AISpeech
 ******************************************************************************/
package com.tchip.speech;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aispeech.AIError;
import com.aispeech.AIResult;
import com.aispeech.IMergeRule;
import com.aispeech.common.Util;
import com.aispeech.export.engines.AILocalGrammarEngine;
import com.aispeech.export.engines.AIMixASREngine;
import com.aispeech.export.listeners.AIASRListener;
import com.aispeech.export.listeners.AILocalGrammarListener;
import com.tchip.aispeech.util.GrammarHelper;
import com.tchip.aispeech.util.NetworkUtil;

/**
 * 本示例将演示通过联合使用本地识别引擎和本地语法编译引擎实现定制识别。<br>
 * 将由本地语法编译引擎根据手机中的联系人和应用列表编译出可供本地识别引擎使用的资源，从而达到离线定制识别的功能。
 * 
 */
public class LocalGrammar{
    public static final String TAG = LocalGrammar.class.getName();
    Context context;
    Toast mToast;
    AILocalGrammarEngine mGrammarEngine;
    AIMixASREngine mAsrEngine;
    String recResult;
    
    /*
     * 编译引擎初始化结果回掉
     */
    public interface OnLocalGrammarInitListener{
    	void onExist(); //引擎存在，不需要初始化
    	void onError(); //引擎初始化错误
    	void onCompleted(); //引擎初始化成功
    }
    OnLocalGrammarInitListener listener;
    public void setOnLocalGrammarInitListener(OnLocalGrammarInitListener listener){
    	this.listener = listener;
    }
    
    
    public LocalGrammar(Context context) {
        this.context = context;
    }
    
    /**
     * init
     */
    public void initLocalGrammar(){
        // 检测是否已生成并存在识别资源，若已存在，则立即初始化本地识别引擎，否则等待编译生成资源文件后加载本地识别引擎
        if (new File(Util.getResourceDir(context) + File.separator + AILocalGrammarEngine.OUTPUT_NAME).exists()) {
        	listener.onExist();
        }else{
            initGrammarEngine();
        }
        
        startResGen();
    }
    

    /**
     * 初始化资源编译引擎
     */
    private void initGrammarEngine() {
        if (mGrammarEngine != null) {
            mGrammarEngine.destroy();
        }
        Log.i(TAG, "grammar create");
        mGrammarEngine = AILocalGrammarEngine.createInstance();
        mGrammarEngine.setResFileName("ebnfc.aicar.0.0.2.bin");
        mGrammarEngine.init(context, new AILocalGrammarListenerImpl(), AppKey.APPKEY, AppKey.SECRETKEY);
        mGrammarEngine.setDeviceId("aA-sS_dD");
    }
    

    /**
     * 开始生成识别资源
     */
    
    private void startResGen() {
        if (mGrammarEngine == null) {
            mGrammarEngine = AILocalGrammarEngine.createInstance();
            mGrammarEngine.setResFileName("ebnfc.aicar.0.0.2.bin");
            //mGrammarEngine.init(context, new AILocalGrammarListenerImpl(), AppKey.APPKEY, AppKey.SECRETKEY);
            mGrammarEngine.setDeviceId("aA-sS_dD");
        }
        // 生成ebnf语法
        GrammarHelper gh = new GrammarHelper(context);
        String contactString = gh.getConatcts();
        String appString = gh.getApps();
        String musicString = gh.getMusicTitles();
        // 如果手机通讯录没有联系人
        if (TextUtils.isEmpty(contactString)) {
            contactString = "无联系人";
        }
        String ebnf = gh.importAssets(contactString,musicString, appString, "grammar.xbnf");
        Log.i(TAG, ebnf);
        // 设置ebnf语法
        mGrammarEngine.setEbnf(ebnf);
        // 启动语法编译引擎，更新资源
        mGrammarEngine.update();
    }

    /**
     * 语法编译引擎回调接口，用以接收相关事件
     */
    public class AILocalGrammarListenerImpl implements AILocalGrammarListener {

        @Override
        public void onError(AIError error) {
            listener.onError();
        }

        @Override
        public void onUpdateCompleted(String recordId, String path) {
            Log.i(TAG, "资源生成/更新成功\npath=" + path + "\n重新加载识别引擎...");
            File file = new File(path);
            byte[] buffer = new byte[10240];
            int i = 0;
            try {
                FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(new File("/sdcard/gram.net.bin"));
                while((i = fis.read(buffer)) > 0){
                    fos.write(buffer);
                }
                fis.close();
                fos.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            listener.onCompleted();
        }

        @Override
        public void onInit(int status) {
            if (status == 0) {
                //资源定制引擎加载成功
            } else {
                //资源定制引擎加载失败
                listener.onError();
            }
        }
    }

    public void pause() {
        if (mGrammarEngine != null) {
            Log.i(TAG, "grammar cancel");
            mGrammarEngine.cancel();
        }
        if (mAsrEngine != null) {
            Log.i(TAG, "asr cancel");
            mAsrEngine.cancel();
        }
    }

    public void destroy() {
        if (mGrammarEngine != null) {
            Log.i(TAG, "grammar destroy");
            mGrammarEngine.destroy();
            mGrammarEngine = null;
        }
        if (mAsrEngine != null) {
            Log.i(TAG, "asr destroy");
            mAsrEngine.destroy();
            mAsrEngine = null;
        }
    }
}