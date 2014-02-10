package cn.com.zhenshiyin.crowd.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import cn.com.zhenshiyin.crowd.common.Constants;
import cn.com.zhenshiyin.crowd.net.exception.RequestException;
import cn.com.zhenshiyin.crowd.net.utils.ErrorUtil;
import cn.com.zhenshiyin.crowd.net.utils.RequestParameter;
import cn.com.zhenshiyin.crowd.net.utils.Utils;
import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.widget.dialog.CustomLoadingDialog;


/**
 *
 * 
 * @author zxy
 *
 */
public class AsyncHttpGet extends BaseRequest{
	private static final String TAG = "AsyncHttpGet";
	
	private static final long serialVersionUID = 2L;
	DefaultHttpClient httpClient;
	List<RequestParameter> parameter;
	CustomLoadingDialog customLoadingDialog;
	
//	static Handler resultHandler = new Handler() {
//		public void handleMessage(Message msg) {
//			String resultData = (String) msg.obj;
//			if(!resultData.contains("ERROR.HTTP.008")){
//				ThreadCallBack callBack = (ThreadCallBack) msg.getData()
//				.getSerializable("callback");
//				callBack.onCallbackFromThread(resultData);
//			}
//		}
//	};
	ThreadCallBack callBack;
	public AsyncHttpGet(ThreadCallBack callBack,String url,List<RequestParameter> parameter,boolean isShowLoadingDialog,String loadingCode,boolean isHideCloseBtn){
		this.callBack = callBack;
		if(isShowLoadingDialog){
			
			customLoadingDialog = new  CustomLoadingDialog((Context)callBack,"",isHideCloseBtn);
			if(customLoadingDialog!=null &&!customLoadingDialog.isShowing()){
				customLoadingDialog.show();
			}
		}
		this.url = url;
		this.parameter = parameter;
		if(httpClient == null)
			httpClient = new DefaultHttpClient();
	}
	
	public AsyncHttpGet(ThreadCallBack callBack,String url,List<RequestParameter> parameter,boolean isShowLoadingDialog,String loadingCode,boolean isHideCloseBtn, int requestCode){
		this(callBack, url, parameter, isShowLoadingDialog, loadingCode, isHideCloseBtn);
		this.requestCode = requestCode;
	}
	
