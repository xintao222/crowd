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
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cn.com.zhenshiyin.crowd.net.URLImageGetter;
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
	private double remoteLongitude = -1;
	private double remoteLatitude = -1;
	private Button btnNav;
	private Button btnGet;
	
	private TextView mAddressThumb;
	
	private LocationClient mLocationClient = null;
	ChatManager chatManager;
	Chat chat;
	public BDLocationListener myListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			
			if (LogUtil.IS_LOG) Log.d(TAG, "longitude=" + longitude + "; latitude=" + latitude);
			sendMessage("longitude:" + longitude +";latitude:"+latitude);
			mLocationClient.stop();
		}

		@Override
		public void onReceivePoi(BDLocation arg0) {
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		mAddressThumb = (TextView) findViewById(R.id.address_pic);
//		String static_img_url = Constants.staticMapUrl("", "");
//		if (LogUtil.IS_LOG) LogUtil.d(TAG, "static_img_url = " + static_img_url);
//		URLImageGetter xxx = new URLImageGetter(this, mAddressThumb);
//		mAddressThumb.setText(Html.fromHtml(static_img_url, xxx, null));
//		mAddressThumb.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				//showMap();
//			}
//			
//		});
		
		btnNav = (Button) findViewById(R.id.nav);
		btnNav.setOnClickListener(this);
		
		btnGet = (Button) findViewById(R.id.get);
		btnGet.setOnClickListener(this);
		
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
							
							mLocationClient.start();
							
						}else if(strMsg.contains("longitude")){
							int splitPos = strMsg.indexOf(";");
							//retrieve longitude
							String remoteLongitudeContnet = strMsg.substring(0, splitPos);
							int annotationPos = remoteLongitudeContnet.indexOf(":");
							remoteLongitude = Double.valueOf(remoteLongitudeContnet.substring(annotationPos +1, splitPos));
							Log.d(TAG, "remoteLongitude: " + remoteLongitude);
							
							//retrieve latitude
							String remoteLatitudeContnet = strMsg.substring(splitPos+1);
							annotationPos = remoteLatitudeContnet.indexOf(":");
							remoteLatitude = Double.valueOf(remoteLatitudeContnet.substring(annotationPos +1));
							Log.d(TAG, "remoteLatitude: " + remoteLatitude);
							
							showAddressThumb(remoteLatitude + "", remoteLongitude + "");
						}
					}
				});
			}
		});
	}
	
	private void showAddressThumb(String latitude, String longitude){
		String static_img_url = Constants.staticMapUrl(latitude, longitude);
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "static_img_url = " + static_img_url);
		URLImageGetter xxx = new URLImageGetter(this, mAddressThumb);
		mAddressThumb.setText(Html.fromHtml(static_img_url, xxx, null));
		
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
            intent.putExtra(Constants.KEY_LATITUDE, remoteLatitude);
            intent.putExtra(Constants.KEY_LONGTITUDE, remoteLongitude);
            intent.putExtra(Constants.KEY_CURRENT_LONGTITUDE, longitude);
            intent.putExtra(Constants.KEY_CURRENT_LATITUDE, latitude);
            
            startActivity(intent);
            break;  
    	case R.id.get:
    		initChat();
    		sendMessage("where");
    		break;
            }
    
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		
		//
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		
	}

}
