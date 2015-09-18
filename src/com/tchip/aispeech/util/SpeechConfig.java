package com.tchip.aispeech.util;


/**
 * 
 * 配置语音识别参数
 * @author wwj
 *
 */
public class SpeechConfig {

	/**
	 * 讯飞语音SDK
	 */
	public static final String XUNFEI_APP_ID = "55e55e42";
	
	/**
	 * 思必驰云TTS
	 */
	public static boolean SIBICHI_CLOUD_TTS = false;
	
	//存储文件
	public static final String SHARED_PREFERENCES_SPEECH_NAME = "speech";
	//在线音乐是不是酷我音乐盒
	public static final boolean isOnlineMusicKuwo = true;
	
	/*
	 * 设置tts声音  
	 * syn_chnsnt_zhilingf
	 * syn_chnsnt_anonyf 女播音员
	 * syn_chnsnt_anonyg 小女生
	 * syn_chnsnt_anonym 男播音员
	 * syn_chnsnt_xyshenf 女客服  声音有点小
	 */
	public static String ttsSound = "syn_chnsnt_anonyg";
	
	//思必驰语音界面是否在前台显示
	public static boolean speechUIShowing = false;
	//语音服务器和界面传递消息的广播
	public static String machineMessage = "com.tchip.speechMachineMessage";
	public static String userMesage = "com.tchip.speechUserMessage";
	public static String uiMesage = "com.tchip.speechUIMessage";

	/**
	 * 语音要接收的消息广播
	 */
	//天气
	public static String weatherMesage = "com.tchip.speechWeatherMessage";
	//讯飞tts播报完毕
	public static String iflytekTTSMesage = "com.tchip.iflyTTSCompletedMessage";
	
	
	
	//本地语音关键词
    public static String app = "app";
    public static String baiduMap = "百度地图";
    public static String kuwoMusic = "酷我音乐盒";
    public static String kugouMusic = "酷狗音乐";
    public static String music = "music";
    public static String musicAction[] ={"random", "resume", "play"}; 
    public static String phone = "phone";
    public static String number = "number";
    public static String person = "person";
    public static String t_chip = "t-chip";
    public static String screenOff = "关闭屏幕关屏灭屏熄屏";
    public static String screenOffing = "屏幕已关闭";
    public static String goFailed = "打开失败";
    public static String goCarLauncher = "返回桌面回到桌面打开桌面";
    public static String goCarLaunchering = "以返回桌面";
    public static String goSettings = "打开设置";
    public static String goSettingsing = "以打开设置";
    public static String goFM = "打开FM";
    public static String goFMing = "以打开FM";
    public static String goMap = "打开导航打开地图";
    public static String goMaping = "以打开地图";
    public static String goMusic = "打开音乐";
    public static String goBack = "返回";
    public static String closeMusic = "关闭音乐";
    public static String closeMap = "关闭地图";
    public static String closeNav = "关闭导航";
    public static String closeMusicing = "正在关闭音乐";
    public static String closeMaping = "正在关闭地图";
    public static String closeNaving = "正在关闭导航";
    
    public static String goToMusic = "正在打开音乐盒";
    public static String unKnown = "我听不懂你说什么！";

    //音量
    public static String volume = "volume";
    public static String upVolume = "增大音量";
    public static String downVolume = "减小音量";
    public static String muteOnVolume = "静音";
    public static String minVolume = "最小音量";
    public static String maxVolume = "最大音量";

    //亮度
    public static String brightness = "brightness";
    public static String upBrightness = "增大亮度";
    public static String downBrightness = "减小亮度";
    public static String minBrightness = "最小亮度";
    public static String maxBrightness = "最大亮度";

	//云端语音关键词
    public static String calendar = "日历";
    public static String time = "时间";
    public static String map = "地图";
    public static String mapNav = "正在启动导航";
    public static String mapNavChoice = "导航目的地选择";
    public static String weather = "天气";
    
    //云端日常用语
    public static String hello = "你好";
    public static String helloAck = "你好，很高兴为你服务。";
    public static String me = "我";
    public static String userDo = "说什么"; //我可以说什么
    public static String userDoAck = "比如您可以说：\n 导航到深圳北站; \n 打开百度地图，打开设置; \n 北京天气怎么样; \n 关闭屏幕、返回桌面；\n增大、减小、最大、最小音量;\n增大、减小、最大、最小亮度 \n 当然您也可以唤醒在后台的我哦。";
}