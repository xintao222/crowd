package cn.com.zhenshiyin.crowd.common;

import java.util.HashMap;
import java.util.List;

import cn.com.zhenshiyin.crowd.net.utils.Utils;
import cn.com.zhenshiyin.crowd.net.utils.RequestParameter;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

public class Constants {
	
	//客户端版本相关
	public static String HWID = "";
	public static final String SERVICE_CODE = "0";
	public static final String CHECK_UPDATE = "";
	
	//地图相关 TODO:换成我们自己的KEY?
	public static final String MAP_KEY = "11C32DD70BE81FA45B835D7FED716C6EE6D3C43C";
	
	//应用相关
	public static boolean isGzip = false;//是否启用gzip
	public static boolean IS_STOP_REQUEST = false;//请求线程是否停止
	
	// Network related.
	public static final String HTTP_POST = "POST";
	public static final String HTTP_GET = "GET";
	
	public static final int REQUEST_INTRODUCTION = 100;
	public static final int REQUEST_FOCUS = 101;
	public static final int REQUEST_DETAIL = 102;
	public static final int REQUEST_CONCLUSTION = 103;
	public static final int REQUEST_INDEX = 104;
	public static final int REQUEST_RAIDER = 105;
	
	public static final int BOOK_CATEGORY_INTRODUCTION = 1;
	public static final int BOOK_CATEGORY_FOCUS = 2;
	public static final int BOOK_CATEGORY_EVALUATION_DETAIL = 3;
	public static final int BOOK_CATEGORY_EVALUATION_CONCLUSION = 4;
	public static final int BOOK_CATEGORY_EVALUATION_INDEX = 5;
	public static final int BOOK_CATEGORY_TRAVEL_RAIDER = 6;
		
	public static final String BOOK_DETAIL_PRIFIX = "BOOK_DETAIL";
		
	//account webservice 
	public static final int ACCOUNT_OPT_LOGIN = 1;
	public static final int ACCOUNT_OPT_REGISTER = 2;
	public static final int ACCOUNT_OPT_CHANGE_PASSWORD = 3;
	public static final String MESSAGE_ID = "updateproduct20120525165049665";
	public static final String MAGIC_KEY = "a026a499d9d30f5d724522214bd03065";
	public static final String PROXY_ID = "android_sign";
	public static final String PARTNER_CODE = "9000";
	
	// App头图
	public static final int ID_APP_HOME_IMAGE = 41;
	public static final int TAG_DEPART_City  = 1;
	public static final int TAG_ARRIVAL_City = 2;
	
	public static final String FTYPE_SINGLE = "1";
	public static final String FTYPE_DOUBLE = "2";
	
	public static final int CONNECTION_SHORT_TIMEOUT = 5000;//连接超时 5s
	public static final int READ_SHORT_TIMEOUT = 5000;//连接超时 5s
	
	public static final int CONNECTION_MIDDLE_TIMEOUT = 10000;//连接超时 10s
	public static final int READ_MIDDLE_TIMEOUT = 10000;//连接超时 10s
	
	public static final int CONNECTION_LONG_TIMEOUT = 20000;//连接超时20s
	public static final int READ_LONG_TIMEOUT = 20000;//连接超时 20s
	
	
	public static final String ERROR_MESSAGE = "没有找到与您搜索内容相关的信息，请重试";
	public static String LOADING_CONTENTS = "正在努力加载数据，请稍候...";
	
	public static int SLIPPING_DISTANCE = 180;
	
	//无退改签政策
	public static final String NO_REFUND = "无退改政策";
	
	//舱位数  
	public static String cabinNum = "0";
	public static int goCabinNum = 0;
	public static int returnCabinNum = 0;
	
	
	//--------------------------------------------------------------------------------------------------------------------
	// Image cache in SD card. SHOULD INIT in application's onCreate.
	//public static String imageCachePath = "";
	//public static String imageCachePath_data = "";
	public static MyApplication appInstance;
	
	// Map related.
	public static final String KEY_LONGTITUDE = "LONGTITUDE";
	public static final String KEY_LATITUDE = "LATITUDE";
	public static final String KEY_CURRENT_LONGTITUDE = "CURRENT_LONGTITUDE";
	public static final String KEY_CURRENT_LATITUDE = "CURRENT_LATITUDE";
	
	// Scenic search related.
	public static String KEY_SEARCH_KEYWORDS = "SEARCH_KEY";
	public static String KEY_SEARCH_THEME = "SEARCH_THEME";
	public static String KEY_SEARCH_AREA = "SEARCH_AREA";
	public static String KEY_ALL_THEME = "ALL_THEME";
	public static String KEY_MAP_OF_THEME_ID = "MAP_THEME_ID";
	
	// Setting related.
	public static String KEY_PUSH_STATUS = "PUSH_STATUS";
	
	//二维码扫描基本网址
	public static String BARCODE_BASE_URL = "visitbeijing.com.cn";
	public static String BARCODE_PARAM_CATEGORY = "category";
	public static String BARCODE_PARAM_ID = "id";
	public static String BARCODE_PARAM_CATEGORY_TICKET = "1";
	public static String BARCODE_PARAM_CATEGORY_ROUTINE = "2";
	public static String BARCODE_PARAM_CATEGORY_RAMBLE = "3";
	
	
	public static final String STATIC_MAP = "http://api.map.baidu.com/staticimage";

	public static String makeUrl(String url,List<RequestParameter> parameter) {

		 if(parameter!=null&&parameter.size()>0){
			 StringBuilder bulider  = new StringBuilder();
			 for(RequestParameter p:parameter){
				 if(bulider.length()!=0){
					 bulider.append("&");
				 }
				 
				 bulider.append(Utils.encode(p.getName()));
				 bulider.append("=");
				 bulider.append(Utils.encode(p.getValue()));
			 } 
			 url += "?"+bulider.toString();
		 }
		return url;
	}
}