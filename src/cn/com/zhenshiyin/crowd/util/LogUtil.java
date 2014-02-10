package cn.com.zhenshiyin.crowd.util;

import android.util.Log;

public class LogUtil {

	public static final boolean IS_LOG = false;

	private final static String LOG_TAG_STRING = "CROWD_Android";

	public static void d(String tag, String msg) {
		try {
			if (IS_LOG) {
				Log.d(tag, tag + " : " + msg);
			}
		} catch (Throwable t) {
		}
	}

	public static void i(String tag, String msg) {
		try {
			if (IS_LOG) {
				Log.i(LOG_TAG_STRING, tag + " : " + msg);
			}
		} catch (Throwable t) {
		}
	}

	public static void e(String tag, String msg) {
		try {
			if (IS_LOG) {
				Log.e(LOG_TAG_STRING, tag + " : " + msg);
			}
		} catch (Throwable t) {
		}
	}
	
	public static void w(String tag, String msg) {
		try {
			if (IS_LOG) {
				Log.w(LOG_TAG_STRING, tag + " : " + msg);
			}
		} catch (Throwable t) {
		}
	}
}
