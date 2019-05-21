package com.lztek.api.demo;

import com.lztek.api.demo.R;

import android.app.Activity;
import android.os.Bundle; 
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PowerActivity extends Activity  { 
	
	private Button mBtReboot = null;
	private Button mBtPowerOff = null; 
	private EditText mEtTime = null; 
	
	private com.lztek.toolkit.Lztek mLztek;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);     
		setContentView(R.layout.power_layout); 
		
		setTitle(R.string.power_demo);

		mLztek = com.lztek.toolkit.Lztek.create(this); 
		
		mBtReboot = (Button)findViewById(R.id.power_reboot); 
		mBtPowerOff = (Button)findViewById(R.id.power_schedule_poweroff);
		mEtTime = (EditText)findViewById(R.id.power_schedule_interval); 
		
		mBtReboot.setOnClickListener(new View.OnClickListener() { 
			@Override
			public void onClick(View v) { 
				mLztek.softReboot();
			} 
		}); 
		mBtPowerOff.setOnClickListener(new View.OnClickListener() { 
			@Override
			public void onClick(View v) {
				int min = -1;
				try {
					min = Integer.parseInt(mEtTime.getText().toString());
				} catch (Exception e) { 
				} 
				if (min <= 0) {
					mEtTime.requestFocus();
					return;
				}
				 
				mLztek.alarmPoweron(min*60);
			}
			
		});  
	} 
}
