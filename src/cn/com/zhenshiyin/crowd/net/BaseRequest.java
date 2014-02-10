package cn.com.zhenshiyin.crowd.net;

import java.io.Serializable;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

import android.os.Handler;
import android.os.Message;
/**
 * 目标：
 * 1、安全有序
 * 2、高效
 * 3、易用、易控制
 * 4、activity停止后停止该activity所用的线程。
 * 5、监测内存，当内存溢出的时候自动垃圾回收，清理资源 ，当程序退出之后终止线程池
 * @author zxy
 *
 */
public class BaseRequest  implements   Runnable, Serializable {
	//static HttpClient httpClient = null;
	HttpUriRequest request = null;
	//Use to distinguish the request sequence in the situation One activity start http request more than one times.
	int requestCode = -1; 

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	protected ParseHandler handler = null;
	protected String url = null;
	/**
	 * default is 5 ,to set .
	 */
	protected int connectTimeout = 5000;
	/**
	 * default is 5 ,to set .
	 */
	protected int readTimeout = 10000;
//	protected RequestResultCallback requestCallback = null;
	
	Handler resultHandler = new Handler() {
		public void handleMessage(Message msg) {
				String resultData = (String) msg.obj;
				if(!resultData.contains("ERROR.HTTP.008")){
				ThreadCallBack callBack = (ThreadCallBack) msg.getData()
						.getSerializable("callback");
			
				if (requestCode != -1) {
					callBack.onCallbackFromThread(resultData, requestCode);
				} else {
					callBack.onCallbackFromThread(resultData);
				}
			}

		}
	};
	
	@Override
	public void run() {
		
	}
	
	protected void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	protected void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	
	public HttpUriRequest getRequest() {
		return request;
	}
	
}
