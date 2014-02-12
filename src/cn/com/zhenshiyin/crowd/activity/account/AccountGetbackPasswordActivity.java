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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.com.zhenshiyin.crowd.R;
import cn.com.zhenshiyin.crowd.base.BaseActivity;
import cn.com.zhenshiyin.crowd.common.Common;
import cn.com.zhenshiyin.crowd.common.Common.onSimpleAlertDismiss;
import cn.com.zhenshiyin.crowd.common.Constants;
import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.util.ValidateUtil;


public class AccountGetbackPasswordActivity extends BaseActivity {
	private static final String TAG = "AccountGetbackPasswordActivity";
	
	private static final int STEP_INPUT_PHONENUMBER = 1;
	private static final int STEP_INPUT_NEW_PASSWORD = 2;
	
	private static final int OPT_GET_VERIFYCODE = 4;
	private static final int OPT_GETBACK_PASSWORD = 5;
	
	private static final int MSG_UPDATE_TIME = 2000;
	
	private EditText mPhoneView;
	private TextView mPromptView;
	private EditText mVerifyCodeView;
	private EditText mPasswordView;
	private EditText mConfirmPasswordView;
	private Button mBtnRegister;
	private TextView mBtnGetVerifyCodeView;
	
	private int mStep = STEP_INPUT_PHONENUMBER;
	private int mTimer = 60;
	
	private String mPhoneNumber;
	private String mVerifyCode;
	private String mNewPassword;
	private String mConfirmPassword;
	private String mUserId;
	
	private OnClickListener mRegisterClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (CheckValid()) {
				submitRegister(OPT_GETBACK_PASSWORD);
			}
		}
		
	};
	
	private OnClickListener mGetVerifyCodeListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mPhoneNumber = mPhoneView.getText().toString();
			if (!ValidateUtil.isMobile(mPhoneNumber)) {
				showToast(getString(R.string.account_register_mobile_error));
				
				return;
			}
			
			submitRegister(OPT_GET_VERIFYCODE);
		}
		
	};
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "msg=" + msg.what + "; mTimer=" + mTimer);
			mHandler.removeMessages(MSG_UPDATE_TIME);
			
			mTimer--;
			if (mTimer > 0) {
				String timer = getString(R.string.get_verifycode_prompt, mTimer);
				mBtnGetVerifyCodeView.setText(timer);
				
				sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
			} else if (mTimer == 0) {
				mBtnGetVerifyCodeView.setText(R.string.get_verifycode);
				mBtnGetVerifyCodeView.setEnabled(true);
			}
			
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "Timer handler end...................................");
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
		
		setContentView(R.layout.account_getback_password);
		
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
		case OPT_GETBACK_PASSWORD:

			break;
		}
	}
	
	private void saveUserInfo() {
		SharedPreferences preference = getSharedPreferences("account",MODE_PRIVATE);

		Editor edit = preference.edit();
		edit.putString("name", mPhoneNumber);
		edit.putString("password", mNewPassword);
		edit.putString("userID", mUserId);
		edit.commit();
	}
	
	private void initViews() {
		// init title
		((TextView)findViewById(R.id.title_content)).setText(getTitle());
		
		// init views.
		mPhoneView = (EditText) findViewById(R.id.phone_editor);
		mPromptView = (TextView) findViewById(R.id.prompt);
		mVerifyCodeView = (EditText) findViewById(R.id.verifycode_editor);
		mPasswordView = (EditText) findViewById(R.id.password_editor);
		mConfirmPasswordView = (EditText) findViewById(R.id.confirm_password_editor);
		mBtnRegister = (Button) findViewById(R.id.btn_register);
		mBtnGetVerifyCodeView = (TextView) findViewById(R.id.get_verifycode);
		
		// Set focus change listener.
		mPhoneView.setOnFocusChangeListener(Common.ON_EDIT_FOCUS_CHANGE_LISTENER);
		mVerifyCodeView.setOnFocusChangeListener(Common.ON_EDIT_FOCUS_CHANGE_LISTENER);
		mPasswordView.setOnFocusChangeListener(Common.ON_EDIT_FOCUS_CHANGE_LISTENER);
		mConfirmPasswordView.setOnFocusChangeListener(Common.ON_EDIT_FOCUS_CHANGE_LISTENER);
		
		// Set click listener.
		mBtnRegister.setOnClickListener(mRegisterClickListener);
		mBtnGetVerifyCodeView.setOnClickListener(mGetVerifyCodeListener);
		
	}
	
	private void startTimer() {
		mTimer = 60;
		String timer = this.getString(R.string.get_verifycode_prompt, mTimer);
		mBtnGetVerifyCodeView.setText(timer);
		
		mBtnGetVerifyCodeView.setEnabled(false);
		
		mHandler.sendEmptyMessage(MSG_UPDATE_TIME);
	}
	
	private void stopTimer() {
		mHandler.removeMessages(MSG_UPDATE_TIME);
	}
	

	private void submitRegister(int opType) {
	
		
	}
	
	private boolean CheckValid() {
		mPhoneNumber = mPhoneView.getText().toString().trim();
		mVerifyCode = mVerifyCodeView.getText().toString().trim();
		mNewPassword = mPasswordView.getText().toString().trim();
		mConfirmPassword = mConfirmPasswordView.getText().toString().trim();
		
		if (!ValidateUtil.isMobile(mPhoneNumber)) {
			showToast(getString(R.string.account_register_mobile_error));
			return false;
		}
		
		if (TextUtils.isEmpty(mVerifyCode)) {
			showToast(getString(R.string.account_register_verifycode_error));
			return false;
		}
		
		if (TextUtils.isEmpty(mNewPassword)) {
			showToast(getString(R.string.account_register_password_error));
			return false;
		}
		
		if (TextUtils.isEmpty(mConfirmPassword)) {
			showToast(getString(R.string.account_register_confirmpassword_error));
			return false;
		}
		
		if (!mNewPassword.equals(mConfirmPassword)) {
			showToast(getString(R.string.account_register_confirmpassword_notunique));
			return false;
		}
		return true;
	}
	
}