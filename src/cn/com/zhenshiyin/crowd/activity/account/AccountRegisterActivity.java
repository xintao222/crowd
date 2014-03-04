package cn.com.zhenshiyin.crowd.activity.account;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.com.zhenshiyin.crowd.R;
import cn.com.zhenshiyin.crowd.base.BaseActivity;
import cn.com.zhenshiyin.crowd.common.Common;
import cn.com.zhenshiyin.crowd.common.Constants;
import cn.com.zhenshiyin.crowd.common.Common.onSimpleAlertDismiss;
import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.util.ValidateUtil;
import cn.com.zhenshiyin.crowd.xmpp.XmppConstants;

public class AccountRegisterActivity extends BaseActivity {
	private static final String TAG = "AccountRegisterActivity";
	
	private static final int STEP_INPUT_PHONENUMBER = 1;
	private static final int STEP_INPUT_VERIFYCODE = 2;
	private static final int STEP_INPUT_PASSWORD = 3;
	
	private static final int OPT_GET_VERIFYCODE = 4;
	private static final int OPT_SUBMIT_REGISTER = 5;
	private static final int OPT_CHECK_USER_REGISTER = 6;
	
	private static final int MSG_UPDATE_TIME = 1000;
	
	private EditText mPhoneView;
	private EditText mVerifyCodeView;
	private EditText mPassWordView;
	private EditText mConfirmPassWordView;
	private Button btnRegister;
	private Button btnGetVerify;
	
	private int mStep = STEP_INPUT_PHONENUMBER;
	private int mTimer = 60;
	
	private String mUserId = null;
	private String mPhone = null;
	private String mVerifyCode = null;
	private String mPassword = null;
	private String mConfirmPassword = null;
	
	TextView error;
	
	private OnClickListener mStepClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
//			if (CheckValid()) {
				submitRegister(OPT_SUBMIT_REGISTER);
//			}
		}
		
	};
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "msg=" + msg.what);
			mHandler.removeMessages(MSG_UPDATE_TIME);
			
			mTimer--;
			if (mTimer > 0) {
				String timer = getString(R.string.get_verifycode_prompt, mTimer);
				btnGetVerify.setText(timer);
				
				mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
			} else if (mTimer == 0) {
				btnGetVerify.setText(R.string.get_verifycode);
				btnGetVerify.setEnabled(true);
			}
		}
	};
	
	private onSimpleAlertDismiss mOnAlertDismissListener = new onSimpleAlertDismiss() {
		@Override
		public void onSimpleAlertDismiss() {
			finish();
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.account_register);
		
		initViews();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");

	}
	
	@Override
	protected void onPause() {
		super.onPause();

	}
	
	@Override
	public void onWebServiceCallback(String resultJson, int requestCode) {
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "resultJson=" + resultJson);
		
		switch(requestCode) {
		case OPT_SUBMIT_REGISTER:
		
			
		case OPT_CHECK_USER_REGISTER:

			break;
		}
	}
	
	private void saveUserInfo() {
		SharedPreferences preference = getSharedPreferences("account",MODE_PRIVATE);

		Editor edit = preference.edit();
		edit.putString("name", mPhone);
		edit.putString("userID", mUserId);
		edit.putString("password", mPassword);
		edit.commit();
	}

	/**
	 * Init views.
	 */
	private void initViews() {
		// Init title
		TextView title = (TextView)findViewById(R.id.title_content);
		title.setText(R.string.account_register);
		
		// Init other views.
		mPhoneView = (EditText) findViewById(R.id.phone_editor);
		mVerifyCodeView = (EditText) findViewById(R.id.verifycode_editor);
		mPassWordView = (EditText) findViewById(R.id.password_editor);
		mConfirmPassWordView = (EditText) findViewById(R.id.confirm_password_editor);
		mPhoneView.setOnFocusChangeListener(Common.ON_EDIT_FOCUS_CHANGE_LISTENER);
		mVerifyCodeView.setOnFocusChangeListener(Common.ON_EDIT_FOCUS_CHANGE_LISTENER);
		mPassWordView.setOnFocusChangeListener(Common.ON_EDIT_FOCUS_CHANGE_LISTENER);
		mConfirmPassWordView.setOnFocusChangeListener(Common.ON_EDIT_FOCUS_CHANGE_LISTENER);
		
		btnRegister = (Button) findViewById(R.id.btn_register);
		btnRegister.setOnClickListener(mStepClickListener);
		
		btnGetVerify = (Button) findViewById(R.id.get_verifycode);
		
		btnGetVerify.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPhone = mPhoneView.getText().toString();
				if (!ValidateUtil.isMobile(mPhone)) {
					//showToast(getString(R.string.account_register_mobile_error));
					return;
				}
				
				//submitUserExist();
			}
			
		});
	}
	
	private void submitRegister(int opType) {
		mPhone = mPhoneView.getText().toString();
		mPassword = mPassWordView.getText().toString();
    	
		xmppManager.setPassword(mPassword);
		xmppManager.setUsername(mPhone);
    	xmppManager.registerAccountHandler(handler);
    	
		notificationService.setXmppManager(xmppManager);
		notificationService.taskSubmitter.submit(new Runnable() {
            public void run() {
            	notificationService.register();
            }
        });

	}
	
	protected Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (LogUtil.IS_LOG)Log.i(TAG, "msg is " + msg.what);
			switch (msg.what) {
			case XmppConstants.REGISTER_SUCCESSFULLY:
				if (LogUtil.IS_LOG)Log.i(TAG, "register successfully ");
				saveUserInfo();
				showToast(getString(R.string.account_register_success));
				break;
			case XmppConstants.REGISTER_FAILED:
				if (LogUtil.IS_LOG)Log.i(TAG, "register failed ");
				showToast(getString(R.string.account_register_success));
				break;
			}
			}
	};
	
	private void startTimer() {
		mTimer = 60;
		String timer = this.getString(R.string.get_verifycode_prompt, mTimer);
		
		btnGetVerify.setText(timer);
		btnGetVerify.setEnabled(false);
		
		mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
	}
	
	private void stopTimer() {
		mHandler.removeMessages(MSG_UPDATE_TIME);
	}
	
	private boolean CheckValid() {
		mPhone = mPhoneView.getText().toString();
		mVerifyCode = mVerifyCodeView.getText().toString();
		mPassword = mPassWordView.getText().toString();
		mConfirmPassword = mConfirmPassWordView.getText().toString();
		
		if (!ValidateUtil.isMobile(mPhone)) {
			showToast(getString(R.string.account_register_mobile_error));
			return false;
		}
		
		if (TextUtils.isEmpty(mVerifyCode)) {
			showToast(getString(R.string.account_register_verifycode_error));
			return false;
		}
		
		if (TextUtils.isEmpty(mPassword)) {
			showToast(getString(R.string.account_register_password_error));
			return false;
		}
		
		if (TextUtils.isEmpty(mConfirmPassword)) {
			showToast(getString(R.string.account_register_confirmpassword_error));
			return false;
		}
		
		if (!mPassword.equals(mConfirmPassword)) {
			showToast(getString(R.string.account_register_confirmpassword_notunique));
			return false;
		}
		return true;
	}
	
}