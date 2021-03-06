package cn.com.zhenshiyin.crowd.base;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import cn.com.zhenshiyin.crowd.common.Constants;
import cn.com.zhenshiyin.crowd.net.AsyncHttpGet;
import cn.com.zhenshiyin.crowd.net.AsyncHttpPost;
import cn.com.zhenshiyin.crowd.net.BaseRequest;
import cn.com.zhenshiyin.crowd.net.DefaultThreadPool;
import cn.com.zhenshiyin.crowd.net.ThreadCallBack;
import cn.com.zhenshiyin.crowd.net.utils.CheckNetWorkUtil;
import cn.com.zhenshiyin.crowd.net.utils.RequestParameter;
import cn.com.zhenshiyin.crowd.util.AsyncImageLoader;
import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.util.StringUtil;
import cn.com.zhenshiyin.crowd.util.SystemInfoUtils;
import cn.com.zhenshiyin.crowd.xmpp.NotificationService;
import cn.com.zhenshiyin.crowd.xmpp.XmppManager;
import cn.com.zhenshiyin.crowd.R;

import com.baidu.mobstat.StatService;

public class BaseActivity extends FragmentActivity implements ThreadCallBack{
	private static final String TAG = BaseActivity.class.getSimpleName();


	/**
	 * 当前activity所持有的所有请求
	 */
	List<BaseRequest> requestList = null;

	public static final int TITLE_LEFT_BUTTON = 1;
    public static final int TITLE_RIGHT_BUTTON = 2;
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestList = new ArrayList<BaseRequest>();
		super.onCreate(savedInstanceState);

//		if (LogUtil.IS_LOG) LogUtil.d(TAG, "Window=" + getWindow());
//		if (LogUtil.IS_LOG) LogUtil.d(TAG, "Attach Menu -----");
//		BottomMenu menu = new BottomMenu(this);
//		menu.attachToActivity(this);
//		menu.setMenu(R.layout.menu_frame);
//		getFragmentManager().beginTransaction()
//			.replace(R.id.menu_frame, new BottomMenuFragment())
//			.commit();
//		if (LogUtil.IS_LOG) LogUtil.d(TAG, "Attach Menu ----------------");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		/**
		 * FIXME zhuyanlin 2013-4-10 统计信息
		 * 此处调用基本统计代码
		 **/
		StatService.onResume(this);
		
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		/**
		 * 在activity销毁的时候同时设置停止请求，停止线程请求回调
		 */
		cancelRequest();
		super.onPause();
		/**
		 * FIXME zhuyanlin 2013-4-10 统计信息
		 * 此处调用基本统计代码
		 **/
		StatService.onPause(this);
		//MobclickAgent.onPause(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		/**
		 * 在activity销毁的时候同时设置停止请求，停止线程请求回调
		 */
		cancelRequest();
		super.onDestroy();
	}

	public  void cancelRequest() {
		if (requestList != null && requestList.size() > 0) {
			for (BaseRequest request : requestList) {
				if (request.getRequest() != null) {
					try {
						request.getRequest().abort();
						requestList.remove(request.getRequest());
						if (LogUtil.IS_LOG) Log.d("netlib", "netlib ,onDestroy request to  "
								+ request.getRequest().getURI()
								+ "  is removed");
					} catch (UnsupportedOperationException e) {
						//do nothing .
					}
				}
			}
		}
	}

	/**
	 *  The Back button's click listener.
	 */
	public void onBack(View view) {
		finish();
	}
	
	@Override
	public void onBackPressed() {
//		if (menu != null && menu.isMenuShowing()) {
//			menu.showContent();
//		} else {
			super.onBackPressed();
//		}
	}
	
   public void hideTitleButton(int type) {
        if ((type & TITLE_LEFT_BUTTON) != 0) {
            findViewById(R.id.title_left_button).setVisibility(View.GONE);
        }
        
        if ((type & TITLE_RIGHT_BUTTON) != 0) {
            findViewById(R.id.title_right_button).setVisibility(View.GONE);
        }
    }
	
	@Override
	public void onCallbackFromThread(String resultJson) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onCallbackFromThread(String resultXml, int requestCode) {
		// TODO Auto-generated method stub	
	}
	
	@Override
    public void onTrimMemory(int level) {
        if (LogUtil.IS_LOG) LogUtil.d(TAG, "onTrimMemory: " + level);
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
        	AsyncImageLoader.clearImageCache();
        }
    }

	@Override
	public void onWebServiceCallback(String resultXml, int requestCode) {
		
	}

	@Override
	public void onWebServiceCallback(String resultJson, int requestCode,
			Object callbackData) {
		// TODO Auto-generated method stub
		
	}
	protected void showToast(String message)
	{
		Toast toast=Toast.makeText(this, (!StringUtil.isEmpty(message))?message:Constants.ERROR_MESSAGE, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	protected void showToast(String message, int length){
		Toast toast=Toast.makeText(this, (!StringUtil.isEmpty(message))?message:Constants.ERROR_MESSAGE, length);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
}