	public AsyncHttpGet(ThreadCallBack callBack, String url,List<RequestParameter> parameter,boolean isShowLoadingDialog,int connectTimeout,int readTimeout) {
		this(callBack,url,parameter,isShowLoadingDialog,"",false);
		if(connectTimeout>0){
			this.connectTimeout = connectTimeout;
		}
		if(readTimeout>0){
			this.readTimeout = readTimeout;
		}
	}
	public AsyncHttpGet(ThreadCallBack callBack, String url,List<RequestParameter> parameter,boolean isShowLoadingDialog,String loadingDialogContent,boolean isHideCloseBtn,int connectTimeout,int readTimeout) {
		this(callBack,url,parameter,isShowLoadingDialog,loadingDialogContent,isHideCloseBtn);
		if(connectTimeout>0){
			this.connectTimeout = connectTimeout;
		}
		if(readTimeout>0){
			this.readTimeout = readTimeout;
		}
	}
	@Override
	public void run() {
		String ret = "";
		 try{
			 if(parameter!=null&&parameter.size()>0){
				 StringBuilder bulider  = new StringBuilder();
				 for(RequestParameter p:parameter){
					 if(bulider.length()!=0){
						 bulider.append("&");
					 }
					 
					 bulider.append(Utils.encode(p.getName()));
					 bulider.append("=");
					 bulider.append(Utils.encode(p.getValue()));
				 } 
				 url += "?"+bulider.toString();
			 }
			 if (LogUtil.IS_LOG) LogUtil.d(TAG,
						"AsyncHttpGet  request to url :" + url);
			 request = new HttpGet(url);
			 /*if(Constants.isGzip){
				 	request.addHeader("Accept-Encoding", "gzip");
				}else{
					 request.addHeader("Accept-Encoding", "default");
				}*/
			// 请求超时 
			 httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeout); 
			// 读取超时 
			 httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, readTimeout);
			 HttpResponse response = httpClient.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			if (LogUtil.IS_LOG) LogUtil.d(TAG,"statusCode=" + statusCode);
			if (statusCode == HttpStatus.SC_OK) {
				 InputStream is = response.getEntity().getContent();
				 BufferedInputStream bis = new BufferedInputStream(is);
		            bis.mark(2);
		            // 取前两个字节
		            byte[] header = new byte[2];
		            int result = bis.read(header);
		            // reset输入流到开始位置
		            bis.reset();
		            // 判断是否是GZIP格式
		            int headerData = getShort(header);
		            // Gzip 流 的前两个字节是 0x1f8b
		            if (result != -1 && headerData == 0x1f8b) {
		            	if (LogUtil.IS_LOG) LogUtil.d(TAG, " use GZIPInputStream  ");
		                is = new GZIPInputStream(bis);
		            } else {
		            	if (LogUtil.IS_LOG) LogUtil.d(TAG, " not use GZIPInputStream");
		                is = bis;
		            }
		            InputStreamReader reader = new InputStreamReader(is, "utf-8");
		            char[] data = new char[100];
		            int readSize;
		            StringBuffer sb = new StringBuffer();
		            while ((readSize = reader.read(data)) > 0) {
		                sb.append(data, 0, readSize);
		            }
		            ret = sb.toString();
		            bis.close();
		            reader.close();

//				ByteArrayOutputStream content = new ByteArrayOutputStream();
//				response.getEntity().writeTo(content);
//				ret = new String(content.toByteArray()).trim();
//				content.close();
			}else{
				RequestException exception = new RequestException(RequestException.IO_EXCEPTION,"响应码异常,响应码："+ statusCode);
				ret = ErrorUtil.errorJson("ERROR.HTTP.001", exception.getMessage());
			}
			
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "AsyncHttpGet  request to url :"+url+"  finished !");
			 
		}catch(java.lang.IllegalArgumentException e){
			
			RequestException exception = new RequestException(
					RequestException.IO_EXCEPTION, Constants.ERROR_MESSAGE);
			ret = ErrorUtil.errorJson("ERROR.HTTP.002", exception.getMessage());
			if (LogUtil.IS_LOG) LogUtil.d(TAG,
					"AsyncHttpGet  request to url :" + url + "  onFail  "
							+ e.getMessage());
		}  catch (org.apache.http.conn.ConnectTimeoutException e) {
			RequestException exception = new RequestException(
					RequestException.SOCKET_TIMEOUT_EXCEPTION, Constants.ERROR_MESSAGE);
			ret = ErrorUtil.errorJson("ERROR.HTTP.003", exception.getMessage());
			if (LogUtil.IS_LOG) LogUtil.d(TAG,
					"AsyncHttpGet  request to url :" + url + "  onFail  "
							+ e.getMessage());
		} catch (java.net.SocketTimeoutException e) {
			RequestException exception = new RequestException(
					RequestException.SOCKET_TIMEOUT_EXCEPTION, Constants.ERROR_MESSAGE);
			ret = ErrorUtil.errorJson("ERROR.HTTP.004", exception.getMessage());
			if (LogUtil.IS_LOG) LogUtil.d(TAG,
					"AsyncHttpGet  request to url :" + url + "  onFail  "
							+ e.getMessage());
		} catch (UnsupportedEncodingException e) {
			RequestException exception = new RequestException(
					RequestException.UNSUPPORTED_ENCODEING_EXCEPTION, "编码错误");
			ret = ErrorUtil.errorJson("ERROR.HTTP.005", exception.getMessage());
			if (LogUtil.IS_LOG) LogUtil.d(TAG,
					"AsyncHttpGet  request to url :" + url + "  UnsupportedEncodingException  "
							+ e.getMessage());
		} catch (org.apache.http.conn.HttpHostConnectException e) {
			RequestException exception = new RequestException(
					RequestException.CONNECT_EXCEPTION, Constants.ERROR_MESSAGE);
			ret = ErrorUtil.errorJson("ERROR.HTTP.006", exception.getMessage());
			if (LogUtil.IS_LOG) LogUtil.d(TAG,
					"AsyncHttpGet  request to url :" + url + "  HttpHostConnectException  "
							+ e.getMessage());
		} catch (ClientProtocolException e) {
			RequestException exception = new RequestException(
					RequestException.CLIENT_PROTOL_EXCEPTION, "客户端协议异常");
			ret = ErrorUtil.errorJson("ERROR.HTTP.007", exception.getMessage());
			e.printStackTrace();
			if (LogUtil.IS_LOG) LogUtil.d(TAG,
					"AsyncHttpGet  request to url :" + url + "  ClientProtocolException "
							+ e.getMessage());
		}
		catch (IOException e) {
			RequestException exception = new RequestException(
					RequestException.IO_EXCEPTION, "数据读取异常");
			ret = ErrorUtil.errorJson("ERROR.HTTP.008", exception.getMessage());
			e.printStackTrace();
			if (LogUtil.IS_LOG) LogUtil.d(TAG,
					"AsyncHttpGet  request to url :" + url + "  IOException  "
							+ e.getMessage());
		}
		finally {
			if(!Constants.IS_STOP_REQUEST){
				Message msg = new Message();
				msg.obj = ret;
				if (LogUtil.IS_LOG) LogUtil.d(TAG, ret);
				msg.getData().putSerializable("callback", callBack);
				resultHandler.sendMessage(msg);
			}
				//request.//暂时注释掉
				if(customLoadingDialog!=null&&customLoadingDialog.isShowing()){
					customLoadingDialog.dismiss();
					customLoadingDialog = null;
				}
		}
		super.run();
	}
	
	private int getShort(byte[] data) {
        return (int)((data[0]<<8) | data[1]&0xFF);
    }
}
