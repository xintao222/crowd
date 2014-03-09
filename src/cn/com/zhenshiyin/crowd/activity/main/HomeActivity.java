package cn.com.zhenshiyin.crowd.activity.main;


import java.util.Collection;

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
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cn.com.zhenshiyin.crowd.net.URLImageGetter;
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
	private Button btnNav;
	private Button btnGet;
	
	private TextView mAddressThumb;
	
	private LocationClient mLocationClient = null;
	ChatManager chatManager;
	Chat chat;
	String friend;
	protected static NotificationService.NotificationServiceBinder  binder;
	protected static NotificationService notificationService;
	protected static XmppManager xmppManager;
	protected ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			 Log.d(TAG, "onServiceConnected()...");
			binder = (NotificationService.NotificationServiceBinder) service;
			notificationService = binder.getService();
			xmppManager = notificationService.getXmppManager();
			if(xmppManager == null){
				xmppManager = new XmppManager(notificationService);
				notificationService.setXmppManager(xmppManager);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};
	
	private final int MSG_REMOTE_POS_RECEIVED = 0;
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
		mAddressThumb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showMap();
			}
			
		});
		
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
	    
    	Intent intent = new Intent(this, cn.com.zhenshiyin.crowd.xmpp.NotificationService.class);

    	boolean bindResult = getApplicationContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    	if (!bindResult) {
            if (LogUtil.IS_LOG) Log.d(TAG, "Binding to service failed");
            throw new IllegalStateException("Binding to service failed " + intent);

        }
	    
	}
	private void initChat(){
    	//XmppManager xm = notificationService.getXmppManager();
		XMPPConnection connection = xmppManager.getConnection();
		if(connection == null){
			Log.d(TAG, "connection is null ");
			return;
		}
    	chatManager = connection.getChatManager();
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
                throw new IllegalStateException("Not logged in to server.");
            }
            if (connection.isAnonymous()) {
                throw new IllegalStateException("Anonymous users can't have a roster.");
            }
            
            Roster r = connection.getRoster();
        	if(r == null){
        		Log.d(TAG, "roster is null");
        	}
    		if (r != null) {
    			r.addRosterListener(new RosterListener() {
    				public void entriesDeleted(Collection<String> addresses) {
    					Log.i(TAG, "entriesDeleted()");
    					System.out.println("deleted: " + addresses.size());
    				}

    				public void entriesUpdated(Collection<String> addresses) {
    					Log.i(TAG, "entriesUpdated()");
    					System.out.println("updated: " + addresses.size());
    				}

    				public void entriesAdded(Collection<String> addresses) {
    					Log.i(TAG, "entriesAdded()");
    					System.out.println("added: " + addresses.size());
    					for(String address : addresses){
    						System.out.println("added:address =  " + address);
    					}
    				}

    				public void presenceChanged(Presence presence) {
    					Log.i(TAG, "presenceChanged()");
    					System.out.println("Presence changed: "
    							+ presence.getFrom() + " " + presence + " "
    							+ presence.getStatus());
    					System.out.println(presence.getProperty("key"));
    				}
    			});

    			Log.d(TAG, "roster...Entry count:" + r.getEntryCount());
    			
    			Collection<RosterEntry> entries = r.getEntries();
    			// loop through
    			for (RosterEntry entry : entries) {
    				Presence entryPresence = r.getPresence(entry.getUser());
    				Log.d(TAG, "initChat roster..." + entry.getUser());
    				friend = entry.getUser();
    				Presence.Type userType = entryPresence.getType();
    			}

    			Log.d(TAG, "presence..." + r.getEntryCount());
    		}            
        
	}
	
	private void showAddressThumb(String latitude, String longitude){
		String static_img_url = Constants.staticMapUrl(latitude, longitude);
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "static_img_url = " + static_img_url);
		URLImageGetter xxx = new URLImageGetter(this, mAddressThumb);
		mAddressThumb.setText(Html.fromHtml(static_img_url, xxx, null));
		mAddressThumb.invalidate();
	}

	
    public void sendMessage(String msg){

		
		SharedPreferences sharedPrefs;
        sharedPrefs = getSharedPreferences(XmppConstants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		String xmppHost = sharedPrefs.getString(XmppConstants.XMPP_HOST, "localhost");
		
		xmppHost = "127.0.0.1";//Yes, it is ugly hard code to avoid RCVD: <message id="NlU08-4" to="lisi@192.168.101.122/AndroidpnClient" from="lisi@127.0.0.1/AndroidpnClient"
		Log.d(TAG, "xmppHost: "+xmppHost);
		
		if(chat == null)
		chat = chatManager.createChat(friend + "@" + xmppHost + "/AndroidpnClient", null);
		
		try {
			Log.d(TAG, "-------->send msg: "+msg);
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
			
			switch (msg.what) {
			case MSG_REMOTE_POS_RECEIVED:
				showAddressThumb(remoteLatitude + "", remoteLongitude + "");

				break;
			default:
				break;
			}
			}
	};
    @Override
    public void onClick(View view) {
    	 switch(view.getId()) {
        case R.id.nav:
        	showMap();
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
