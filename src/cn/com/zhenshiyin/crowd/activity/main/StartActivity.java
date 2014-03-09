package cn.com.zhenshiyin.crowd.activity.main;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import cn.com.zhenshiyin.crowd.R;
import cn.com.zhenshiyin.crowd.base.BaseActivity;
import cn.com.zhenshiyin.crowd.common.Constants;
import cn.com.zhenshiyin.crowd.common.Preferences;
import cn.com.zhenshiyin.crowd.util.SharePreferencesUtil;

public class StartActivity extends BaseActivity {
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			Intent intent = new Intent();
			intent.setClass(StartActivity.this, PortalActivity.class);
			startActivity(intent);
			StartActivity.this.finish();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//start notificationService
		Intent intent = new Intent(this, cn.com.zhenshiyin.crowd.xmpp.NotificationService.class);
		startService(intent);

	}
	
	protected void onResume() {
		super.onResume();
		
		mHandler.sendEmptyMessageDelayed(0, 500);
	}

}
