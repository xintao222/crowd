package cn.com.zhenshiyin.crowd.base;


import android.os.Bundle;
import android.util.Log;
import cn.com.zhenshiyin.crowd.net.ThreadCallBack;
import cn.com.zhenshiyin.crowd.util.LogUtil;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class BaseMapActivity extends BaseActivity implements ThreadCallBack{
	private static final String TAG = "BaseMapActivity";
	
	public boolean mLocationReceived = false;
	public double longtitude;
	public double latitude;
	public LocationClient mLocationClient = null;
	
	public BDLocationListener myListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			longtitude = location.getLongitude();
			latitude = location.getLatitude();
			
			mLocationReceived = true;
			onLocationReceived(location);
			if (LogUtil.IS_LOG) Log.d(TAG, "longtitude=" + longtitude + "; latitude=" + latitude);
		}

		@Override
		public void onReceivePoi(BDLocation arg0) {
		}
		
	};
	
	protected void onLocationReceived(BDLocation location) {
		if (LogUtil.IS_LOG) Log.d(TAG, "[onLocationReceived] you should override the method!");
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initMaps();
	}
	@Override
	protected void onResume() {
		super.onResume();
		
		mLocationClient.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mLocationClient.stop();
	}
	
	private void initMaps() {
		mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
	    mLocationClient.registerLocationListener( myListener );    //注册监听函数
	    LocationClientOption locationClientOption = new LocationClientOption();
	    locationClientOption.setOpenGps(true);
	    locationClientOption.setCoorType("bd09ll");
	    //locationClientOption.setScanSpan(5000);
	    mLocationClient.setLocOption(locationClientOption);
	}
}
