package cn.com.zhenshiyin.crowd.net.utils;

public class ErrorUtil {
	public static String errorJson(String resultCode,String message){
		return "{\"result\":{\"resultCode\":\""+resultCode+"\",\"message\":\""+message+"\"}}";
	}
}
