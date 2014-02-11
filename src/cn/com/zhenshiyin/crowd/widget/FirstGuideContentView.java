package cn.com.zhenshiyin.crowd.widget;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import cn.com.zhenshiyin.crowd.R;
import cn.com.zhenshiyin.crowd.util.LogUtil;
import cn.com.zhenshiyin.crowd.widget.ScrollLayout.OnScreenChangeListener;


public class FirstGuideContentView extends ViewGroup {
	private static final String TAG = "FirstGuideContentView";
	
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	
	private static final int SNAP_VELOCITY = 200;
	
	private Context mContext;
	
	private int mCurScreen;
	private int mDefaultScreen = 0;
	
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	
	private int mTouchState = TOUCH_STATE_REST;
	private int mTouchSlop;
	
	private float mLastMotionX;
	private float mLastMotionY;
	
	public FirstGuideContentView(Context context) {
		this(context, null);
	}
	
	public FirstGuideContentView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public FirstGuideContentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mContext = context;
		
		mScroller = new Scroller(context);

		mCurScreen = mDefaultScreen;
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FirstGuideView);
		int entriesId = a.getResourceId(R.styleable.FirstGuideView_guideEntries, 0);
		a.recycle();
		
		// init guide views.
		a = context.getResources().obtainTypedArray(entriesId);
		int N = a.length();
		for (int i = 0; i < N; i++) {
			int resId = a.getResourceId(i, 0);
			
			ImageView imageView = new ImageView(mContext);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
	                LinearLayout.LayoutParams.WRAP_CONTENT));
			
			imageView.setBackgroundResource(resId);
			
			addView(imageView);
		}
		a.recycle();
		
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[onLayout] begin");
		
		int childLeft = 0;
		final int childCount = getChildCount();
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "	childCount=" + childCount);
		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				final int childWidth = childView.getMeasuredWidth();
				childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
		
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[onLayout] end .................................");
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[onMeasure] begin");
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException(
					"ScrollLayout only canmCurScreen run at EXACTLY mode!");
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException(
					"ScrollLayout only can run at EXACTLY mode!");
		}

		// The children are given the same width and height as the scrollLayout
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		System.out.println("moving to screen " + mCurScreen);
		scrollTo(mCurScreen * width, 0);
		
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[onMeasure] end.............................");
	}

	/**
	 * According to the position of current layout scroll to the destination
	 * page.
	 */
	public void snapToDestination() {
		final int screenWidth = getWidth();
		final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
		snapToScreen(destScreen);
	}
	
	public void snapToScreen(int whichScreen) {
		// get the valid layout page
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		if (getScrollX() != (whichScreen * getWidth())) {

			final int delta = whichScreen * getWidth() - getScrollX();
			mScroller.startScroll(getScrollX(), 0, delta, 0,
					Math.abs(delta) * 2);
			
			mCurScreen = whichScreen;
			invalidate(); // Redraw the layout
		}
	}
	
	public void setToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		mCurScreen = whichScreen;
		scrollTo(whichScreen * getWidth(), 0);
	}

	public int getCurScreen() {
		return mCurScreen;
	}
	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (LogUtil.IS_LOG) LogUtil.d(TAG, "[onTouchEvent]");
		
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (LogUtil.IS_LOG) Log.d(TAG, "	event down!");
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			break;

		case MotionEvent.ACTION_MOVE:
			int deltaX = (int) (mLastMotionX - x);
			mLastMotionX = x;

			if (deltaX < 0) {
				int tmpDelta = Math.abs(deltaX);
				
				if (getScrollX() > tmpDelta) {
					scrollBy(deltaX, 0);
				} else {
					scrollTo(0, 0);
				}
			} else {
				scrollBy(deltaX, 0);
			}
			break;

		case MotionEvent.ACTION_UP:
			if (LogUtil.IS_LOG) Log.d(TAG, "	event up");

			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000);
			int velocityX = (int) velocityTracker.getXVelocity();

			if (LogUtil.IS_LOG) Log.d(TAG, "	velocityX:" + velocityX);

			if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
				// Fling enough to move left
				if (LogUtil.IS_LOG)  Log.d(TAG, "	snap left");
				onScreenChangeListener.onScreenChange(mCurScreen - 1, getChildCount());
				System.out.println("mCurScreen=" + (mCurScreen - 1));
				snapToScreen(mCurScreen - 1);
			} else if (velocityX < -SNAP_VELOCITY
					&& mCurScreen < getChildCount() ) {
				// Fling enough to move right
				if (LogUtil.IS_LOG)  Log.e(TAG, "	snap right");
				onScreenChangeListener.onScreenChange(mCurScreen + 1, getChildCount());
				
				System.out.println("mCurScreen=" + (mCurScreen + 1));
				if(mCurScreen<2){
				snapToScreen(mCurScreen + 1);
				}
			} else {
				snapToDestination();
			}

			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
			break;
		}

		return true;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (LogUtil.IS_LOG) Log.d(TAG, "[onInterceptTouchEvent] mTouchSlop:" + mTouchSlop);

		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}

		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(mLastMotionX - x);
			if (xDiff > mTouchSlop) {
				mTouchState = TOUCH_STATE_SCROLLING;

			}
			break;

		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY = y;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;
			break;

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			break;
		}

		return mTouchState != TOUCH_STATE_REST;
	}

	public interface OnScreenChangeListener {
		void onScreenChange(int currentIndex, int totalCount);
	}

	private OnScreenChangeListener onScreenChangeListener;

	public void setOnScreenChangeListener(
			OnScreenChangeListener onScreenChangeListener) {
		this.onScreenChangeListener = onScreenChangeListener;
	}
	
}