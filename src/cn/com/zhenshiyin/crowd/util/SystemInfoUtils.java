package cn.com.zhenshiyin.crowd.util;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.view.Display;
import cn.com.zhenshiyin.crowd.common.Constants;

/**
* @ClassName: SystemInfoUtils
* @Description: TODO(获取系统信息的工具类)
* 
*/
public class SystemInfoUtils {
	/**
	* 获取当前的网络状态 -1：没有网络
	* 1：WIFI网络2：wap网络3：net网络
	* @param context
	* @return
	*/
	public static String getNetWorkType(Context context) {
		String netType = "unknown";
		try{
			ConnectivityManager connMgr = (ConnectivityManager) context
			.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if(networkInfo!=null){
				int nType = networkInfo.getType();
				if (nType == ConnectivityManager.TYPE_MOBILE) {
					netType =  networkInfo.getExtraInfo().toLowerCase();
				} else if (nType == ConnectivityManager.TYPE_WIFI) {
					netType =  "wifi";
				}
			}
		}catch (Exception e) {
		}
		return netType;
	}
	

	/**
	 * @Title :getScreenSize
	 * @Description :获取屏幕尺寸
	 * @params @param context
	 * @params @return 
	 * @return String  高_宽
	 * 
	 */
	public static String getScreenSize(Context context){
		String defaultSize = "800_480";
		try{
			Display d = ((Activity)context).getWindowManager().getDefaultDisplay();
//			DisplayMetrics dm = new DisplayMetrics();
//			d.getMetrics(dm);
			defaultSize = d.getHeight()+"_"+d.getWidth();

		}catch (Exception e) {
			// TODO: handle exception
		}
		return defaultSize;
	}
	
	public static Rect getDefaultImageBounds(Context context) {
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = (int) (width * 9 / 16);
		
		Rect bounds = new Rect(0, 0, width, height);
		return bounds;
	}
	
	/**
	 * 判断是否存在sd卡
	 * 
	 * @return
	 */
	public static boolean avaiableSdcard() {
		String status = Environment.getExternalStorageState();

		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	
}
