package cn.com.zhenshiyin.crowd.activity.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PoiOverlay;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.IllegalNaviArgumentException;
import com.baidu.mapapi.navi.NaviPara;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.zhenshiyin.crowd.R;
import cn.com.zhenshiyin.crowd.base.BaseMapActivity;
import cn.com.zhenshiyin.crowd.common.Constants;
import cn.com.zhenshiyin.crowd.common.MyApplication;
import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.common.Preferences;
import cn.com.zhenshiyin.crowd.util.SharePreferencesUtil;
import cn.com.zhenshiyin.crowd.util.StringUtil;


public class NavigationMapActivity extends BaseMapActivity implements OnClickListener {
	private static final String TAG = "NavigationMapActivity";
	
	private static final int TAB_LEFT = 0;
	private static final int TAB_CENTER = 1;
	private static final int TAB_RIGHT = 2;
	
	private BMapManager mBMapMan = null;
	private MapView mMapView;
	private MKSearch mSearch = null;
	private GeoPoint mDstGeoPoint = null;
	private GeoPoint mOriGeoPoint = null;
	private double longtitude;
	private double latitude;
	private double currentLongitude;
	private double currentLatitude;
	
	private int tabSelected = TAB_LEFT;
	private View tabScenicSpots;
	private View tabScenicRestaurants;
	private View tabScenicHotels;
	private Button popButton;
	
