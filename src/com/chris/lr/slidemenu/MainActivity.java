package com.chris.lr.slidemenu;

import com.chris.lr.slidemenu.LayoutRelative.OnScrollListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.app.Activity;

public class MainActivity extends Activity implements OnGestureListener, OnTouchListener, OnItemClickListener {

	private static final String TAG = "ChrisSlideMenu";
	//代表Activity的中心部分
	private RelativeLayout mainLayout;
	//代表Activity的左侧菜单
	private RelativeLayout leftLayout;
	//代表Activity的右侧菜单
	private RelativeLayout rightLayout;
	//代表滑动菜单
	private LayoutRelative layoutSlideMenu;
	//代表滑动菜单当中的选项
	private ListView mListMore;
	
	//点击两个ImageView将会分别呼出两侧的菜单
	private ImageView ivMore;
	private ImageView ivSettings;
	//手势处理器对象
	private GestureDetector mGestureDetector;
	
	//滚动的速度
	private static final int SPEED = 30;
	//是否在滚动
	private boolean bIsScrolling = false;
	
	private int iLimited = 0;
	//滚动的距离
	private int mScroll = 0;
	private View mClickedView = null;
	//左侧菜单当中的数据
	private String title[] = {"待发送队列", 
							  "同步分享设置", 
							  "编辑我的资料", 
							  "找朋友", 
							  "告诉朋友", 
							  "节省流量", 
							  "推送设置", 
							  "版本更新", 
							  "意见反馈", 
							  "积分兑换", 
							  "精品应用", 
							  "常见问题", 
							  "退出当前帐号", 
							  "退出1", 
							  "退出2", 
							  "退出3", 
							  "退出4"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initView();
	}
	
	private void initView(){
		mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
		leftLayout = (RelativeLayout) findViewById(R.id.leftLayout);
		rightLayout = (RelativeLayout) findViewById(R.id.rightLayout);
		mainLayout.setOnTouchListener(this);
		leftLayout.setOnTouchListener(this);
		rightLayout.setOnTouchListener(this);
		
		//获取滑动菜单对象
		layoutSlideMenu = (LayoutRelative) findViewById(R.id.layoutSlideMenu);
		//注册监听器
		layoutSlideMenu.setOnScrollListener(new OnScrollListener(){
			@Override
			public void doOnScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				onScroll(distanceX);
			}
			
			@Override
			public void doOnRelease(){
				onRelease();
			}
		});
		
		ivMore = (ImageView) findViewById(R.id.ivMore);
		ivSettings = (ImageView) findViewById(R.id.ivSettings);
		ivMore.setOnTouchListener(this);
		ivSettings.setOnTouchListener(this);
		
		mListMore = (ListView) findViewById(R.id.listMore);
		mListMore.setAdapter(new ArrayAdapter<String>(this, R.layout.item, R.id.tv_item, title));
		mListMore.setOnItemClickListener(this);
		
		mGestureDetector = new GestureDetector(this);
		mGestureDetector.setIsLongpressEnabled(false);
		
