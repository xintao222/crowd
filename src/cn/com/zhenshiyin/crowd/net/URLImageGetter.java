package cn.com.zhenshiyin.crowd.net;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.params.HttpParams;

import cn.com.zhenshiyin.crowd.util.SystemInfoUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;


public class URLImageGetter implements ImageGetter {
	Context context;
	TextView textView;

	public URLImageGetter(Context context, TextView textView) {
		this.context = context;
		this.textView = textView;
	}
	
	@Override
	public Drawable getDrawable(String paramString) {
		final URLDrawable urlDrawable = new URLDrawable(context);

		ImageGetterAsyncTask getterTask = new ImageGetterAsyncTask(urlDrawable);
		getterTask.execute(paramString);
		return urlDrawable;
	}
	
	public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
		URLDrawable urlDrawable;
		
		public ImageGetterAsyncTask(URLDrawable drawable) {
			this.urlDrawable = drawable;
		}
		
		@Override
		protected void onPostExecute(Drawable result) {
			if (result != null) {
				urlDrawable.drawable = result;
				
				URLImageGetter.this.textView.requestLayout();
			}
		}
		
		@Override
		protected Drawable doInBackground(String... params) {
			String source = params[0];
			return fetchDrawable(source);
		}
		
		public Drawable fetchDrawable(String url) {
			try {
				InputStream is = fetch(url);
				
				Rect bounds = SystemInfoUtils.getDefaultImageBounds(context);
				Bitmap bitmapOrg = BitmapFactory.decodeStream(is);
				
				if (bitmapOrg != null) {
					Bitmap bitmap = Bitmap.createScaledBitmap(bitmapOrg, bounds.right, bounds.bottom, true);
					
					BitmapDrawable drawable = new BitmapDrawable(bitmap);
					drawable.setBounds(bounds);
					
					// recycle the original bitmap.
					if (bitmapOrg != bitmap) {
						bitmapOrg.recycle();
					}
					
					return drawable;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}
		CookieSpecFactory csf = new CookieSpecFactory() {
            public CookieSpec newInstance(HttpParams params) {
                return new BrowserCompatSpec() { 
                    @Override
                    public void validate(Cookie cookie, CookieOrigin origin)
                    throws MalformedCookieException {
                    }
                };
            }
        };
		private InputStream fetch(String url) throws ClientProtocolException, IOException {
			DefaultHttpClient client = new DefaultHttpClient();
			 client.getCookieSpecs().register("easy", csf);
			 client.getParams().setParameter(ClientPNames.COOKIE_POLICY, "easy");
			// HttpClientParams.setCookiePolicy(client.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);
			HttpGet request = new HttpGet(url.trim());
			
			HttpResponse response = client.execute(request);
			return response.getEntity().getContent();
		}
	}
	
}