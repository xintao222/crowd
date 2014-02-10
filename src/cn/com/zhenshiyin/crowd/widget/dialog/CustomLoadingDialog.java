package cn.com.zhenshiyin.crowd.widget.dialog;



import java.util.concurrent.TimeUnit;

import cn.com.zhenshiyin.crowd.R;
import cn.com.zhenshiyin.crowd.common.Constants;
import cn.com.zhenshiyin.crowd.net.DefaultThreadPool;
import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.util.StringUtil;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.TextView;

public class CustomLoadingDialog extends Dialog{
	TextView loading_text;
	static int theme = R.style.custom_dialog;
	String content = "";
	boolean isHideCloseBtn = false;
	public CustomLoadingDialog(Context context,String content,boolean isHideCloseBtn) {
	    super(context,theme);
	    this.content = content;
	    this.isHideCloseBtn = isHideCloseBtn;
		LogUtil.d("TAG","****************************");
	}

	 protected void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		 //去掉标题
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
		 //设置view样式
		 setContentView(R.layout.custom_loading_dialog);	
		 loading_text= (TextView) findViewById(R.id.loading_text);
		 if(!TextUtils.isEmpty(content)){
			 loading_text.setText(content);
		 }
	 }
	 //called when this dialog is dismissed
	 protected void onStop() {
		 LogUtil.d("onStop()","this dialog is dismissed");
		 super.onStop();
	 }
	 @Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
		 LogUtil.d("show()","this dialog is shown");
		 Constants.IS_STOP_REQUEST = false;
		 
	}
	 @Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
		 LogUtil.d("dismiss()","this dialog is dismissed");
		 try{
			 DefaultThreadPool.pool.awaitTermination(1, TimeUnit.MICROSECONDS);
		 }catch (Exception e) {
			 LogUtil.d("awaitTermination","awaitTermination");
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		 LogUtil.d("onStart()","this dialog is shown");
	}
	 
	 

	 
}
