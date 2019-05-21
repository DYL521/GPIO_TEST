package com.lztek.api.demo;

import com.lztek.api.demo.R;

import android.app.Activity;
import android.os.Bundle; 
import android.os.Handler;
import android.view.View;
import android.widget.Button; 
import android.widget.EditText;
import android.widget.Toast;

public class ScreenActivity extends Activity  { 
	
	private Button mBtScreenOn = null;
	private Button mBtScreenOff = null; 
	private Button mBtTrigger = null; 
	private EditText mEdIO = null; 

	private Handler mHandler = new Handler();
	private boolean mScreenOn = true; 
	

	private int mIOPort = -1; 
	
	private com.lztek.toolkit.Lztek mLztek;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);     
		setContentView(R.layout.screen_layout); 
		
		setTitle(R.string.screen_demo);

		mLztek = com.lztek.toolkit.Lztek.create(this); 
		
		mBtScreenOn = (Button)findViewById(R.id.screen_on); 
		mBtScreenOff = (Button)findViewById(R.id.screen_off);
		mBtTrigger = (Button)findViewById(R.id.screen_iotrigger); 
		mEdIO = (EditText)findViewById(R.id.screen_io); 
		
		mBtScreenOn.setOnClickListener(new View.OnClickListener() { 
			@Override
			public void onClick(View v) { 
				mLztek.setLcdBackLight(true);
				mScreenOn = true;
			} 
		}); 
		mBtScreenOff.setOnClickListener(new View.OnClickListener() { 
			@Override
			public void onClick(View v) { 
				mLztek.setLcdBackLight(false);
				mScreenOn = false;
			} 
		}); 

		mBtTrigger.setOnClickListener(new View.OnClickListener() { 
			@Override
			public void onClick(View v) {  
				int port = -1;
				try {
					port = Integer.parseInt(mEdIO.getText().toString());
				} catch (Exception e) { 
				}   

				if (port <= 0) {  
					return;
				}
				
				if (!mLztek.gpioEnable(port) ) { // || !mLztek.setGpioInputMode(port)
					Toast.makeText(ScreenActivity.this, "GPIO[" + port + "] setting failed", 
							Toast.LENGTH_LONG).show();
					return;
				}
				mLztek.setGpioInputMode(port);
				
				mIOPort = port;
				mHandler.postDelayed(mTriggerRunnable, 200);

				mEdIO.setEnabled(false);
				mBtTrigger.setEnabled(false); 
			} 
		}); 
	} 
	
	@Override
	protected void onStop() { 
		super.onStop();
		mHandler.removeCallbacksAndMessages(null);
		if (!mScreenOn) {  
			mLztek.setLcdBackLight(true);
			mScreenOn = true;
		} 
	} 
	
	private Runnable mTriggerRunnable = new Runnable() {  
		@Override
		public void run() { 
			final int PRESS_DOWN = 0;
			if (PRESS_DOWN == mLztek.getGpioValue(mIOPort)) {
				if (mScreenOn) { 
					mLztek.setLcdBackLight(false);
					mScreenOn = false;
				} else { 
					mLztek.setLcdBackLight(true);
					mScreenOn = true;
				} 
				mHandler.postDelayed(mTriggerRunnable, 1000);
			} else {
				mHandler.postDelayed(mTriggerRunnable, 200);
			} 
		}
	}; 
	
}
