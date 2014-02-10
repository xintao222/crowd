package cn.com.zhenshiyin.crowd.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import cn.com.zhenshiyin.crowd.common.Constants;
import cn.com.zhenshiyin.crowd.common.MyApplication;


public class FileUtil {
	private static final String TAG = "FileUtil";
	
	public static boolean saveBookDetailToFile(String data, String name) {
		boolean retValue = false;
		
		String cacheDirPath = Constants.appInstance.getCacheDirPath(MyApplication.CACHE_SDCARD);
		
		
		if (cacheDirPath == null) {
			cacheDirPath = Constants.appInstance.getCacheDirPath(MyApplication.CACHE_INTERNAL);
		}
		
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[saveBookDetailToFile] cacheDirPath=" + cacheDirPath);
		if (cacheDirPath != null) {
			String fileName = cacheDirPath + name;
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "[saveBookDetailToFile] fileName=" + fileName);
			
			try {
				FileWriter writer = new FileWriter(fileName);
				writer.write(data);
				writer.close();
				retValue = true;
			} catch (IOException e) {
				retValue = false;
				e.printStackTrace();
				if (LogUtil.IS_LOG) LogUtil.d(TAG, "[saveBookDetailToFile] error=" + e);
			}
		}
		
		return retValue;
	}
	
	public static String generateDetailFileName(String id, int type) {
		return Constants.BOOK_DETAIL_PRIFIX + "_" + id + "_" + String.valueOf(type);
	}
	
	public static String getBookDetailFromFile(String name) {
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[getBookDetailFromFile] begin name=" + name);
		String cacheDirPath = Constants.appInstance.getCacheDirPath(MyApplication.CACHE_SDCARD);
		
		if (cacheDirPath == null) {
			cacheDirPath = Constants.appInstance.getCacheDirPath(MyApplication.CACHE_INTERNAL);
		}
		
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "cacheDirPath=" + cacheDirPath);
		if (cacheDirPath != null) {
			String fileName = cacheDirPath + name;
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "fileName=" + fileName);
			if (new File(fileName).exists()) {
				try {
					StringBuilder builder = new StringBuilder();
					FileInputStream is = new FileInputStream(fileName);
					BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8")); 
				
					String line ;
					while((line = br.readLine())!=null){
						builder.append(line);
					}
					
					return builder.toString();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					if (LogUtil.IS_LOG) LogUtil.d(TAG, "e=" + e);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					if (LogUtil.IS_LOG) LogUtil.d(TAG, "e=" + e);
				} catch (IOException e) {
					e.printStackTrace();
					if (LogUtil.IS_LOG) LogUtil.d(TAG, "e=" + e);
				}
			}
		}
		
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[getBookDetailFromFile] end with NULL");
		return null;
	}
	
}