		resizeLayout();
	}
	
	/*
	 * 使用leftMargin及rightMargin防止layout被挤压变形
	 * Math.abs(leftMargin - rightMargin) = layout.width
	 */
	private void resizeLayout(){
		DisplayMetrics dm = getResources().getDisplayMetrics();
		
		// 固定 main layout, 防止被左、右挤压变形
		RelativeLayout.LayoutParams lp = (LayoutParams) mainLayout.getLayoutParams();
		lp.width = dm.widthPixels;
		mainLayout.setLayoutParams(lp);
		
		// 将左layout调整至main layout左边
		lp = (LayoutParams) leftLayout.getLayoutParams();
		lp.leftMargin = -lp.width;
		leftLayout.setLayoutParams(lp);
		Log.d(TAG, "left l.margin = " + lp.leftMargin);
		
		// 将左layout调整至main layout右边
		lp = (LayoutParams) rightLayout.getLayoutParams();
		lp.leftMargin = dm.widthPixels;
		lp.rightMargin = -lp.width;
		rightLayout.setLayoutParams(lp);
		Log.d(TAG, "right l.margin = " + lp.leftMargin);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			RelativeLayout.LayoutParams lp = (LayoutParams) mainLayout.getLayoutParams();
			
			if(lp.leftMargin != 0){
				if(lp.leftMargin > 0){
					new SlideMenu().execute(leftLayout.getLayoutParams().width, -SPEED);
				}else if(lp.leftMargin < 0){
					new SlideMenu().execute(rightLayout.getLayoutParams().width, SPEED);
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void rollLayout(int margin){
		RelativeLayout.LayoutParams lp = 
				(LayoutParams) mainLayout.getLayoutParams();
		lp.leftMargin += margin;
		lp.rightMargin -= margin;
		mainLayout.setLayoutParams(lp);
		lp = (LayoutParams) leftLayout.getLayoutParams();
		lp.leftMargin += margin;
		leftLayout.setLayoutParams(lp);
		lp = (LayoutParams) rightLayout.getLayoutParams();
		lp.leftMargin += margin;
		lp.rightMargin -= margin;
		rightLayout.setLayoutParams(lp);
	}

	//该方法是真正实现布局滚动的方法
	private void onScroll(float distanceX){
		bIsScrolling = true;
		mScroll += distanceX;  // 向左为正
		Log.d(TAG, "mScroll = " + mScroll + ", distanceX = " + distanceX);
		
		RelativeLayout.LayoutParams lp = (LayoutParams) mainLayout.getLayoutParams();
		RelativeLayout.LayoutParams lpLeft = (LayoutParams) leftLayout.getLayoutParams();
		RelativeLayout.LayoutParams lpRight = (LayoutParams) rightLayout.getLayoutParams();
		Log.d(TAG, "lp.leftMargin = " + lp.leftMargin);
		
		int distance = 0;
		if(mScroll > 0){ // 向左移动
			if(lp.leftMargin <= 0){ // 打开右导航菜单
				if(iLimited > 0){
					return;
				}
				distance = lpRight.width - Math.abs(lp.leftMargin);
			}else if(lp.leftMargin > 0){ // 关闭左导航菜单
				distance = lp.leftMargin;
			}
			if(mScroll >= distance){
				mScroll = distance;
			}
		}else if(mScroll < 0){  // 向右移动
			if(lp.leftMargin >= 0){ // 打开左导航菜单
				if(iLimited < 0){
					return;
				}
				distance = lpLeft.width - Math.abs(lp.leftMargin);
			}else if(lp.leftMargin < 0){ // 关闭右导航菜单
				distance = Math.abs(lp.leftMargin);
			}
			if(mScroll <= -distance){
				mScroll = -distance;
			}
		}

		Log.d(TAG, "mScroll = " + mScroll);
		if(mScroll != 0){
			rollLayout(-mScroll);
		}
	}
	
	private void onRelease(){
		RelativeLayout.LayoutParams lp = (LayoutParams) mainLayout.getLayoutParams();
		if(lp.leftMargin < 0){ // 左移
			/*
			 * 	左移大于右导航宽度一半，则自动展开,否则自动缩回去
			 */
			if(Math.abs(lp.leftMargin) > rightLayout.getLayoutParams().width/2){
				new SlideMenu().execute(rightLayout.getLayoutParams().width - Math.abs(lp.leftMargin), -SPEED);
			}else{
				new SlideMenu().execute(Math.abs(lp.leftMargin), SPEED);
			}
		}else if(lp.leftMargin > 0){
			/*
			 * 	右移大于左导航宽度一半，则自动展开,否则自动缩回去
			 */
			if(Math.abs(lp.leftMargin) > leftLayout.getLayoutParams().width/2){
				new SlideMenu().execute(leftLayout.getLayoutParams().width - Math.abs(lp.leftMargin), SPEED);
			}else{
				new SlideMenu().execute(Math.abs(lp.leftMargin), -SPEED);
			}
		}
	}

	///////////////////// ListView.onItemClick ///////////////////////
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Toast.makeText(this, title[arg2], Toast.LENGTH_SHORT).show();
	}
	
	////////////////////////////// onTouch ///////////////////////////////
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mClickedView = v;
		
		if(MotionEvent.ACTION_UP == event.getAction() && bIsScrolling){
			onRelease();
		}
		
		return mGestureDetector.onTouchEvent(event);
	}
	
	/////////////////// GestureDetector Override Begin ///////////////////
	@Override
	public boolean onDown(MotionEvent e) {
		
		bIsScrolling = false;
		mScroll = 0;
		iLimited = 0;
		RelativeLayout.LayoutParams lp = (LayoutParams) mainLayout.getLayoutParams();
		if(lp.leftMargin > 0){
			iLimited = 1;
		}else if(lp.leftMargin < 0){
			iLimited = -1;
		}
		
		return true;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		onScroll(distanceX);
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		/*
		 * 	正常情况下，mainLayout的leftMargin为0,
		 * 	当左/右菜单为打开中，此时就不为0，需要判断
		 */
		if(mClickedView != null){
			RelativeLayout.LayoutParams lp = (LayoutParams) mainLayout.getLayoutParams();
			
			if(mClickedView == ivMore){
				Log.d(TAG, "[onSingleTapUp] ivMore clicked! leftMargin = " + lp.leftMargin);
				
				if(lp.leftMargin == 0){
					new SlideMenu().execute(leftLayout.getLayoutParams().width, SPEED);
				}else{
					new SlideMenu().execute(leftLayout.getLayoutParams().width, -SPEED);
				}
			}else if(mClickedView == ivSettings){
				Log.d(TAG, "[onSingleTapUp] ivSettings clicked! leftMargin = " + lp.leftMargin);
				
				if(lp.leftMargin == 0){
					new SlideMenu().execute(rightLayout.getLayoutParams().width, -SPEED);
				}else{
					new SlideMenu().execute(rightLayout.getLayoutParams().width, SPEED);
				}
			}else if(mClickedView == mainLayout){
				Log.d(TAG, "[onSingleTapUp] mainLayout clicked!");
			}
		}
		return true;
	}
	/////////////////// GestureDetector Override End ///////////////////
	
	/**
	 * 
	 * @author cheng.yang
	 *
	 *	左、右菜单滑出
	 *
	 *	params[0]: 滑动距离
	 *	params[1]: 滑动速度,带方向
	 */
	public class SlideMenu extends AsyncTask<Integer, Integer, Void>{
		@Override
		protected Void doInBackground(Integer... params) {
			if(params.length != 2){
				Log.e(TAG, "error, params must have 2!");
			}

			int times = params[0] / Math.abs(params[1]);
			if(params[0] % Math.abs(params[1]) != 0){
				times ++;
			}
			
			for(int i = 0; i < times; i++){
				this.publishProgress(params[0], params[1], i+1);
			}
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			if(values.length != 3){
				Log.e(TAG, "error, values must have 3!");
			}

			int distance = Math.abs(values[1]) * values[2];
			int delta = values[0] - distance;

			int leftMargin = 0;
			if(values[1] < 0){ // 左移
				leftMargin = (delta > 0 ? values[1] : -(Math.abs(values[1]) - Math.abs(delta)));
			}else{
				leftMargin = (delta > 0 ? values[1] : (Math.abs(values[1]) - Math.abs(delta)));
			}
			
			rollLayout(leftMargin);
		}
	}
}
