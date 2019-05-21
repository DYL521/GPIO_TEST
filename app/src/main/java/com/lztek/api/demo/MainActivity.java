package com.lztek.api.demo;

import com.lztek.api.demo.R; 

import android.app.Activity; 
import android.content.Intent;
import android.os.Bundle; 
import android.view.View;
import android.widget.Button; 

public class MainActivity extends Activity implements 
	android.view.View.OnClickListener {

	private android.util.SparseArray<Intent> mBtnIdMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);   
  
		setContentView(R.layout.main_layout);  
		
		mBtnIdMap = new android.util.SparseArray<Intent>();
		mBtnIdMap.put(R.id.power_demo, new Intent(this, PowerActivity.class));
		mBtnIdMap.put(R.id.serialport_demo, new Intent(this, SerialPortActivity.class));
		mBtnIdMap.put(R.id.gpio_demo, new Intent(this, GPIOActivity.class));
		mBtnIdMap.put(R.id.watchdog_demo, new Intent(this, WatchDogActivity.class));
		mBtnIdMap.put(R.id.screen_demo, new Intent(this, ScreenActivity.class));
		mBtnIdMap.put(R.id.ethernet_demo, new Intent(this, EthernetActivity.class));
		mBtnIdMap.put(R.id.density_demo, new Intent(this, DensityActivity.class));
		mBtnIdMap.put(R.id.apk_demo, new Intent(this, ApkActivity.class)); 
		mBtnIdMap.put(R.id.system_demo, new Intent(this, SystemActivity.class)); 
		mBtnIdMap.put(R.id.hdmiin_demo, new Intent(this, HdmiInActivity.class)); 
		mBtnIdMap.put(R.id.ota_demo, new Intent(this, OtaUpdateActivity.class)); 
		
		for (int i=0; i<mBtnIdMap.size(); ++i) {
			((Button)findViewById(mBtnIdMap.keyAt(i))).setOnClickListener(this);
		} 
		((Button)findViewById(R.id.exit_app)).setOnClickListener(this); 
	} 

	@Override
	protected void onDestroy() { 
		super.onDestroy(); 
		System.exit(0);
	}  

	@Override
	public void onClick(View v) {  
		int id = v.getId();
		if (id == R.id.exit_app) {   
			finish();
		} else { 
			Intent intent = mBtnIdMap.get(v.getId(), null); 
			if (null != intent) { 
				startActivity(intent); 
			}
		} 
	}   
}
