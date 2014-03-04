package cn.com.zhenshiyin.crowd.activity.account;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.com.zhenshiyin.crowd.R;
import cn.com.zhenshiyin.crowd.base.BaseActivity;
import cn.com.zhenshiyin.crowd.common.Common;
import cn.com.zhenshiyin.crowd.common.Constants;
import cn.com.zhenshiyin.crowd.util.LogUtil;

import cn.com.zhenshiyin.crowd.xmpp.NotificationService;
import cn.com.zhenshiyin.crowd.xmpp.ServiceManager;
import cn.com.zhenshiyin.crowd.xmpp.XmppManager;
import cn.com.zhenshiyin.crowd.xmpp.XmppConstants;
public class StartAccountActivity extends BaseActivity {

	private static final String TAG = "StartAccountActivity";
	
	private static final int REQUEST_LOGIN = 100;
	private static final int REQUEST_USER_INFO = 101;
	private static final int QUERY_FIRST_ORDER_INFO = 102;
	
	private String name = "";
	private String password = "";
	private String userID = "";
	private View verifiedAccountView;
	private View unverifiedAccountView;
	private EditText nameEditor;
	private EditText passwordEditor;
	private View mRegister;

	Button btnForgetPassword;
	TextView error;

	private TextView mAccountNameView;
	private TextView mChangyoubiView;
	private ProgressBar mChangyoubiProgressView;
	
	private boolean mFromShake = false;
	private boolean mFromOrder = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.account);
		
		initViews();
		
        // Start the service
        ServiceManager serviceManager = new ServiceManager(this);
        serviceManager.setNotificationIcon(R.drawable.notification);
        serviceManager.startService();
    	Intent intent = new Intent(this, cn.com.zhenshiyin.crowd.xmpp.NotificationService.class);

    	boolean bindResult = getApplicationContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    	if (!bindResult) {
            if (LogUtil.IS_LOG) Log.d(TAG, "Binding to service failed");
            throw new IllegalStateException("Binding to service failed " + intent);

        }
	}
	
	@Override
	public void onWebServiceCallback(String resultJson, int requestCode) {
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "resultJson=" + resultJson);
		
		switch(requestCode) {
		case REQUEST_LOGIN:
	
		}
	}
	
	private void saveUserInfo() {
		SharedPreferences preference = getSharedPreferences("account",MODE_PRIVATE);

		Editor edit = preference.edit();
		edit.putString("name", name);
		edit.putString("userID", userID);
		edit.putString("password", password);
		edit.commit();
		
		passwordEditor.setText("");
	}
	

	

	
	private void initViews() {
		// Init title.
		((TextView)findViewById(R.id.title_content)).setText(getTitle());
		findViewById(R.id.title_left_button).setVisibility(View.INVISIBLE);
		findViewById(R.id.title_right_button).setVisibility(View.GONE);
		
		// Init content.
		error = (TextView) findViewById(R.id.error);
		verifiedAccountView = (View) findViewById(R.id.account_verified);
		unverifiedAccountView = (View) findViewById(R.id.account_unverified);
		
		btnForgetPassword = (Button) findViewById(R.id.title_right_button1);
		btnForgetPassword.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.rectangle_background_red));
		btnForgetPassword.setVisibility(View.VISIBLE);
		btnForgetPassword.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onTitleRightButtonClick(v);
			}
		});
		
		nameEditor = (EditText) findViewById(R.id.name_editor);
		nameEditor.setOnFocusChangeListener(Common.ON_EDIT_FOCUS_CHANGE_LISTENER);
		
		passwordEditor = (EditText) findViewById(R.id.password_editor);
		passwordEditor.setOnFocusChangeListener(Common.ON_EDIT_FOCUS_CHANGE_LISTENER);
		
		mRegister = findViewById(R.id.register);
		mRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(StartAccountActivity.this,
						AccountRegisterActivity.class);
				startActivity(intent);
			}
			
		});
		
		mAccountNameView = (TextView) findViewById(R.id.account_name);
		mChangyoubiView = (TextView) findViewById(R.id.account_friend);
		mChangyoubiProgressView = (ProgressBar) findViewById(R.id.account_friend_progress);
	}

	public void onTitleRightButtonClick(View v) {
		Intent intent = new Intent();
		
		if (isLogedIn()) {
			// Change password
			intent.setClass(this, AccountChangePasswordActivity.class);
		} else {
			// Get password back.
			intent.setClass(this, AccountGetbackPasswordActivity.class);
		}
		
		startActivity(intent);
	}
	
	private void freshViews() {
		if (isLogedIn()) {
			if (LogUtil.IS_LOG) LogUtil.d(TAG, name + " is login!");
			
			verifiedAccountView.setVisibility(View.VISIBLE);
			unverifiedAccountView.setVisibility(View.GONE);
			
			btnForgetPassword.setText(R.string.account_change_password);
			
			mAccountNameView.setText(name);
		} else {
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "No user login!");
			
			verifiedAccountView.setVisibility(View.GONE);
			unverifiedAccountView.setVisibility(View.VISIBLE);			
			btnForgetPassword.setText(R.string.account_forget_password);
		}
	}
	
	private boolean isLogedIn() {
		boolean logedIn = false;
		
		SharedPreferences preference = getSharedPreferences("account",
				this.MODE_PRIVATE);
		name = preference.getString("name", name);
		userID = preference.getString("userID", userID);
		
		if (!name.equals("")) {
			logedIn = true;
		}
		
		return logedIn;
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// state may change when resume activity, for example coming back after
		// registering a new account.
		freshViews();
	}

	
	public void login(View view) {
		// Invalid check. TODO:
		name = nameEditor.getText().toString();
		password = passwordEditor.getText().toString();
    	
		xmppManager.setPassword(password);
		xmppManager.setUsername(name);
    	xmppManager.registerAccountHandler(handler);
    	
		notificationService.setXmppManager(xmppManager);
		notificationService.taskSubmitter.submit(new Runnable() {
            public void run() {
            	notificationService.start();
            }
        });
		
		// Build and request.

	}

	protected Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (LogUtil.IS_LOG)Log.i(TAG, "msg is " + msg.what);
			switch (msg.what) {
			case XmppConstants.LOGIN_SUCCESSFULLY:
				if (LogUtil.IS_LOG)Log.i(TAG, "login successfully ");
				saveUserInfo();
				freshViews();
				break;
			case XmppConstants.LOGIN_FAILED:
				if (LogUtil.IS_LOG)Log.i(TAG, "login failed ");
				break;
			}
			}
	};
	
	public void onClickLogout(View v) {
		SharedPreferences preference = getSharedPreferences("account",MODE_PRIVATE);

		Editor edit = preference.edit();
		edit.putString("name", "");
		edit.putString("userID", "");
		edit.putString("password", "");
		edit.commit();

		freshViews();
		
		notificationService.taskSubmitter.submit(new Runnable() {
            public void run() {
            	notificationService.stop();
            }
        });
	}
}
