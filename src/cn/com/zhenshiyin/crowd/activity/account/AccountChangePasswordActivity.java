package cn.com.zhenshiyin.crowd.activity.account;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.zhenshiyin.crowd.R;
import cn.com.zhenshiyin.crowd.base.BaseActivity;
import cn.com.zhenshiyin.crowd.common.Common;
import cn.com.zhenshiyin.crowd.common.Constants;
import cn.com.zhenshiyin.crowd.common.Common.onSimpleAlertDismiss;
import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.util.StringUtil;


public class AccountChangePasswordActivity extends BaseActivity {
	private static final String TAG = "AccountChangePasswordActivity";
	
	private static final int OPT_CHANGE_PASSWORD = 1;
	
	private TextView error;
	private EditText passWordView;
	private EditText newPassWordView;
	private EditText confirmNewPassWordView;
	
	private String mUserId;
	private String mUserName;
	private String mPassword;
	private String mNewPassword;
	private String mConfirmNewPassword;
	
	private onSimpleAlertDismiss mOnAlertDismissListener = new onSimpleAlertDismiss() {
		@Override
		public void onSimpleAlertDismiss() {
			finish();
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.account_change_password);
		
		initViews();
		
		getUserInfo();
	}

	@Override
	public void onWebServiceCallback(String resultJson, int requestCode) {
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "resultJson=" + resultJson);
		
		switch(requestCode) {
		case OPT_CHANGE_PASSWORD:

			break;
		}
	}
	
	private void saveUserInfo() {
		SharedPreferences preference = getSharedPreferences("account",MODE_PRIVATE);

		Editor edit = preference.edit();
		edit.putString("name", mUserName);
		edit.putString("userID",mUserId);
		edit.putString("password", mNewPassword);
		edit.commit();
	}
	
	private void initViews() {
		// Init title.
		TextView title = (TextView) findViewById(R.id.title_content);
		title.setText(R.string.account_change_password);
		
		error = (TextView) findViewById(R.id.error);
		passWordView = (EditText) findViewById(R.id.password);
		passWordView.setOnFocusChangeListener(Common.ON_EDIT_FOCUS_CHANGE_LISTENER);
		
		newPassWordView = (EditText) findViewById(R.id.new_password);
		newPassWordView.setOnFocusChangeListener(Common.ON_EDIT_FOCUS_CHANGE_LISTENER);
		
		confirmNewPassWordView = (EditText) findViewById(R.id.confirm_password);
		confirmNewPassWordView.setOnFocusChangeListener(Common.ON_EDIT_FOCUS_CHANGE_LISTENER);
	}
	
	private void getUserInfo() {
		SharedPreferences preference = getSharedPreferences("account", this.MODE_PRIVATE);
		mUserName = preference.getString("name","");
		mUserId = preference.getString("userID","");
	}
	
	private String buildChangePasswordHeader() {


		return "";
	}
	
	private String buildChangePasswordBody() {

		
		return "";
	}
	
	private void submitChangePassword() {
	}
	
	public void change_password(View view) {
		mPassword = passWordView.getText().toString().trim();
		mNewPassword = newPassWordView.getText().toString().trim();
		mConfirmNewPassword = confirmNewPassWordView.getText().toString().trim();
		
		if (TextUtils.isEmpty(mPassword)) {
			error.setVisibility(View.VISIBLE);
			error.setText(R.string.account_change_password_origant_hint);
			return;
		}
		
		if (TextUtils.isEmpty(mNewPassword)) {
			error.setVisibility(View.VISIBLE);
			error.setText(R.string.account_change_password_new_hint);
			return;
		}
		
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[change_password] mPassword=" + mPassword + "; mNewPassword=" + mNewPassword );
		if(!mConfirmNewPassword.equals(mNewPassword)) {
			error.setVisibility(View.VISIBLE);
			error.setText(R.string.account_newp_password_confirm_error);
			return;
		}
		
		submitChangePassword();
	
	}

}