package com.lztek.api.demo;
 

import com.lztek.api.demo.R;

import android.app.Activity;
import android.os.Bundle; 
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class WatchDogActivity extends Activity implements 
	android.view.View.OnClickListener {
 
	private Button mBtEnableDog;
	private Button mBtnStartFeed;
	private Button mBtnStopFeed;
	private Button mBtnDisableDog;  

	private Handler mHandler = new Handler();
	
	private com.lztek.toolkit.Lztek mLztek;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.watchdog_layout);
		setTitle(R.string.watchdog_demo); 

		mLztek = com.lztek.toolkit.Lztek.create(this); 
		
		mBtEnableDog = (Button)findViewById(R.id.watchdog_enable); 
		mBtnStartFeed = (Button)findViewById(R.id.watchdog_startfeed);
		mBtnStopFeed = (Button)findViewById(R.id.watchdog_stopfeed); 
		mBtnDisableDog = (Button)findViewById(R.id.watchdog_disable); 
		
		mBtEnableDog.setEnabled(true);
		mBtnDisableDog.setEnabled(false); 
		mBtnStartFeed.setEnabled(false);
		mBtnStopFeed.setEnabled(false);
		
		mBtEnableDog.setOnClickListener(this);
		mBtnDisableDog.setOnClickListener(this);
		mBtnStartFeed.setOnClickListener(this);
		mBtnStopFeed.setOnClickListener(this);
	} 
	
	@Override
	protected void onDestroy() { 
		super.onDestroy();
	}
	
	@Override
	protected void onPause() { 
		super.onPause();
		mBtnDisableDog.performClick();
	}

	@Override
	public void onClick(View v) {  
		if (v == null) {  
		} else if (v == mBtEnableDog) { 
			if (!mLztek.watchDogEnable()) {
				Toast.makeText(this, "Open watch dog failed", Toast.LENGTH_LONG).show();
				return;
			}
			mBtEnableDog.setEnabled(false);
			mBtnDisableDog.setEnabled(true);
			mHandler.post(new Runnable() { 
				@Override
				public void run() { 
					mLztek.watchDogFeed();
					mHandler.postDelayed(this, 3000);
				}
			});

			mBtnStartFeed.setEnabled(false);
			mBtnStopFeed.setEnabled(true); 
		} else if (v == mBtnStartFeed) {  
			mHandler.post(new Runnable() { 
				@Override
				public void run() { 
					mLztek.watchDogFeed();
					mHandler.postDelayed(this, 3000);
				}
			});
			mBtnStartFeed.setEnabled(false);
			mBtnStopFeed.setEnabled(true); 
		} else if (v == mBtnStopFeed) {  
			mHandler.removeCallbacksAndMessages(null);
			mBtnStartFeed.setEnabled(true);
			mBtnStopFeed.setEnabled(false); 
		} else if (v == mBtnDisableDog) {
			mLztek.watchDogDisable(); 
			mHandler.removeCallbacksAndMessages(null);
			
			mBtEnableDog.setEnabled(true);
			mBtnDisableDog.setEnabled(false);
			
			mBtnStartFeed.setEnabled(false);
			mBtnStopFeed.setEnabled(false);
		}  
	}  
}
