package cn.com.zhenshiyin.crowd.activity.main;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cn.com.zhenshiyin.crowd.base.BaseActivity;
import cn.com.zhenshiyin.crowd.activity.map.NavigationMapActivity;
import cn.com.zhenshiyin.crowd.common.Constants;
import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.R;
public class HomeActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "HomeActivity";
	private double longitude = -1;
	private double latitude = -1;
	private Button btnNav;
	private LocationClient mLocationClient = null;
	
	public BDLocationListener myListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			
			if (LogUtil.IS_LOG) Log.d(TAG, "longitude=" + longitude + "; latitude=" + latitude);
		}

		@Override
		public void onReceivePoi(BDLocation arg0) {
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		btnNav = (Button) findViewById(R.id.nav);
		btnNav.setOnClickListener(this);
		
		mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
	    mLocationClient.registerLocationListener( myListener );    //注册监听函数
	    LocationClientOption locationClientOption = new LocationClientOption();
	    locationClientOption.setOpenGps(true);
	    locationClientOption.setCoorType("bd09ll");
	    locationClientOption.setScanSpan(5000);
	    mLocationClient.setLocOption(locationClientOption);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.home, menu);
//		return true;
//	}
	
    @Override
    public void onClick(View view) {
    	 switch(view.getId()) {
        case R.id.nav:
            Intent intent = new Intent(this, NavigationMapActivity.class);
            intent.putExtra(Constants.KEY_LATITUDE, latitude+0.001);
            intent.putExtra(Constants.KEY_LONGTITUDE, longitude+0.001);
            intent.putExtra(Constants.KEY_CURRENT_LONGTITUDE, longitude);
            intent.putExtra(Constants.KEY_CURRENT_LATITUDE, latitude);
            
            startActivity(intent);
            break;  
            }
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

}
