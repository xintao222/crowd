package cn.com.zhenshiyin.crowd.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import cn.com.zhenshiyin.crowd.R;


public class Common {
	public interface onSimpleAlertDismiss {
		public void onSimpleAlertDismiss();
	}
	
	public static void showSimpleAlert(final Context context, final String message, final String title) {
		showSimpleAlert(context, message, title, null);
	}
	
	public static void showSimpleAlert(final Context context, final String message, final String title, final onSimpleAlertDismiss callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
				if (callback != null) {
					callback.onSimpleAlertDismiss();
				}
			}

		});
		
		builder.create().show();
	}
	
	// mOnEditFocusChangeListener
	public static final OnFocusChangeListener ON_EDIT_FOCUS_CHANGE_LISTENER = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			EditText textView = (EditText)v;
            String hint;
            if (hasFocus) {
                hint = textView.getHint().toString();
                textView.setTag(hint);
                textView.setHint("");
            } else {
            	String text = textView.getText().toString();
            	if (TextUtils.isEmpty(text)) {
            		hint = (String) textView.getTag();
            		if (!TextUtils.isEmpty(hint)) {
            			textView.setHint(hint);
            		}
            	}
            }
		}
		
	};
	
	public static final SmsMessage[] getMessagesFromIntent(
            Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }
}