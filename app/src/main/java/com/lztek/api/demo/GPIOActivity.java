package com.lztek.api.demo;

import com.lztek.api.demo.R; 

import android.app.Activity;
import android.os.Bundle; 
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class GPIOActivity extends Activity implements 
	View.OnClickListener {  
	
	private EditText mEtPort;
	private Button mBtEnbale;
	private RadioButton mRbIn;
	private RadioButton mRbOut;
	private RadioButton mRbHigh;
	private RadioButton mRbLow; 
	private Button mBtRead;
	private Button mBtWrite;
	
	private RadioGroup mRbgValue;
	
	private com.lztek.toolkit.Lztek mLztek;
	
	private int mPort;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);    
		setContentView(R.layout.gpio_layout);  
		setTitle(R.string.gpio_demo);

		mLztek = com.lztek.toolkit.Lztek.create(this); 

		mEtPort = (EditText)findViewById(R.id.gpid_port);
    	mBtEnbale = (Button)findViewById(R.id.gpio_enable);
		mRbIn = (RadioButton)findViewById(R.id.gpio_in_radiobutton);
		mRbOut = (RadioButton)findViewById(R.id.gpio_out_radiobutton);
		mRbHigh = (RadioButton)findViewById(R.id.gpio_value_high);
		mRbLow = (RadioButton)findViewById(R.id.gpio_value_low); 
		mBtRead = (Button)findViewById(R.id.gpio_read_button);
		mBtWrite = (Button)findViewById(R.id.gpio_write_button); 
		
		mRbgValue = (RadioGroup)findViewById(R.id.gpio_value_group); 
		
		
		mRbIn.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() { 
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mBtRead.setEnabled(isChecked);
				mBtWrite.setEnabled(!isChecked); 
				mRbHigh.setEnabled(!isChecked);
				mRbLow.setEnabled(!isChecked);
				
				mRbgValue.clearCheck(); 
			}
		}); 
		 
		mBtEnbale.setOnClickListener(this); 
		mBtRead.setOnClickListener(this); 
		mBtWrite.setOnClickListener(this); 
	} 

	@Override
	public void onClick(View v) {  
		if (v == null) { 
		} else if (v == mEtPort) {  
		} else if (v == mBtEnbale) { 
			onBtEnbale();
		} else if (v == mBtRead) {
			onBtRead();
		} else if (v == mBtWrite) {
			onBtWrite();
		} 
	}

	private void onBtEnbale() {  
		int port = -1;
		try {
			port = Integer.parseInt(mEtPort.getText().toString());
		} catch (Exception e) { 
		} 
		if (port <= 0) {
			mEtPort.requestFocus();
			return;
		}
		if (!mLztek.gpioEnable(port)) {
			Toast.makeText(this, "GPIO[" + port + "] enable failed", Toast.LENGTH_LONG).show();
			return;
		}

		mEtPort.setEnabled(false);  
		mBtEnbale.setEnabled(false); 
		
		mRbIn.setEnabled(true);  
		mRbOut.setEnabled(true);  
		
		mLztek.setGpioInputMode(port); 
		mRbIn.setChecked(true);
		
		mPort = port;
	}
	
	private void onBtRead() {
		int value = mLztek.getGpioValue(mPort);
		if (0 != value && 1 != value) { 
			Toast.makeText(this, "GPIO[" + mPort + "] read failed", Toast.LENGTH_LONG).show();
			return;
		}
		mRbLow.setChecked(0 == value);
		mRbHigh.setChecked(1 == value);
	}
	
	private void onBtWrite() {  
		int value = mRbHigh.isChecked()? 1 : 0;
		mLztek.setGpioValue(mPort, value);
		// if (!mLztek.setGpioValue(mPort, value)) { 
		//	Toast.makeText(this, "GPIO[" + mPort + "] write[" + value + "] failed", 
		//			Toast.LENGTH_LONG).show();
		//	return;
		//} 
	}
} 
