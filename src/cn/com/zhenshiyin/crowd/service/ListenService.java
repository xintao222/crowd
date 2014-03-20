package cn.com.zhenshiyin.crowd.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.xmpp.NotificationService;
import cn.com.zhenshiyin.crowd.xmpp.XmppConstants;
import cn.com.zhenshiyin.crowd.xmpp.XmppManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
/**
 * Polling service
 * @Author Ryan
 * @Create 2013-7-13 上午10:18:44
 */
public class ListenService extends Service{

	public static final String ACTION = "cn.com.zhenshiyin.crowd.service.ListenService";
	private final String TAG = "ListenService";
    WakeLock wakeLock = null;  
    
    
    public double longitude;
    public double latitude;
    public LocationClient mLocationClient = null;
    
	ChatManager chatManager;
	Chat chat;
	String friend;
    
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
			
			SharedPreferences preference = getSharedPreferences("account", MODE_PRIVATE);
			String userName = preference.getString("name", "");
			String password = preference.getString("password", "");
			if(name.equals("")){
				if(LogUtil.IS_LOG) Log.i(TAG, "No user is verified on this device.");
				return;
			}

			xmppManager.setPassword(password);
			xmppManager.setUsername(userName);
			xmppManager.registerAccountHandler(handler);
			xmppManager.connect();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if(LogUtil.IS_LOG) Log.d(TAG, "onServiceDisconnected()...");
			binder = null;
			notificationService = null;
			xmppManager = null;
		}
	};
    
    private BDLocationListener myListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
        	longitude = location.getLongitude();
            latitude = location.getLatitude();
            onLocationReceived(location);
            mLocationClient.stop();
			
            if (LogUtil.IS_LOG) Log.d(TAG, "longtitude=" + longitude + "; latitude=" + latitude);
            sendMessage("longitude:" + longitude +";latitude:"+latitude);
        }

        @Override
        public void onReceivePoi(BDLocation arg0) {
        }
        
    };
    
    private void onLocationReceived(BDLocation location) {
        if (LogUtil.IS_LOG) Log.d(TAG, "[onLocationReceived] you should override the method!");
    }
    
    private void initLocationClient() {
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        LocationClientOption locationClientOption = new LocationClientOption();
        //locationClientOption.setOpenGps(true);
        locationClientOption.setCoorType("bd09ll");
        locationClientOption.setScanSpan(5 * 1000);
        mLocationClient.setLocOption(locationClientOption);
    }
    
    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行  
    private void acquireWakeLock()  
    {  
        if (null == wakeLock)  
        {  
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);  
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PostLocationService");  
            if (null != wakeLock)  
            {  
                wakeLock.acquire();  
            }  
        }  
    }  
      
    //释放设备电源锁  
    private void releaseWakeLock()  
    {  
        if (null != wakeLock)  
        {  
            wakeLock.release();  
            wakeLock = null;  
        }  
    }  

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		if(LogUtil.IS_LOG) Log.d(TAG, "onCreate()...");
		acquireWakeLock();
		initLocationClient();

	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		initChat();
	    if(LogUtil.IS_LOG) Log.d(TAG, "onStart(), bind service now.");
    	Intent i = new Intent(this, NotificationService.class);

    	boolean bindResult = getApplicationContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    	if (!bindResult) {
            if (LogUtil.IS_LOG) Log.d(TAG, "Binding to NotificationService failed");
            throw new IllegalStateException("Binding to NotificationService failed " + i);
        }
	}

    public void sendMessage(String msg){

		SharedPreferences sharedPrefs;
        sharedPrefs = getSharedPreferences(XmppConstants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		String xmppHost = sharedPrefs.getString(XmppConstants.XMPP_HOST, "localhost");
		
		xmppHost = "127.0.0.1";//Yes, it is ugly hard code to avoid RCVD: <message id="NlU08-4" to="lisi@192.168.101.122/AndroidpnClient" from="lisi@127.0.0.1/AndroidpnClient"
		Log.d(TAG, "xmppHost: "+xmppHost);
		
		if(chatManager == null)// !!!!!when User A log out and user B login, chatManager shoud be re-build.
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
						friend = message.getFrom();// store where it comes from
						if(LogUtil.IS_LOG) Log.d(TAG, "processMessage from: " + message.getFrom() + ", body: " + strMsg);
						if(strMsg.equalsIgnoreCase("where")){
							mLocationClient.start();
						}
					}
				});
			}
		});
    	 	
            if (!connection.isAuthenticated()) {
            	if(LogUtil.IS_LOG) Log.d(TAG, "connection is not Authenticated. ");
            	//showToast("No authenticated connection");
        		return false;
            }
            if (connection.isAnonymous()) {
            	if(LogUtil.IS_LOG) Log.d(TAG, "connection is isAnonymous.");
            	//showToast("Anonymous user can't have roster.");
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

	protected Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if(LogUtil.IS_LOG) Log.d(TAG, "handler got msg:" + msg.what);
			switch (msg.what) {
			case XmppConstants.CONNECT_SUCCESSFULLY:
				if (LogUtil.IS_LOG)Log.i(TAG, "connect successfully ");
					initChat();
				break;
			case XmppConstants.CONNECT_FAILED:
				if (LogUtil.IS_LOG)Log.i(TAG, "connect failed ");
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		releaseWakeLock();
		 if (LogUtil.IS_LOG) Log.d(TAG,"Service:onDestroy");
	}

}