	private String[] searchKeys = {"景点", "餐馆", "酒店"};
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "NavigationMapActivity onCreate()");
		
		mBMapMan = ((MyApplication)getApplication()).getMapManager();
		
		setContentView(R.layout.navigation_map_layout);
		
		getDataFromIntent();
		
		initView();
		
		SearchNearBy(tabSelected);
		
		//getRoute();
	}
	
	private void getDataFromIntent() {
		Intent intent = getIntent();
		longtitude = intent.getDoubleExtra(Constants.KEY_LONGTITUDE, -1);
		latitude = intent.getDoubleExtra(Constants.KEY_LATITUDE, -1);
		
		if (LogUtil.IS_LOG) Log.d(TAG, "longtitude=" + longtitude + " ; latitude=" + latitude);
		if (longtitude != -1 && latitude != -1) {
			mDstGeoPoint = new GeoPoint((int)(latitude*1e6), (int)(longtitude*1e6));
		}
		
		currentLongitude = intent.getDoubleExtra(Constants.KEY_CURRENT_LONGTITUDE, -1);
		currentLatitude = intent.getDoubleExtra(Constants.KEY_CURRENT_LATITUDE, -1);
		if (LogUtil.IS_LOG) Log.d(TAG, "currentLongitude=" + currentLongitude + " ; currentLatitude=" + currentLatitude);
		if (longtitude != -1 && latitude != -1) {
			mOriGeoPoint = new GeoPoint((int)(currentLatitude*1e6), (int)(currentLongitude*1e6));
		}
	}
	
	private void initView() {
		// Init title.
		TextView title = (TextView) findViewById(R.id.title_content);
		title.setText(getTitle());
		
		ImageView leftIcon = (ImageView) findViewById(R.id.title_left_button);
		leftIcon.setImageResource(R.drawable.ic_back);
		leftIcon.setOnClickListener(this);
		
		ImageView rightIcon = (ImageView) findViewById(R.id.title_right_button);
		rightIcon.setVisibility(View.GONE);
		
		Button rightBtn = (Button) findViewById(R.id.title_right_button1);
		rightBtn.setVisibility(View.VISIBLE);
		rightBtn.setText(R.string.navigation_nav);
		rightBtn.setOnClickListener(this);
		
		// Init top tabbar.
		tabScenicSpots = findViewById(R.id.tab_left);
		tabScenicRestaurants = findViewById(R.id.tab_center);
		tabScenicHotels = findViewById(R.id.tab_right);
		tabScenicSpots.setOnClickListener(this);
		tabScenicRestaurants.setOnClickListener(this);
		tabScenicHotels.setOnClickListener(this);
		
		// Init map.
		mMapView = (MapView) findViewById(R.id.bmapsView);
        mMapView.setBuiltInZoomControls(true);
        
        MapController mMapController = mMapView.getController();
        mMapController.setZoom(17);
        
        mSearch = new MKSearch();
        mSearch.init(mBMapMan, new MKSearchListener() {

			@Override
			public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetDrivingRouteResult(MKDrivingRouteResult result, int iError) {
				if (LogUtil.IS_LOG) LogUtil.d(TAG, "[onGetDrivingRouteResult] iError=" + iError);
				
				if (iError != 0 || result == null) {
					return;
				}
				RouteOverlay routeOverlay = new RouteOverlay(NavigationMapActivity.this, mMapView);
			    routeOverlay.setData(result.getPlan(0).getRoute(0));
			    
			    mMapView.getOverlays().add(routeOverlay);
			    mMapView.refresh();
			}

			@Override
			public void onGetPoiDetailSearchResult(int arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetPoiResult(MKPoiResult res, int type, int error) {
				
				if ( error == MKEvent.ERROR_RESULT_NOT_FOUND){  
					Toast.makeText(NavigationMapActivity.this, "抱歉，未找到结果",Toast.LENGTH_LONG).show();  
					return ;  
				}  else if (error != 0 || res == null) {  
					Toast.makeText(NavigationMapActivity.this, "搜索出错啦..", Toast.LENGTH_LONG).show();  
					return;  
				} 
				
				if (LogUtil.IS_LOG) LogUtil.d(TAG, "count=" + res.getNumPois());
				
				mMapView.getOverlays().clear();
				mMapView.refresh();
				
				MyLocationOverlay myLocationOverlay = new MyLocationOverlay(mMapView);
				LocationData locData = new LocationData();
				locData.latitude = Double.valueOf(latitude);
				locData.longitude = Double.valueOf(longtitude);
				locData.direction = 2.0f;
				myLocationOverlay.setData(locData);
				mMapView.getOverlays().add(myLocationOverlay);
				
				// 将poi结果显示到地图上  
//				PoiOverlay poiOverlay = new PoiOverlay(NavigationMapActivity.this, mMapView);  
//				poiOverlay.setData(res.getAllPoi());
//				mMapView.getOverlays().add(poiOverlay);  
//				mMapView.refresh();
				OverlayCustom overlayCustom = new OverlayCustom(NavigationMapActivity.this, getOverlayResId(), mMapView);
				List<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
				
				
				for(MKPoiInfo info : res.getAllPoi() ){  
					if ( info.pt != null ){
						if (LogUtil.IS_LOG) LogUtil.d(TAG, "POI =" + info.name);
						OverlayItem item = new OverlayItem(info.pt, info.name, info.name);
						overlayItems.add(item);
					}  
				}
				overlayCustom.addItem(overlayItems);
				mMapView.getOverlays().add(overlayCustom);
				mMapView.getController().animateTo(mDstGeoPoint);
				
				mMapView.refresh();
				
				getRoute();
			}

			@Override
			public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetTransitRouteResult(MKTransitRouteResult arg0,
					int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetWalkingRouteResult(MKWalkingRouteResult arg0,
					int arg1) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        
        popButton = new Button(this);
        popButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_background_black));
        popButton.setTextSize(14);
        popButton.setTextColor(getResources().getColor(R.color.white));
        
	}

	private void getRoute() {
		//initLoc();
		
		MKPlanNode stNode = new MKPlanNode();
		stNode.pt = mOriGeoPoint;
		MKPlanNode enNode = new MKPlanNode();
		enNode.pt = mDstGeoPoint;
		mSearch.drivingSearch(null, stNode, null, enNode);
	}
	
	private void initLoc(){
    	String lastCo = SharePreferencesUtil.getPreference(this, Preferences.COORDINATE_TYPE_TAG, Preferences.COORDINATE_KEY_TAG);
		String currentCo = SharePreferencesUtil.getPreference(this, Preferences.CURRENT_COORDINATE_TYPE_TAG, Preferences.CURRENT_COORDINATE_KEY_TAG);
		if(StringUtil.isNotEmpty(currentCo)){
			mOriGeoPoint = new GeoPoint((int)(Double.valueOf(currentCo.split("_")[0])*1e6), (int)(Double.valueOf(currentCo.split("_")[1])*1e6));
			return;
		}else if(StringUtil.isNotEmpty(lastCo)){
			mOriGeoPoint = new GeoPoint((int)(Double.valueOf(lastCo.split("_")[0])*1e6), (int)(Double.valueOf(lastCo.split("_")[1])*1e6));
			return;
		}else{
			//showToast("无法获得用户当前位置");
			return;  
		}
    }
	
	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.title_left_button:
			finish();
			break;
		case R.id.title_right_button1:
			openBaiduNavi();
			break;
		case R.id.tab_left:
			if (tabSelected != TAB_LEFT) {
				tabSelected = TAB_LEFT;
				SearchNearBy(tabSelected);
			}
			break;
		case R.id.tab_center:
			if (tabSelected != TAB_CENTER) {
				tabSelected = TAB_CENTER;
				SearchNearBy(tabSelected);
			}
			break;
		case R.id.tab_right:
			if (tabSelected != TAB_RIGHT) {
				tabSelected = TAB_RIGHT;
				SearchNearBy(tabSelected);
			}
			break;
		}
	}
	
	@Override  
	protected void onDestroy(){  
        mMapView.destroy();  
 
        super.onDestroy();  
	}
	
	@Override  
	protected void onPause(){  
        mMapView.onPause();  
        if(mBMapMan!=null){  
           mBMapMan.stop();  
        }  
        super.onPause();  
	}
	
	@Override  
	protected void onResume(){  
	    mMapView.onResume();  
	    if(mBMapMan!=null){  
            mBMapMan.start();  
	    }  
	   super.onResume();  
	}
	
	private void openBaiduNavi() {
		try {
			NaviPara para = new NaviPara();
			para.startPoint = mOriGeoPoint;
			para.endPoint = mDstGeoPoint;
			
			BaiduMapNavigation.openBaiduMapNavi(para, this);
		} catch (BaiduMapAppNotSupportNaviException e) {
			e.printStackTrace();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
			builder.setTitle("提示");
			builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				@Override
			   public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					BaiduMapNavigation.GetLatestBaiduMapApp(NavigationMapActivity.this);
			   }
			});

			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

			builder.create().show();
		} catch (IllegalNaviArgumentException e) {
			Toast.makeText(NavigationMapActivity.this, "起点或者终点为空，请重试!",Toast.LENGTH_LONG).show(); 
		}
	}
	
	private void SearchNearBy(int searchIndex) {
		mMapView.removeView(popButton);
		
		if (tabSelected == TAB_LEFT) {
			tabScenicSpots.setSelected(true);
			tabScenicRestaurants.setSelected(false);
			tabScenicHotels.setSelected(false);
		} else if (tabSelected == TAB_CENTER) {
			tabScenicSpots.setSelected(false);
			tabScenicRestaurants.setSelected(true);
			tabScenicHotels.setSelected(false);
		} else if (tabSelected == TAB_RIGHT) {
			tabScenicSpots.setSelected(false);
			tabScenicRestaurants.setSelected(false);
			tabScenicHotels.setSelected(true);
		}
		
		mSearch.poiSearchNearBy(searchKeys[searchIndex], mDstGeoPoint, 1000);
	}
	
	private int getOverlayResId() {
		if (tabSelected == TAB_LEFT) {
			return R.drawable.ic_scenic_spots_selected;
		} else if (tabSelected == TAB_CENTER) {
			return R.drawable.ic_scenic_restaurants_selected;
		} else if (tabSelected == TAB_RIGHT) {
			return R.drawable.ic_scenic_hotels_selected;
		}
		
		return -1;
	}
	
	private class OverlayCustom extends ItemizedOverlay<OverlayItem> {
		
		public OverlayCustom(Context context, int resId, MapView mapView) {
			super(context.getResources().getDrawable(resId), mapView);
		}
		
		public OverlayCustom(Drawable drawable, MapView mapView) {
			super(drawable, mapView);
		}
		
		protected boolean onTap(int index) {
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "[OverlayCustom] onTap()");
			OverlayItem item = getItem(index);
			popButton.setText(item.getTitle());
			// Generate layout params.
			MapView.LayoutParams layoutParam  = new MapView.LayoutParams(
	               MapView.LayoutParams.WRAP_CONTENT,
	               MapView.LayoutParams.WRAP_CONTENT,
	               item.getPoint(),
	               0,
	               -40,
	               MapView.LayoutParams.BOTTOM_CENTER);
			
	         // Add to MapView.
	         mMapView.addView(popButton,layoutParam);
//			
//			OverlayItem item = getItem(index);
//			if (LogUtil.IS_LOG) LogUtil.d(TAG, "[OverlayCustom] title=" + item.getTitle());
//			
//			Bitmap map = null;
//			try {
//				map = BitmapFactory.decodeStream(getAssets().open("room_full.png"));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			PopupOverlay pop = new PopupOverlay(mMapView,null);
//			pop.showPopup(map, item.getPoint(), 32);
			return true;
		}
		
		@Override
		public boolean onTap(GeoPoint pt , MapView mMapView){
			mMapView.removeView(popButton);
			return false;
		}
	}
}