package com.tchip.speech;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

/**
 * 
 * 语音识别时在界面显示dialog
 * @author wwj
 *
 */
public class SpeechCASRDialog extends Dialog {

    private Context context;
    private LinearLayout speechListening;
    private ImageView speechColume;
    private ProgressBar speechPB;

    public interface ClickListenerInterface {

        public void doConfirm();

        public void doCancel();
    }

    public SpeechCASRDialog(Context context) {
        super(context, R.style.MyVolumeDialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        init();
    }

    /**
     * 初始化dialog界面
     */
    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.speech_casr_dialog, null);
        setContentView(view);
        
        speechListening = (LinearLayout) view.findViewById(R.id.speech_listening);
        speechColume = (ImageView) findViewById(R.id.speech_volume);
        speechPB = (ProgressBar) view.findViewById(R.id.result_progress);

        //Window dialogWindow = getWindow();
        //WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        //DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        //lp.width = (int) (d.widthPixels * 0.8); // 高度设置为屏幕的0.6
        //dialogWindow.setAttributes(lp);
    }
    
    /**
     * 有语音输入界面显示状态
     */
    public void speechListening(){
    	speechColume.setImageResource(R.drawable.icon_volume);
    	speechListening.setVisibility(View.VISIBLE);
    	speechPB.setVisibility(View.INVISIBLE);
    }
    
    /**
     * 无语音输入界面显示状态
     */
    public void speechListen(){
    	speechColume.setImageResource(R.drawable.icon_volume_1);
    	speechListening.setVisibility(View.VISIBLE);
    	speechPB.setVisibility(View.INVISIBLE);
    }
    
    /**
     * 语音识别界面显示状态
     */
    public void speechCASRing(){
    	speechListening.setVisibility(View.INVISIBLE);
    	speechPB.setVisibility(View.VISIBLE);
    }
}