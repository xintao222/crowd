/*
 * Copyright (C) 2010 The MobileSecurePay Project
 * All right reserved.
 * author: shiqun.shi@alipay.com
 */

package cn.com.zhenshiyin.crowd.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import android.app.AlertDialog;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.KeyEvent;

public class BaseHelper
{
	public static String convertStreamToString(InputStream is)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try
		{
			while ((line = reader.readLine()) != null)
			{
				sb.append(line);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	public static void showDialog(Activity context,String strTitle ,String strText,int icon)
	{
		AlertDialog.Builder tDialog = new AlertDialog.Builder(context);
		tDialog.setIcon(icon);
		tDialog.setTitle(strTitle);
		tDialog.setMessage(strText);
		tDialog.setPositiveButton("确定", null);
		tDialog.show();
	}
	
	public static void log(String tag, String info)
	{
		Log.d(tag, info);
	}	
	
	public static void chmod(String permission, String path)
	{
		try
			{
				String command 	= "chmod " + permission + " " + path;
				Runtime runtime = Runtime.getRuntime();
				runtime.exec(command);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
	}
	
	//
	// show the progress bar.
	public static ProgressDialog showProgress(Context context,
			CharSequence title, CharSequence message, boolean indeterminate,
			boolean cancelable)
	{
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setIndeterminate(indeterminate);
		dialog.setCancelable(false);
		//dialog.setDefaultButton(false);
		dialog.setOnCancelListener( new AlixOnCancelListener( (Activity)context ) );

		dialog.show();
		return dialog;
	}
	static class AlixOnCancelListener implements DialogInterface.OnCancelListener
	{
		Activity mcontext;
		AlixOnCancelListener(Activity context)
		{
			mcontext = context;
		}
		
		 public void onCancel(DialogInterface dialog)
		{
			mcontext.onKeyDown(KeyEvent.KEYCODE_BACK, null);
		}	
	}	
}