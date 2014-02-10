package cn.com.zhenshiyin.crowd.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import cn.com.zhenshiyin.crowd.R;
//import cn.com.zhenshiyin.crowd.service.LocationService;
import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.util.SystemInfoUtils;
import cn.com.zhenshiyin.crowd.net.DefaultThreadPool;


import com.baidu.mapapi.BMapManager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.telephony.TelephonyManager;


public class MyApplication extends Application {
	private static final String TAG = "MyApplication";
	
	public static int CACHE_INTERNAL = 0;
	public static int CACHE_SDCARD = 1;
	
	private BMapManager mBMapMan = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// 获取硬件id
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		if (tm.getDeviceId() != null) {
			Constants.HWID = tm.getDeviceId();
		}
		
		// Get the sd card image cache path.
//		if (SystemInfoUtils.avaiableSdcard()) {
//			Constants.imageCachePath = getExternalCacheDir().getAbsolutePath() + File.separator;
//		}
//		
//		Constants.imageCachePath_data = getCacheDir().getAbsolutePath() + File.separator;
		Constants.appInstance = this;
		
		//Copy the Home View image.
		//copyHomeViewImg();
		
		// Initiate the Map Manager.
		mBMapMan = new BMapManager(this);
        mBMapMan.init(Constants.MAP_KEY, null);
        
        // Start LocationService.
        //startService(new Intent(this, LocationService.class));
	}
	
	@Override
	public void onLowMemory() {
		/**
		 * 低内存的时候主动释放所有线程和资源 
		 * 
		 * PS:这里不一定每被都调用
		 */
		DefaultThreadPool.shutdown();
		LogUtil.i(this.getClass().getName(), "MyApplication  onError  onLowMemory");
		super.onLowMemory();
	}
	
	@Override
	public void onTerminate() {
		/**
		 * 系统退出的时候主动释放所有线程和资源
		 * PS:这里不一定被都调用
		 */
		DefaultThreadPool.shutdown();
		LogUtil.i(this.getClass().getName(), "MyApplication  onError  onTerminate");
		super.onTerminate();
	}
	
	/**
	 * 向本包的其它类暴露全局的mBMapMan
	 */
	public BMapManager getMapManager() {
		return mBMapMan;
	}
	

	
	public String getCacheDirPath(int cacheType) {
		if (cacheType == CACHE_SDCARD) {
			try {
				String status = Environment.getExternalStorageState();
				if (status.equals(Environment.MEDIA_MOUNTED)) {
					return getExternalCacheDir().getAbsolutePath() + File.separator;
				} else {
					return null;
				}
			} catch (Exception e) {
				if (LogUtil.IS_LOG) LogUtil.d(this.getClass().getName(), "get external cache error: "  + e);
				return null;
			}
		} else if (cacheType == CACHE_INTERNAL) {
			return getCacheDir().getAbsolutePath() + File.separator;
		}
		
		return null;
	}
}