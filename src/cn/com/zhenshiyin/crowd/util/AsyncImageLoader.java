package cn.com.zhenshiyin.crowd.util;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.com.zhenshiyin.crowd.R;
import cn.com.zhenshiyin.crowd.common.Constants;
import cn.com.zhenshiyin.crowd.common.MyApplication;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 异步图片下载器
 * 
 * @author huangyu
 * 
 */
public class AsyncImageLoader {
	private static final String TAG = AsyncImageLoader.class.getSimpleName();
	private static final int DEFAULT_SAMPLE_SIZE = 2;
	private static final int BUFFER_SIZE = 128 * 1024;
	
	//private static Map<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();
	private static final int cacheSize = 8 * 1024 * 1024;
	private static LruCache imageCache = new LruCache<String, Bitmap>(cacheSize) {
		protected int sizeOf(String key, Bitmap value) {
			// Since the getByteCount() is added in API level 12. So we use getRowBytes() * getHeight()
			// to replace.
			//return value.getByteCount();
			
			return value.getRowBytes() * value.getHeight();
		}
		
		protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "entryRemoved oldValue=" + oldValue);
			if (evicted) {
				oldValue.recycle();
			}
		}
	           
	};

	// static ThreadPool threadPool_image = new ThreadPool(2);
	public static ExecutorService pool = Executors.newFixedThreadPool(5);

	public static void clearImageCache() {
		synchronized(imageCache) {
		    imageCache.evictAll();
		}
	}

	public static Bitmap loadDrawable(final String imageUrl,
			final ImageCallback callback) {
		MyApplication app = Constants.appInstance;
		String cacheDir = app.getCacheDirPath(MyApplication.CACHE_SDCARD);
		String chacheInternal = app.getCacheDirPath(MyApplication.CACHE_INTERNAL);
		
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadDrawable] cacheDir=" + cacheDir);
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadDrawable] chacheInternal=" + chacheInternal);
		
		Bitmap drawable = null;
		// 1、从缓存中取bitmap
		synchronized(imageCache) {
		    drawable = (Bitmap) imageCache.get(imageUrl);
		}
		
		if (drawable != null) {
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadDrawable] load from cache");
			return drawable;
		}
		if (avaiableSdcard()) {// 如果有sd卡，读取sd卡
			drawable = getPicByPath(cacheDir, imageUrl);
			if (drawable != null) {
				if (LogUtil.IS_LOG) LogUtil.d(TAG, "get bitmap from sdcard!");
				synchronized(imageCache) {
				    imageCache.put(imageUrl, drawable);
				}
				return drawable;
			}
		} else {// 如果没有sd卡，读取手机里面
			drawable = getPicByPath(chacheInternal, imageUrl);
			if (drawable != null) {
				if (LogUtil.IS_LOG) LogUtil.d(TAG, "get bitmap from disk");
				synchronized(imageCache) {
				    imageCache.put(imageUrl, drawable);
				}
				return drawable;
			}
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (callback != null) {
					callback.imageLoaded((Bitmap) msg.obj, imageUrl);
				}
			}
		};
		Runnable task = new Runnable() {
			public void run() {
				Bitmap drawable = loadImageFromUrl(imageUrl);
				
				if (drawable != null) {
					handler.sendMessage(handler.obtainMessage(0, drawable));
				}
			};
		};

		// new Thread(task).start();
		pool.execute(task);

		return null;
	}

	public static Bitmap loadDrawable(final String imageUrl, final int requestWidth, final int requestHeight,
			final ImageCallback callback) {
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadDrawable] begin imageUrl=" + imageUrl);
		MyApplication app = Constants.appInstance;
		String cacheDir = app.getCacheDirPath(MyApplication.CACHE_SDCARD);
		String chacheInternal = app.getCacheDirPath(MyApplication.CACHE_INTERNAL);
		
		Bitmap drawable = null;
		// 1、从缓存中取bitmap
		synchronized(imageCache) {
		    drawable = (Bitmap) imageCache.get(imageUrl);
		}
		
		if (drawable != null) {
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadDrawable] load from cache");
			return drawable;
		}
		
		if (avaiableSdcard()) {// 如果有sd卡，读取sd卡
			drawable = getPicByPath(cacheDir+imageUrl, requestWidth, requestHeight);
			if (drawable != null) {
				if (LogUtil.IS_LOG) LogUtil.d(TAG, "get bitmap from sdcard!");
				synchronized(imageCache) {
				    imageCache.put(imageUrl, drawable);
				}
				return drawable;
			}
		} else {// 如果没有sd卡，读取手机里面
			drawable = getPicByPath(chacheInternal+imageUrl, requestWidth, requestHeight);
			if (drawable != null) {
				if (LogUtil.IS_LOG) LogUtil.d(TAG, "get bitmap from disk");
				synchronized(imageCache) {
				    imageCache.put(imageUrl, drawable);
				}
				return drawable;
			}
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (callback != null) {
					callback.imageLoaded((Bitmap) msg.obj, imageUrl);
				}
			}
		};
		Runnable task = new Runnable() {
			public void run() {
				Bitmap drawable = loadImageFromUrl(imageUrl, requestWidth, requestHeight);
				
				if (drawable != null) {
					handler.sendMessage(handler.obtainMessage(0, drawable));
				}
			};
		};

		// new Thread(task).start();
		pool.execute(task);

		return null;
	}
	
	public static Bitmap loadDrawable(final String imageUrl, final int requestWidth, final int requestHeight, final boolean fitXY,
			final ImageCallback callback) {
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadDrawable] begin");
		MyApplication app = Constants.appInstance;
		String cacheDir = app.getCacheDirPath(MyApplication.CACHE_SDCARD);
		String chacheInternal = app.getCacheDirPath(MyApplication.CACHE_INTERNAL);
		
		Bitmap drawable = null;
		// 1、从缓存中取bitmap
		synchronized(imageCache) {
		    drawable = (Bitmap) imageCache.get(imageUrl);
		}
		
		if (drawable != null) {
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadDrawable] load from cache");
			return drawable;
		}
		
		if (avaiableSdcard()) {// 如果有sd卡，读取sd卡
			drawable = getPicByPath(cacheDir+imageUrl, requestWidth, requestHeight);
			if (drawable != null) {
				if (LogUtil.IS_LOG) LogUtil.d(TAG, "get bitmap from sdcard!");
				synchronized(imageCache) {
				    imageCache.put(imageUrl, drawable);
				}
				return drawable;
			}
		} else {// 如果没有sd卡，读取手机里面
			drawable = getPicByPath(chacheInternal+imageUrl, requestWidth, requestHeight);
			if (drawable != null) {
				if (LogUtil.IS_LOG) LogUtil.d(TAG, "get bitmap from disk");
				synchronized(imageCache) {
				    imageCache.put(imageUrl, drawable);
				}
				return drawable;
			}
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (callback != null) {
					callback.imageLoaded((Bitmap) msg.obj, imageUrl);
				}
			}
		};
		Runnable task = new Runnable() {
			public void run() {
				Bitmap drawable = loadImageFromUrl(imageUrl, requestWidth, requestHeight, fitXY);
				handler.sendMessage(handler.obtainMessage(0, drawable));
			};
		};

		// new Thread(task).start();
		pool.execute(task);

		return null;
	}
	
	/**
	 * 判断是否存在sd卡
	 * 
	 * @return
	 */
	public static boolean avaiableSdcard() {
		String status = Environment.getExternalStorageState();

		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取图片
	 * 
	 * @param picName
	 * @return
	 */
	public static Drawable getPic_Draw_ByPath(String path, String picName) {
		picName = picName.substring(picName.lastIndexOf("/") + 1);
		String filePath = path + picName;
		return Drawable.createFromPath(filePath);
	}

	/**
	 * 获取图片
	 * 
	 * @param picName
	 * @return
	 */
	public static Bitmap getPicByPath(String path, String picName) {
		if (!"".equals(picName) && picName != null) {
			//picName = picName.substring(picName.lastIndexOf("/") + 1);
			String filePath = path + picName;

			// 判断文件是否存在
			File file = new File(filePath);
			if (!file.exists()) {// 文件不存在
				return null;
			}

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = DEFAULT_SAMPLE_SIZE;
			Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
			
			//if (LogUtil.IS_LOG && bitmap != null) LogUtil.d(TAG, "	[getPicByPath] bytes=" + bitmap.getByteCount());
			
			return bitmap;
		} else {
			return null;
		}

	}

	/**
     * Resize a bitmap to the specified width and height
     *
     * @param filename The filename
     * @param width The request width
     * @param height The request height
     *
     * @return The resized bitmap
     */
    private static Bitmap getPicByPath(String filename, int width, int height) {
    	// if file not exist, just return NULL.
    	File file = new File(filename);
		if (!file.exists()) {// 文件不存在
			return null;
		}
		
        final BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, dbo);

        final int nativeWidth = dbo.outWidth;
        final int nativeHeight = dbo.outHeight;
        if (LogUtil.IS_LOG) {
        	LogUtil.d(TAG, "[getPicByPath]: Input: " + nativeWidth + "x" + nativeHeight
                    + ", resize to: " + width + "x" + height);
        }

        final Bitmap srcBitmap;
        
        int sampleSize = makeSampleSize(nativeWidth, nativeHeight, width, height);
//        float bitmapWidth, bitmapHeight;
//        if (nativeWidth > width || nativeHeight > height) {
//            float dx = ((float)nativeWidth) / ((float)width);
//            float dy = ((float)nativeHeight) / ((float)height);
//
//            if (dx > dy) {
//                bitmapWidth = width;
//
//                if (((float)nativeHeight / dx) < (float)height) {
//                    bitmapHeight = (float)Math.ceil(nativeHeight / dx);
//                } else { // value equals the requested height
//                    bitmapHeight = (float)Math.floor(nativeHeight / dx);
//                }
//
//            } else {
//                if (((float)nativeWidth / dy) > (float)width) {
//                    bitmapWidth = (float)Math.floor(nativeWidth / dy);
//                } else { // value equals the requested width
//                    bitmapWidth = (float)Math.ceil(nativeWidth / dy);
//                }
//
//                bitmapHeight = height;
//            }
//
//            /**
//             *  Create the bitmap from file
//             */
//            int sampleSize = (int) Math.ceil(Math.max(
//                    (float) nativeWidth / bitmapWidth,
//                    (float) nativeHeight / bitmapHeight));
//            sampleSize = nextPowerOf2(sampleSize);
//            
//        } else {
//            bitmapWidth = width;
//            bitmapHeight = height;
//            srcBitmap = BitmapFactory.decodeFile(filename);
//        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        if (LogUtil.IS_LOG) LogUtil.d(TAG, "	[getPicByPath] sampleSize=" + sampleSize);
        srcBitmap = BitmapFactory.decodeFile(filename, options);
        //if (LogUtil.IS_LOG && srcBitmap != null) LogUtil.d(TAG, "	[getPicByPath] bytes=" + srcBitmap.getByteCount());
        
        return srcBitmap;
    }

    public static int nextPowerOf2(int n) {
        n -= 1;
        n |= n >>> 16;
        n |= n >>> 8;
        n |= n >>> 4;
        n |= n >>> 2;
        n |= n >>> 1;
        return n + 1;
    }
    
    private static int makeSampleSize(int nativeWidth, int nativeHeight, int width, int height) {
    	int sampleSize;
    	float bitmapWidth, bitmapHeight;
        if (nativeWidth > width || nativeHeight > height) {
            float dx = ((float)nativeWidth) / ((float)width);
            float dy = ((float)nativeHeight) / ((float)height);

            if (dx > dy) {
                bitmapWidth = width;

                if (((float)nativeHeight / dx) < (float)height) {
                    bitmapHeight = (float)Math.ceil(nativeHeight / dx);
                } else { // value equals the requested height
                    bitmapHeight = (float)Math.floor(nativeHeight / dx);
                }

            } else {
                if (((float)nativeWidth / dy) > (float)width) {
                    bitmapWidth = (float)Math.floor(nativeWidth / dy);
                } else { // value equals the requested width
                    bitmapWidth = (float)Math.ceil(nativeWidth / dy);
                }

                bitmapHeight = height;
            }

            /**
             *  Create the bitmap from file
             */
            sampleSize = (int) Math.ceil(Math.max(
                    (float) nativeWidth / bitmapWidth,
                    (float) nativeHeight / bitmapHeight));
            sampleSize = nextPowerOf2(sampleSize);
        } else {
        	sampleSize = 1;
        }
            
        return sampleSize;
    }
    
	public static Bitmap loadDrawable(String imageUrl) {
	    synchronized(imageCache) {
    		if (imageCache.snapshot().containsKey(imageUrl)) {
    //			SoftReference<Bitmap> sf = imageCache.get(imageUrl);
    //			Bitmap drawable = sf != null ? sf.get() : null;
    			Bitmap drawable = (Bitmap) imageCache.get(imageUrl);
    			if (drawable != null) {
    				return drawable;
    			} else {
    				return null;
    			}
    		}
	    }
		return null;
	}

	protected static Bitmap loadImageFromUrl(String imageUrl) {
		try {
			URL url = new URL(imageUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10 * 1000);
			conn.connect();
			
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			}
			
			InputStream inputStream = conn.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
	        if(null != inputStream){
	        	inputStream.close();
	        	inputStream = null;
	        }
			conn.disconnect();
			if (bitmap != null) {
				LogUtil.d("from net", "get bitmap from net");
				synchronized(imageCache) {
				    imageCache.put(imageUrl, bitmap);
				}
				savePic(bitmap, imageUrl);// 保存图片
			}
			
			//if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadImageFromUrl] size=" + (bitmap != null ? bitmap.getByteCount() : 0));
			return bitmap;
		} catch (Exception e) {
			return null;
		}
	}

	/**
     * Returns an immutable bitmap from subset of source bitmap transformed by the given matrix.
     */
    public static Bitmap createBitmap(Bitmap bitmap, Matrix m) {
        // TODO: Re-implement createBitmap to avoid ARGB -> RBG565 conversion on platforms
        // prior to honeycomb.
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }
    
	protected static Bitmap loadImageFromUrl(String imageUrl, int requestWidth, int requestHeight) {
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadImageFromUrl] begin");
		
		try {
			URL url = new URL(imageUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10 * 1000);
			conn.connect();
			
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			}
			
			InputStream inputStream = conn.getInputStream();
			
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
			//if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadImageFromUrl] bitmap size=" + (bitmap != null ? bitmap.getByteCount() : 0));
			
			// Scale down the sampled bitmap if it's still larger than the desired dimension.
	        if (bitmap != null) {
	            float scale = Math.min((float) requestWidth / bitmap.getWidth(),
	                    (float) requestHeight / bitmap.getHeight());
	            scale = Math.max(scale, Math.min((float) requestHeight / bitmap.getWidth(),
	                    (float) requestWidth / bitmap.getHeight()));
	            
	            float scaleX = (float) requestWidth / bitmap.getWidth();
	            float scaleY = (float) requestHeight / bitmap.getHeight();
	            if (scaleX < 1 || scaleY < 1) {
	                Matrix m = new Matrix();
	                //m.setScale(scale, scale);
	                m.setScale(scaleX, scaleY);
	                Bitmap transformed = createBitmap(bitmap, m);
	                if (LogUtil.IS_LOG) LogUtil.d(TAG, "bitmap=" + bitmap);
	                if (LogUtil.IS_LOG) LogUtil.d(TAG, "transformed=" + transformed);
	                
	                if (transformed != bitmap) {
		                bitmap.recycle();
		                bitmap = transformed;
	                }
	            }
	        }
	        
	        if(null != inputStream){
	        	inputStream.close();
	        	inputStream = null;
	        }
			conn.disconnect();
			if (bitmap != null) {
				LogUtil.d("from net", "get bitmap from net");
				synchronized(imageCache) {
				    imageCache.put(imageUrl, bitmap);
				}
				savePic(bitmap, imageUrl);// 保存图片
			}
			
			//if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadImageFromUrl] size=" + (bitmap != null ? bitmap.getByteCount() : 0));
			return bitmap;
		} catch (Exception e) {
			return null;
		}
	}
	
	protected static Bitmap loadImageFromUrl(String imageUrl, int requestWidth, int requestHeight, boolean fitXY) {
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadImageFromUrl] begin fitXY=" + fitXY);
		
		try {
			URL url = new URL(imageUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10 * 1000);
			conn.connect();
			InputStream inputStream = conn.getInputStream();
			
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
			//if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadImageFromUrl] bitmap size=" + (bitmap != null ? bitmap.getByteCount() : 0));
			
			// Scale down the sampled bitmap if it's still larger than the desired dimension.
	        if (bitmap != null) {
	            float scaleX = (float) requestWidth / bitmap.getWidth();
	            float scaleY = (float) requestHeight / bitmap.getHeight();
	            if (fitXY) {
	                Matrix m = new Matrix();
	                //m.setScale(scale, scale);
	                m.setScale(scaleX, scaleY);
	                Bitmap transformed = createBitmap(bitmap, m);
	                
	                if (LogUtil.IS_LOG) LogUtil.d(TAG, "bitmap=" + bitmap);
	                if (LogUtil.IS_LOG) LogUtil.d(TAG, "transformed=" + transformed);
	                
	                if (transformed != bitmap) {
		                bitmap.recycle();
		                bitmap = transformed;
	                }
	            } else if (scaleX < 1 || scaleY < 1) {
	            	Matrix m = new Matrix();
	                //m.setScale(scale, scale);
	                m.setScale(scaleX, scaleY);
	                Bitmap transformed = createBitmap(bitmap, m);
	                
	                if (LogUtil.IS_LOG) LogUtil.d(TAG, "bitmap=" + bitmap);
	                if (LogUtil.IS_LOG) LogUtil.d(TAG, "transformed=" + transformed);
	                
	                if (transformed != bitmap) {
		                bitmap.recycle();
		                bitmap = transformed;
	                }
	            }
	        }
	        
	        if(null != inputStream){
	        	inputStream.close();
	        	inputStream = null;
	        }
			conn.disconnect();
			if (bitmap != null) {
				LogUtil.d("from net", "get bitmap from net");
				synchronized(imageCache) {
				    imageCache.put(imageUrl, bitmap);
				}
				savePic(bitmap, imageUrl);// 保存图片
			}
			
			//if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadImageFromUrl] size=" + (bitmap != null ? bitmap.getByteCount() : 0));
			return bitmap;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static void savePic(Bitmap bitmap, String imageUrl) {
		if (bitmap != null && imageUrl != null && !"".equals(imageUrl)) {
			if (avaiableSdcard()) {// 如果有sd卡，保存在sd卡
				savePicToSdcard(bitmap, imageUrl);
			} else {// 如果没有sd卡，保存在手机里面
				saveToDataDir(bitmap, imageUrl);
			}
		}
	}

	/**
	 * 将图片保存在sd卡
	 * 
	 * @param bitmap
	 *            图片
	 * @param picName
	 *            图片名称（同新闻id名）
	 */
	private static void savePicToSdcard(Bitmap bitmap, String picName) {
		MyApplication app = Constants.appInstance;
		String cacheDir = app.getCacheDirPath(MyApplication.CACHE_SDCARD);
		
		//picName = picName.substring(picName.lastIndexOf("/") + 1);
		File file = new File(cacheDir + picName);
		FileOutputStream outputStream;
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
				outputStream = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
				if(null != outputStream){
					outputStream.close();
					outputStream = null;
				}
			} catch (Exception e) {
				// Log.e("", e.toString());
			}
		}
	}

	/**
	 * 保存文件到应用目录
	 * 
	 * @param bitmap
	 * @param fileName
	 *            文件名称
	 */
	static void saveToDataDir(Bitmap bitmap, String fileName) {
		MyApplication app = Constants.appInstance;
		String chacheInternal = app.getCacheDirPath(MyApplication.CACHE_INTERNAL);
		
		//fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
		File file = new File(chacheInternal + fileName);
		FileOutputStream outputStream;
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
				outputStream = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
				outputStream.close();
			} catch (Exception e) {
				Log.e("", e.toString());
			}
		}
	}

	public interface ImageCallback {
		public void imageLoaded(Bitmap imageDrawable, String imageUrl);
	}

	/**
	 * @Title :deleteFile
	 * @Description :TODO
	 * @params @param file
	 * @return void
	 * 
	 */
	public static  void deleteFile(File file) {
		if (file.exists()) { // 判断文件是否存在
			if (file.isFile()) { // 判断是否是文件
				file.delete(); // delete()方法 你应该知道 是删除的意思;
			} else if (file.isDirectory()) { // 否则如果它是一个目录
				File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
				for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
					deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
				}
			}
			file.delete();
		}
	}
	
	public static void loadAndSaveFile(final Context context, final String url, final String path) {
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadAndSaveFile] parent=" + path);
		if (isFileExist(path, url)) {
			Toast.makeText(context.getApplicationContext(), "download_finished_with_file_already_exists", Toast.LENGTH_SHORT).show();
			return;
		}
		
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				boolean success = (Boolean) msg.obj;
				
				if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadAndSaveFile] success=" + success);
				if (success) {
					String name = url.substring(url.lastIndexOf("/") + 1);
					String filePath = path + File.separator + name;
					
					File picture = new File(filePath);
					if (picture.exists()) {
						Uri result = Uri.fromFile(picture);
						
						if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadAndSaveFile] result=" + result);
						
						ContentValues values = new ContentValues(2);
						values.put(ImageColumns.DISPLAY_NAME, name);
						values.put(ImageColumns.DATA, filePath);
						context.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
						
//						Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result);
//						intent.setDataAndType(result, "image/*");
//						context.sendBroadcast(intent);
					}
					Toast.makeText(context.getApplicationContext(), "download_finished",  Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context.getApplicationContext(), "download_failed", Toast.LENGTH_SHORT).show();
				}
			}
		};
		
		Runnable task = new Runnable() {
			public void run() {
				boolean success = loadAndSaveFileFromUrl(url, path);
				handler.sendMessage(handler.obtainMessage(0, success));
			};
		};

		new Thread(task).start();
		//pool.execute(task);
	}
	
	protected static boolean loadAndSaveFileFromUrl(String fileUrl, String path) {
		File dir = new File(path);
		if (!dir.exists()) {
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadAndSaveFileFromUrl] create parent directory =" + path);
			dir.mkdirs();
		}
		
		String name = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
		String filePath = path + File.separator + name;
		
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[loadAndSaveFileFromUrl] filePath=" + filePath);
		try {
			URL url = new URL(fileUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10 * 1000);
			conn.connect();
			InputStream inputStream = conn.getInputStream();
			FileOutputStream outputStream = new FileOutputStream(filePath);
			
	        if(null != inputStream){
	        	byte[] buffer = new byte[8192];
				int count = 0;
				
				// 开始复制db文件
				while ((count = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, count);
				}
				
				outputStream.close();
				inputStream.close();
	        }
			conn.disconnect();

			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean isFileExist(String path, String picName) {
		if (!"".equals(picName) && picName != null) {
			picName = picName.substring(picName.lastIndexOf("/") + 1);
			String filePath = path + File.separator + picName;

			// 判断文件是否存在
			File file = new File(filePath);
			if (!file.exists()) {// 文件不存在
				return false;
			}

			return true;
		} else {
			return false;
		}

	}
	
	public static void AsyncSetImage(final ImageView view, final String url, final int width, final int height) {
		if(StringUtil.isNotEmpty(url)){
			Bitmap bitmap = AsyncImageLoader.loadDrawable(url, width, height, new ImageCallback() {
				@Override
				public void imageLoaded(Bitmap imageDrawable, String imageUrl) {
					if(imageDrawable!=null && view!=null){
						view.setImageBitmap(imageDrawable);
					}
				}
			});
			
			if (bitmap != null) {
				view.setImageBitmap(bitmap);
			} else {
				view.setImageResource(R.drawable.default_image_min);
			}
		}
	}
}
