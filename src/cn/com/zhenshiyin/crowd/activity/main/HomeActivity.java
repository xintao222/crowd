package cn.com.zhenshiyin.crowd.activity.main;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
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
	private double longitude = 116.33371;
	private double latitude = 39.98796;
	private Button btnNav;
	
	public BDLocationListener myListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "longitude=" + longitude + "; latitude=" + latitude);
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
            intent.putExtra(Constants.KEY_LATITUDE, latitude+0.000001);
            intent.putExtra(Constants.KEY_LONGTITUDE, longitude+0.000001);
            intent.putExtra(Constants.KEY_CURRENT_LONGTITUDE, longitude);
            intent.putExtra(Constants.KEY_CURRENT_LATITUDE, latitude);
            
            startActivity(intent);
            break;  
            }
    }

}
