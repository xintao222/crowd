package cn.com.zhenshiyin.crowd.net;

import java.io.Serializable;
import java.util.ArrayList;

public interface ThreadCallBack extends Serializable {

	public void onCallbackFromThread(String resultJson);
	
	public void onCallbackFromThread(String resultJson, int requestCode);
	
	public void onWebServiceCallback(String resultJson, int requestCode);
	
	public void onWebServiceCallback(String resultJson, int requestCode, Object callbackData);
}
