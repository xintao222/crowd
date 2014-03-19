package cn.com.zhenshiyin.crowd.activity.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import cn.com.zhenshiyin.crowd.net.utils.RequestParameter;
import cn.com.zhenshiyin.crowd.xmpp.NotificationService;
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

	private Button btnRefresh;
	
	private ImageView mAddressThumb;
	
	private LocationClient mLocationClient = null;
	ChatManager chatManager;
	Chat chat;
	String friend;
	
	private final int MSG_REMOTE_POS_RECEIVED = 0;
	private final int MSG_PIC_RECEIVED_SUCESSFULLY = 1;
	private final int MSG_PIC_RECEIVED_FAILED = 2;
	
	protected static NotificationService.NotificationServiceBinder  binder;
	protected static NotificationService notificationService;
	protected static XmppManager xmppManager;
	protected ServiceConnection conn = new ServiceConnection() {
		
		//Question: if user A log out and user B login, how to reget binder and notificationService?
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if(LogUtil.IS_LOG) Log.d(TAG, "onServiceConnected()...");
			binder = (NotificationService.NotificationServiceBinder) service;
			notificationService = binder.getService();
			xmppManager = notificationService.getXmppManager();
			if(xmppManager == null){
				if(LogUtil.IS_LOG) Log.d(TAG, "xmppManager is null, we create it now...");
				xmppManager = new XmppManager(notificationService);
				notificationService.setXmppManager(xmppManager);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if(LogUtil.IS_LOG) Log.d(TAG, "onServiceDisconnected()...");
			binder = null;
			notificationService = null;
			xmppManager = null;
		}
	};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		mAddressThumb = (ImageView) findViewById(R.id.address_thumb);
		mAddressThumb.setBackgroundResource(R.drawable.ic_launcher);

		mAddressThumb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showMap();
			}
			
		});
		
		btnRefresh = (Button) findViewById(R.id.refresh);
		btnRefresh.setOnClickListener(this);
		
		mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
	    mLocationClient.registerLocationListener( myListener );    //注册监听函数
	    LocationClientOption locationClientOption = new LocationClientOption();
	    locationClientOption.setOpenGps(true);
	    locationClientOption.setCoorType("bd09ll");
	    locationClientOption.setScanSpan(5000);
	    mLocationClient.setLocOption(locationClientOption);
	    

	}

	private void showAddressThumbInHandler(String latitude, String longitude){
		Thread thread = new Thread(runnable);
		thread.start();
	}

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			List<cn.com.zhenshiyin.crowd.net.utils.RequestParameter> parameter = new ArrayList<RequestParameter>();
			parameter.clear();
			parameter.add(new RequestParameter("center", remoteLongitude +","+remoteLatitude));
			parameter.add(new RequestParameter("width", "600"));
			parameter.add(new RequestParameter("height", "300"));
			parameter.add(new RequestParameter("zoom", "16"));
			parameter.add(new RequestParameter("markers", remoteLongitude +","+remoteLatitude));
			
			String url = Constants.makeUrl(Constants.STATIC_MAP, parameter);
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "img_url = " + url);
			HttpGet httpGet = new HttpGet(url);
			final Bitmap bitmap;
			try {
				HttpResponse httpResponse = httpClient.execute(httpGet);
				bitmap = BitmapFactory.decodeStream(httpResponse.getEntity()
						.getContent());
			} catch (Exception e) {
				handler.obtainMessage(MSG_PIC_RECEIVED_FAILED).sendToTarget();// 获取图片失败
				if(LogUtil.IS_LOG) Log.d(TAG, "exception when load pic: "+e.toString());
				return;
			}
