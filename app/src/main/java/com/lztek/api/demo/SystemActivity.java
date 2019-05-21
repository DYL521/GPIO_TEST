package com.lztek.api.demo;
 

import com.lztek.api.demo.R;

import android.os.Bundle; 
import android.widget.TextView;
import android.app.Activity;

public class SystemActivity extends  Activity {  
	private com.lztek.toolkit.Lztek mLztek;
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.system_layout);
		setTitle(R.string.system_demo);  

		mLztek = com.lztek.toolkit.Lztek.create(this); 

		((TextView)findViewById(R.id.system_version)).setText(mLztek.getSystemVersion());
		((TextView)findViewById(R.id.kernel_version)).setText(mLztek.getKernelVersion());
		((TextView)findViewById(R.id.api_version)).setText(mLztek.getApiVersion());
		((TextView)findViewById(R.id.running_memory)).setText(
				android.text.format.Formatter.formatFileSize(this, 
						mLztek.getSystemMemory()));
		((TextView)findViewById(R.id.eth0_mac)).setText(mLztek.getEthMac().toUpperCase());
		((TextView)findViewById(R.id.wlan0_mac)).setText(wlanMac()); 	
	} 
	
	@Override
	protected void onDestroy() { 
		super.onDestroy();
	}
	
	@Override
	protected void onPause() { 
		super.onPause(); 
	} 
	
	protected String wlanMac() {  
		android.net.wifi.WifiManager mWifiManager = (android.net.wifi.WifiManager)this.getSystemService(
				android.content.Context.WIFI_SERVICE); 
		android.net.wifi.WifiInfo wifiInfo = mWifiManager.getConnectionInfo();  
        if (wifiInfo == null)
        	return "";
        String mac = wifiInfo.getMacAddress();
        return mac == null? "" : mac.toUpperCase(); 
	}
}
