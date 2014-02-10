package cn.com.zhenshiyin.crowd.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;

public class ScreenUtil {

	//获取当前屏幕的截图 png
	public static Bitmap getScreen(Activity activity){
		View view = activity.getWindow().getDecorView();
		view.setDuplicateParentStateEnabled(true);
		view.buildDrawingCache();
		Bitmap bitmapSource = view.getDrawingCache();
		
		//获取titlebar的高度
		Rect rect = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
		int titalBar = rect.top;
		
		//获取整个屏幕的宽和高
		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		int height = activity.getWindowManager().getDefaultDisplay().getHeight();
		
		//生成320*480的图片
		Bitmap bitmap = Bitmap.createBitmap(bitmapSource, 0, titalBar, width, height-titalBar);
		bitmap = Bitmap.createScaledBitmap(bitmap, 320, 480, false);
		
		return bitmap;
	}
	
	//压缩并保存到sd卡上
	public static void saveSDcard(Bitmap bitmap,String filename){
		try {
			FileOutputStream outStream = new FileOutputStream(filename);
			if(outStream != null){
				bitmap.compress(Bitmap.CompressFormat.PNG, 50, outStream);
				outStream.flush();
				outStream.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void PictureSave(Activity activity){
		ScreenUtil.saveSDcard(getScreen(activity), getDateForName());
	}
	
	//生成图片名称
	public static String getDateForName(){
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		
		String str = year+""+month+""+day+""+hour+""+minute+""+second+".png";
		return str;
	}
	
}