//			This thread is not created by UI thread, so the only way is to send bitmap object to UI thread and refresh widget.
			handler.obtainMessage(MSG_PIC_RECEIVED_SUCESSFULLY, bitmap).sendToTarget();
		}
	};

    public void sendMessage(String msg){

		
		SharedPreferences sharedPrefs;
        sharedPrefs = getSharedPreferences(XmppConstants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		String xmppHost = sharedPrefs.getString(XmppConstants.XMPP_HOST, "localhost");
		
		xmppHost = "127.0.0.1";//Yes, it is ugly hard code to avoid RCVD: <message id="NlU08-4" to="lisi@192.168.101.122/AndroidpnClient" from="lisi@127.0.0.1/AndroidpnClient"
		Log.d(TAG, "xmppHost: "+xmppHost);
		
		if(chatManager == null)
			initChat();
		
		if(chat == null)
			chat = chatManager.createChat(friend + "/AndroidpnClient", null);
		
		try {
			if(LogUtil.IS_LOG) Log.d(TAG, "send msg: "+msg);
			chat.sendMessage(msg);
			
		} catch (XMPPException e) {
			e.printStackTrace();
		}
    }
	
	public void showMap() {
        Intent intent = new Intent(this, NavigationMapActivity.class);
        intent.putExtra(Constants.KEY_LATITUDE, remoteLatitude);
        intent.putExtra(Constants.KEY_LONGTITUDE, remoteLongitude);
        intent.putExtra(Constants.KEY_CURRENT_LONGTITUDE, longitude);
        intent.putExtra(Constants.KEY_CURRENT_LATITUDE, latitude);
        
        startActivity(intent);
	}
	
	protected Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if(LogUtil.IS_LOG) Log.d(TAG, "handler got msg:" + msg.what);
			switch (msg.what) {
			case MSG_REMOTE_POS_RECEIVED:
				showAddressThumbInHandler(remoteLatitude + "", remoteLongitude + "");
				break;
			case MSG_PIC_RECEIVED_SUCESSFULLY:
				Bitmap bmp = (Bitmap) msg.obj;
				mAddressThumb.setImageBitmap(bmp);
			case XmppConstants.LOGIN_SUCCESSFULLY:
				if (LogUtil.IS_LOG)Log.i(TAG, "login successfully ");
				break;
			case XmppConstants.LOGIN_FAILED:
				if (LogUtil.IS_LOG)Log.i(TAG, "login failed ");
				break;
			default:
				break;
			}
		}
	};
	
    @Override
    public void onClick(View view) {
    	 switch(view.getId()) {
    	case R.id.refresh:
    		if(initChat())//if we initialize chat successfully, send msg
    			sendMessage("where");
    		else
    			showToast("Somethins is wrong, logout and re-login please.");
    		break;
            }
    }
    
	private boolean initChat(){
		XMPPConnection connection = xmppManager.getConnection();
		if(connection == null){
			if(LogUtil.IS_LOG) Log.d(TAG, "connection is null, connect now... ");
			xmppManager.connect();
			return false;
		}
		
    	chatManager = connection.getChatManager();
		if(chatManager == null){
			if(LogUtil.IS_LOG) Log.d(TAG, "chatManager is null ");
			return false;
		}
		
    	chatManager.addChatListener(new ChatManagerListener() {
			@Override
			public void chatCreated(Chat chat, boolean able) {
				chat.addMessageListener(new MessageListener() {
					@Override
					public void processMessage(Chat chat2, Message message) {
						String strMsg = message.getBody();
						if(LogUtil.IS_LOG) Log.d(TAG, "processMessage from: " + message.getFrom() + ", body: " + strMsg);
						if(strMsg.equalsIgnoreCase("where")){
							mLocationClient.start();
						}else if(strMsg.contains("longitude")){
							int splitPos = strMsg.indexOf(";");
							//retrieve longitude
							String remoteLongitudeContnet = strMsg.substring(0, splitPos);
							int annotationPos = remoteLongitudeContnet.indexOf(":");
							remoteLongitude = Double.valueOf(remoteLongitudeContnet.substring(annotationPos +1, splitPos));
							if(LogUtil.IS_LOG) Log.d(TAG, "remoteLongitude: " + remoteLongitude);
							
							//retrieve latitude
							String remoteLatitudeContnet = strMsg.substring(splitPos+1);
							annotationPos = remoteLatitudeContnet.indexOf(":");
							remoteLatitude = Double.valueOf(remoteLatitudeContnet.substring(annotationPos +1));
							if(LogUtil.IS_LOG) Log.d(TAG, "remoteLatitude: " + remoteLatitude);
							
							//send a msg to main thread and show pic in main thread. An runtime exception will be thrown if we create a async task to download pic.
			                android.os.Message msg = handler.obtainMessage();
			                msg.what = MSG_REMOTE_POS_RECEIVED;
			                msg.obj = null;
			                handler.sendMessage(msg);
						}
					}
				});
			}
		});
    	 	
            if (!connection.isAuthenticated()) {
            	if(LogUtil.IS_LOG) Log.d(TAG, "connection is not Authenticated. ");
            	showToast("No authenticated connection");
        		return false;
            }
            if (connection.isAnonymous()) {
            	if(LogUtil.IS_LOG) Log.d(TAG, "connection is isAnonymous.");
            	showToast("Anonymous user can't have roster.");
        		return false;
            }
            
            Roster r = connection.getRoster();
        	if(r == null){
        		if(LogUtil.IS_LOG) Log.d(TAG, "roster is null");
        	}
    		if (r != null) {
    			r.addRosterListener(new RosterListener() {
    				public void entriesDeleted(Collection<String> addresses) {
    					if(LogUtil.IS_LOG) Log.i(TAG, "entriesDeleted()");
    					System.out.println("deleted: " + addresses.size());
    				}

    				public void entriesUpdated(Collection<String> addresses) {
    					if(LogUtil.IS_LOG) Log.i(TAG, "entriesUpdated()");
    					System.out.println("updated: " + addresses.size());
    				}

    				public void entriesAdded(Collection<String> addresses) {
    					if(LogUtil.IS_LOG) Log.i(TAG, "entriesAdded()");
    					System.out.println("added: " + addresses.size());
    					for(String address : addresses){
    						System.out.println("added:address =  " + address);
    					}
    				}

    				public void presenceChanged(Presence presence) {
    					if(LogUtil.IS_LOG) Log.i(TAG, "presenceChanged()");
    					System.out.println("Presence changed: "
    							+ presence.getFrom() + " " + presence + " "
    							+ presence.getStatus());
    					System.out.println(presence.getProperty("key"));
    				}
    			});

    			if(LogUtil.IS_LOG) Log.d(TAG, "roster...Entry count:" + r.getEntryCount());
    			
    			Collection<RosterEntry> entries = r.getEntries();
    			// loop through
    			for (RosterEntry entry : entries) {
    				Presence entryPresence = r.getPresence(entry.getUser());
    				if(LogUtil.IS_LOG) Log.d(TAG, "initChat roster..." + entry.getUser());
    				friend = entry.getUser();
    				Presence.Type userType = entryPresence.getType();
    			}

    			if(LogUtil.IS_LOG) Log.d(TAG, "presence account:" + r.getEntryCount());
    		}
    		
    		return true;
	}

    
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
	
	public void tryLogin() {
		// Invalid check. TODO:
		
		SharedPreferences preference = getSharedPreferences("account",
				this.MODE_PRIVATE);
		String name = preference.getString("name", "");
		String password = preference.getString("password", "");
		if(name.equals("")){
			if(LogUtil.IS_LOG) Log.i(TAG, "No user is verified on this device.");
			return;
		}
    	
		if(xmppManager == null){// no bind now
			return;
		}else{
			xmppManager.setPassword(password);
			xmppManager.setUsername(name);
			xmppManager.registerAccountHandler(handler);
			xmppManager.connect();
		}
		
		// Build and request.

	}
	@Override
	protected void onResume() {
		super.onResume();
		
		// We bind service onResume because User A may log out and User B login, then new connection must be retrieved.
	    if(LogUtil.IS_LOG) Log.d(TAG, "onResume(), bind service now.");
    	Intent intent = new Intent(this, cn.com.zhenshiyin.crowd.xmpp.NotificationService.class);

    	boolean bindResult = getApplicationContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    	if (!bindResult) {
            if (LogUtil.IS_LOG) Log.d(TAG, "Binding to service failed");
            throw new IllegalStateException("Binding to service failed " + intent);
        }
    	
    	tryLogin();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

}
