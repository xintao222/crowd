package cn.com.zhenshiyin.crowd.net;

import cn.com.zhenshiyin.crowd.R;
import cn.com.zhenshiyin.crowd.util.SystemInfoUtils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class URLDrawable extends BitmapDrawable {
	protected Drawable drawable;
	
	public URLDrawable(Context context) {
		this.setBounds(SystemInfoUtils.getDefaultImageBounds(context));
		
		drawable = context.getResources().getDrawable(R.drawable.default_image_min);
		drawable.setBounds(SystemInfoUtils.getDefaultImageBounds(context));
	}
	
	@Override
	public void draw(Canvas canvas) {
		//Log.d("URLDrawable", "this=" + this.getBounds());
		if (drawable != null) {
			//Log.d("URLDrawable", "draw=" + drawable.getBounds());
			drawable.draw(canvas);
			drawable.invalidateSelf();
			
		}
	}
	
}