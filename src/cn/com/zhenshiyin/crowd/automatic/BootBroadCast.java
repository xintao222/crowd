package cn.com.zhenshiyin.crowd.automatic;

import cn.com.zhenshiyin.crowd.service.ListenService;
import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.xmpp.NotificationService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadCast extends BroadcastReceiver {
	private final String TAG = "BootBroadCast";
	 @Override  
	    public void onReceive(Context context, Intent intent) {  
	        /*  
	         * 开机启动服务*/
		 
		 if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
			  if(LogUtil.IS_LOG)Log.i(TAG, "good. Received boot completed msg.");
		        // Start the service
              Intent ns = NotificationService.getIntent();
              context.startService(ns);
              
              Intent ls = new Intent(context, ListenService.class);
              context.startService(ls);
		 }
	 }

}
