package cn.com.zhenshiyin.crowd.common;

public class Preferences {
	//是否第一次进入应用
	public static final String IS_FIRST_TYPE_TAG = "is_first_type_tag";
	public static final String IS_FIRST_KEY_TAG = "is_first_key_tag";
	
	//PUSH TOKEN值
	public static final String PUSH_TOKEN_VALUE  = "push_token_value";
	public static final String PUSH_TOKEN_KEY  = "push_token_key";
	//是否推送
	public static final String IS_PUSH_TYPE_TAG = "is_push_type_tag";
	public static final String IS_PUSH_KEY_TAG = "is_push_key_tag";
	//推送时段
	public static final String PUSH_TIME_TYPE_TAG = "push_time_type_tag";
	public static final String PUSH_TIME_KEY_TAG = "push_time_key_tag";
	
	//上一次坐标
	public static final String COORDINATE_TYPE_TAG = "coordinate_type_tag";
	public static final String COORDINATE_KEY_TAG = "coordinate_key_tag";
	//当前坐标
	public static final String CURRENT_COORDINATE_TYPE_TAG = "current_coordinate_type_tag";
	public static final String CURRENT_COORDINATE_KEY_TAG = "current_coordinate_key_tag";
	
	
	/////旅游
	
	
	//版本更新
	/* 最后的版本号 */
	public static final String KEY_LATEST_VERSION = "key_latest_version";
	public static final String KEY_APK_DOWNLOAD_URL = "key_apk_download_url";
	/* 最新版本是否安装 */
	public static final String KEY_LATEST_VERSION_INSTALL = "key_latest_version_install";
	public static final String KEY_HELP_VERSION_SHOWN = "key_help_version_shown";
	
	//首次启动
	public static final String FIRST_START_TYPE_TAG = "first_start_type_tag"; 
	public static final String FIRST_START_KEY_TAG = "first_start_key_tag"; 
	
	//登录信息
	public static final String LOGIN_TYPE_TAG = "loginUser_type_tag";
	public static final String LOGIN_KEY_TAG = "memberId_key_tag";
	
	//收货手机号缓存preference
	public static final String RECIPIENT_PHONE_NUMBER  = "recipient_phone_number";
}
