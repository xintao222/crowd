package cn.com.zhenshiyin.crowd.activity.main;


import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cn.com.zhenshiyin.crowd.xmpp.XmppConstants;
import cn.com.zhenshiyin.crowd.xmpp.XmppManager;
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
	private Button btnStart;
	
	private LocationClient mLocationClient = null;
	ChatManager chatManager;
	Chat chat;
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
		
		btnStart = (Button) findViewById(R.id.start);
		btnStart.setOnClickListener(this);
		
		mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
	    mLocationClient.registerLocationListener( myListener );    //注册监听函数
	    LocationClientOption locationClientOption = new LocationClientOption();
	    locationClientOption.setOpenGps(true);
	    locationClientOption.setCoorType("bd09ll");
	    locationClientOption.setScanSpan(5000);
	    mLocationClient.setLocOption(locationClientOption);
	    

	    
	}
	private void initChat(){
    	XmppManager xm = notificationService.getXmppManager();
    	chatManager = xm.getConnection().getChatManager();
    	chatManager.addChatListener(new ChatManagerListener() {
			@Override
			public void chatCreated(Chat chat, boolean able) {
				chat.addMessageListener(new MessageListener() {
					@Override
					public void processMessage(Chat chat2, Message message) {
						String strMsg = message.getBody();
						Log.d(TAG, "---------->processMessage from: " + message.getFrom() + ", body: " + strMsg);
						if(strMsg.equalsIgnoreCase("where")){

						}else if(strMsg.equalsIgnoreCase("pos")){
							
						}
					}
				});
			}
		});
	}
    public void sendMessage(String msg){

		
		SharedPreferences sharedPrefs;
        sharedPrefs = getSharedPreferences(XmppConstants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		String xmppHost = sharedPrefs.getString(XmppConstants.XMPP_HOST, "localhost");
		
		xmppHost = "127.0.0.1";//Yes, it is ugly hard code to avoid RCVD: <message id="NlU08-4" to="lisi@192.168.101.122/AndroidpnClient" from="lisi@127.0.0.1/AndroidpnClient"
		Log.d(TAG, "xmppHost: "+xmppHost);
		String recipient = "a";
		chat = chatManager.createChat(recipient + "@" + xmppHost + "/AndroidpnClient", null);
		
		try {
			Log.d(TAG, "-------->send msg: "+msg);
			chat.sendMessage(msg);
			
		} catch (XMPPException e) {
			e.printStackTrace();
		}
    }
	
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
    	case R.id.start:
    		initChat();
    		sendMessage("where");
    		break;
            }
    
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		
		//mLocationClient.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mLocationClient.stop();
	}

}
