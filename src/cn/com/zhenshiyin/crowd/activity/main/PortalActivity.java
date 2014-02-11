package cn.com.zhenshiyin.crowd.activity.main;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.TabHost;
import cn.com.zhenshiyin.crowd.R;
import cn.com.zhenshiyin.crowd.base.BaseTabActivity;
import cn.com.zhenshiyin.crowd.common.Preferences;
import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.util.SharePreferencesUtil;
import cn.com.zhenshiyin.crowd.widget.FirstGuideView;
import cn.com.zhenshiyin.crowd.widget.FirstGuideView.onFirstGuideFinishedListener;
//import cn.com.zhenshiyin.crowd.zxing.CaptureActivity;


public class PortalActivity extends BaseTabActivity {
	private static final String TAG = "PortalActivity";
	private static final String GUIDE_SHOWN_KEY = "IS_GUIDE_SHOWN";
	
	private FirstGuideView mFirstGuideView;
	private String currentTab = null;
	private TabHost tabHost;
	private View mGuideView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "onCreate");
		setContentView(R.layout.portal_tab_layout);
		
		initView();

	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "onNewIntent");
		setIntent(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		currentTab = getIntent().getStringExtra("CURRENT_TAB");
		
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[onResume] currentTab=" + currentTab);
		if (currentTab != null && currentTab.length() > 0) {
			tabHost.setCurrentTabByTag(currentTab);
			currentTab = null;
			
			Intent intent = getIntent();
			intent.putExtra("CURRENT_TAB", "");
			setIntent(intent);
		}
		
	}
	
	private void initView() {
		tabHost = getTabHost();
		
		tabHost.addTab(tabHost.newTabSpec("HOME")
				.setIndicator(LayoutInflater.from(this).inflate(R.layout.tab_indicator_home, null))
				.setContent(new Intent(this, HomeActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("CENTER")
				.setIndicator(LayoutInflater.from(this).inflate(R.layout.tab_indicator_center, null))
				.setContent(new Intent(this, HomeActivity.class)));
		
		mGuideView = findViewById(R.id.guide);
		
		boolean isGuideShown = getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(GUIDE_SHOWN_KEY, false);
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[onResume] isGuideShown=" + isGuideShown);
		if (!isGuideShown) {
			mGuideView.setVisibility(View.VISIBLE);
			
			getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(GUIDE_SHOWN_KEY, true).apply();
		}
		
		String isFirst = SharePreferencesUtil.getPreference(this, Preferences.IS_FIRST_TYPE_TAG, Preferences.IS_FIRST_KEY_TAG);
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "isFirst=" + isFirst);
		if (TextUtils.isEmpty(isFirst)) {
			mFirstGuideView = (FirstGuideView) findViewById(R.id.first_guide);
			if (mFirstGuideView == null) {
				ViewStub stub = (ViewStub) findViewById(R.id.first_guide_stub);
				mFirstGuideView = (FirstGuideView) stub.inflate();
			}
			
			if (mFirstGuideView != null) {
				mFirstGuideView.setFirstGuideFinishedListener(mOnFirstGuideFinishedListener);
			}
		}
	}
	
	public void onGuideClick(View view) {
		mGuideView.setVisibility(View.GONE);
	}
	
	private onFirstGuideFinishedListener mOnFirstGuideFinishedListener = new onFirstGuideFinishedListener() {

		@Override
		public void onFirstGuideFinished() {
			mFirstGuideView.setVisibility(View.GONE);
			SharePreferencesUtil.savePreference(PortalActivity.this, Preferences.IS_FIRST_TYPE_TAG, Preferences.IS_FIRST_KEY_TAG, "first");
		}
		
	};
}