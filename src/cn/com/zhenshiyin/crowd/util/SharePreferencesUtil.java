package cn.com.zhenshiyin.crowd.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharePreferencesUtil {
	//保存
	public static void savePreference(Context context,String preferenceType,String preferenceKey,String preferenceValue){
		//LogUtil.d("调用了保存Preference",  "preferenceType:"+preferenceType +"preferenceKey:"+preferenceKey+"preferenceValue:"+preferenceValue);
		SharedPreferences preference = context.getSharedPreferences(preferenceType,Context.MODE_PRIVATE);
        Editor edit = preference.edit();
        edit.putString(preferenceKey,preferenceValue);
        edit.commit();
	}
	//获取
	public static String getPreference(Context context,String preferenceType,String preferenceKey){
		SharedPreferences preference = context.getSharedPreferences(preferenceType,Context.MODE_PRIVATE);
		//LogUtil.d("调用了获取Preference",  "preferenceType:"+preferenceType +"preferenceKey:"+preferenceKey);
		return preference.getString(preferenceKey,"");
	}
	//清空
	public static void clearPreference(Context context,String preferenceType,String preferenceKey){
		//LogUtil.d("调用了清空Preference", "preferenceType:"+preferenceType +"preferenceKey:"+preferenceKey);
		SharedPreferences preference = context.getSharedPreferences(preferenceType,Context.MODE_PRIVATE);
        Editor edit = preference.edit();
        edit.putString(preferenceKey,"");
        edit.commit();
	}
}
