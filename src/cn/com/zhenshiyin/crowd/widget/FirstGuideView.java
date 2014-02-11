package cn.com.zhenshiyin.crowd.widget;

import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.widget.FirstGuideContentView.OnScreenChangeListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;


public class FirstGuideView extends FrameLayout {
	private static final String TAG = "FirstGuideView";
	
	private FirstGuideContentView contentView;
	private ViewPageIndicator mIndicator;
	
	private onFirstGuideFinishedListener mOnFirstGuideFinishedListener;
	
	public FirstGuideView(Context context) {
		this(context, null);
	}
	
	public FirstGuideView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public FirstGuideView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		contentView = new FirstGuideContentView(context, attrs, defStyle);
		addView(contentView, lp);
		
		mIndicator = new ViewPageIndicator(context);
		lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		addView(mIndicator, lp);
		
		mIndicator.setPointCount(contentView.getChildCount());
		
		contentView.setOnScreenChangeListener(mOnScreenChangeListener);
	}
	
	private OnScreenChangeListener mOnScreenChangeListener = new OnScreenChangeListener() {

		@Override
		public void onScreenChange(int currentIndex, int totalCount) {
			if (LogUtil.IS_LOG) LogUtil.d(TAG, "[onScreenChange] currentIndex=" + currentIndex + "; totalCount=" + totalCount);
			
			if (currentIndex > totalCount - 1) {
				if (mOnFirstGuideFinishedListener != null) {
					mOnFirstGuideFinishedListener.onFirstGuideFinished();
				}
				
				return;
			}
			
			mIndicator.setPoint(currentIndex);
		}
		
	};
	
	public interface onFirstGuideFinishedListener {
		public void onFirstGuideFinished();
	}
	
	public void setFirstGuideFinishedListener(onFirstGuideFinishedListener listener) {
		mOnFirstGuideFinishedListener = listener;
	}
}