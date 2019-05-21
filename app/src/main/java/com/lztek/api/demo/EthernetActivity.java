package com.lztek.api.demo;

import com.lztek.api.demo.R;

import android.app.Activity;
import android.os.Bundle; 
import android.view.View;
import android.widget.Button; 
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class EthernetActivity extends Activity  { 
	
	private Button mBtOk = null;
	private Button mBtRefresh = null; 

	private CheckBox mSwitch = null;

	private RadioGroup mRdGroup;
	private RadioButton mRdbDhcp;
	private RadioButton mRdbStatic;
	
	private EditText mEdtIpAddr = null; 
	private EditText mEdtNetmask = null; 
	private EditText mEdtGateway = null; 
	private EditText mEdtDnsServer = null; 
	
	
	private com.lztek.toolkit.Lztek mLztek; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);     
		setContentView(R.layout.ethernet_layout); 
		
		setTitle(R.string.ethernet_demo); 
		
		mLztek = com.lztek.toolkit.Lztek.create(this); 
		
		mBtOk = (Button)findViewById(R.id.ethernet_ok); 
		mBtRefresh = (Button)findViewById(R.id.ethernet_refresh);
		
		mSwitch = (CheckBox)findViewById(R.id.switch_ethernet);
		
		mRdGroup = (RadioGroup)findViewById(R.id.ip_mode); 
		mRdbDhcp = (RadioButton)findViewById(R.id.dhcp); 
		mRdbStatic = (RadioButton)findViewById(R.id.staticip); 
		
		mEdtIpAddr = (EditText)findViewById(R.id.ip_address); 
		mEdtNetmask = (EditText)findViewById(R.id.net_mask); 
		mEdtGateway = (EditText)findViewById(R.id.gate_way); 
		mEdtDnsServer = (EditText)findViewById(R.id.dns_server);

		TextView textView = (TextView)findViewById(R.id.switch_ethernet);
		textView.setText(textView.getText() + " [" + mLztek.getEthMac().toUpperCase() + "]");   

		boolean ethEnalbe = mLztek.getEthEnable();
		mSwitch.setChecked(ethEnalbe);
		refreshAddrInfo(ethEnalbe); 
		
		mBtOk.setOnClickListener(new View.OnClickListener() { 
			@Override
			public void onClick(View v) {  
				onBtnConfirm();
			} 
		}); 
		mBtRefresh.setOnClickListener(new View.OnClickListener() { 
			@Override
			public void onClick(View v) {
				boolean ethEnalbe = mLztek.getEthEnable();
				mSwitch.setChecked(ethEnalbe);
				refreshAddrInfo(ethEnalbe); 
			} 
		});  
		
		mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { 
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {  
				mLztek.setEthEnable(isChecked);
				refreshAddrInfo(isChecked);
			} 
		}); 
		mRdGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { 
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (group == mRdGroup) {
					setAddrInput(mRdbStatic.isChecked());
				} 
			} 
		});
		
		mEdtIpAddr.setOnFocusChangeListener(new View.OnFocusChangeListener() { 
			@Override
			public void onFocusChange(View v, boolean hasFocus) { 
				if (!hasFocus) {
					String ip = mEdtIpAddr.getText().toString().trim();
					if (!ipCheck(ip)) { 
						return;
					} 
					if (mEdtNetmask.getText().toString().trim().length() == 0) {  
						int type = Integer.parseInt(ip.substring(0, ip.indexOf('.')));
						String mask = 1 <= type && type <= 126? "255.0.0.0" :
							(128 <= type && type <= 191? "255.255.0.0" : "255.255.255.0"); 
						mEdtNetmask.setText(mask);
					}
					if (mEdtGateway.getText().toString().trim().length() == 0) {   
						mEdtGateway.setText(ip.substring(0, ip.lastIndexOf('.')) + ".1");
					}
					if (mEdtDnsServer.getText().toString().trim().length() == 0) {   
						mEdtDnsServer.setText(ip.substring(0, ip.lastIndexOf('.')) + ".1");
					}
				}
			} 
		}); 
		mEdtGateway.setOnFocusChangeListener(new View.OnFocusChangeListener() { 
			@Override
			public void onFocusChange(View v, boolean hasFocus) { 
				if (!hasFocus) {
					String gateway = mEdtGateway.getText().toString().trim();
					if (!ipCheck(gateway)) { 
						return;
					} 
					if (mEdtDnsServer.getText().toString().trim().length() == 0) { 
						mEdtDnsServer.setText(gateway);
					} 
				}
			} 
		}); 
	}


	private void refreshAddrInfo(boolean ethEnalbe) { 
		if (ethEnalbe) { 
			setRadioButton(true);
			com.lztek.toolkit.AddrInfo addrInfo = mLztek.getEthAddrInfo();
			if (null != addrInfo) {   
				boolean dhcp = addrInfo.getIpMode() == com.lztek.toolkit.AddrInfo.DHCP; 
				mRdbDhcp.setChecked(dhcp);
				mRdbStatic.setChecked(!dhcp);
				mEdtIpAddr.setText(addrInfo.getIpAddress());
				mEdtNetmask.setText(addrInfo.getNetmask());
				mEdtGateway.setText(addrInfo.getGateway());
				mEdtDnsServer.setText(addrInfo.getDns());  
				setAddrInput(!dhcp);
			} else {  
				setAddrInput(false);
				Toast.makeText(this, "Cannot get ethernet info", Toast.LENGTH_LONG).show();
			}
		} else { 
			mEdtIpAddr.setText("");
			mEdtNetmask.setText("");
			mEdtGateway.setText("");
			mEdtDnsServer.setText("");  
			setRadioButton(false); 
			setAddrInput(false);
		}
		setButton(ethEnalbe);
	} 
	
	private void setRadioButton(boolean enable) {
		mRdGroup.setEnabled(enable);
		mRdbDhcp.setEnabled(enable);
		mRdbStatic.setEnabled(enable); 
	}
	
	private void setButton(boolean enable) {
		mBtOk.setEnabled(enable);
		mBtRefresh.setEnabled(enable); 
	}

	private void setAddrInput(boolean enable) { 
		mEdtIpAddr.setEnabled(enable);
		mEdtNetmask.setEnabled(enable);
		mEdtGateway.setEnabled(enable);
		mEdtDnsServer.setEnabled(enable);

		mEdtIpAddr.setFocusable(enable);
		mEdtNetmask.setFocusable(enable);
		mEdtGateway.setFocusable(enable);
		mEdtDnsServer.setFocusable(enable); 
		
		mEdtIpAddr.setFocusableInTouchMode(enable);
		mEdtNetmask.setFocusableInTouchMode(enable);
		mEdtGateway.setFocusableInTouchMode(enable);
		mEdtDnsServer.setFocusableInTouchMode(enable); 
	}
	

	public static boolean ipCheck(String text) {
		if (text != null && !text.isEmpty()) {
			String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
					+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
					+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
					+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
			if (text.matches(regex)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	private boolean validateDate() {// 验证输入的信息是否正确
		String ip = mEdtIpAddr.getText().toString();
		if (ip.equals("") || !ipCheck(ip)) {
			mEdtIpAddr.setFocusable(true);
			mEdtIpAddr.requestFocus();
			return false;
		}
		String net = mEdtNetmask.getText().toString();
		if (net.equals("") || !ipCheck(net)) {
			mEdtNetmask.setFocusable(true);
			mEdtNetmask.requestFocus();
			return false;
		}
		/*
		String gateway = mEdtGateway.getText().toString();
		gateway = null == gateway? "" : gateway.trim();
		if (gateway.length() == 0 || !ipCheck(gateway)) {
			mEdtGateway.setFocusable(true);
			mEdtGateway.requestFocus();
			return false;
		}
		String dns = mEdtDnsServer.getText().toString();
		dns = null == dns? "" : dns.trim();
		if (dns.length() == 0) { 
			int pos = gateway.lastIndexOf('.');
			dns = gateway.substring(0, pos) + ".1";  
			mEdtDnsServer.setText(dns);
		}
		if (!ipCheck(dns)) {
			 mEdtDnsServer.setFocusable(true);
			 mEdtDnsServer.requestFocus();
			 return false; 
		} */
		return true;
	}

	private void onBtnConfirm() { 
		
		if (mRdbStatic.isChecked()) {
			if (!validateDate()) { 
				return;
			} 
			String ip = mEdtIpAddr.getText().toString();
			String mask = mEdtNetmask.getText().toString();
			String gate = mEdtGateway.getText().toString();
			String dns = mEdtDnsServer.getText().toString();
			mLztek.setEthIpAddress(ip, mask, gate, dns);
			
			Toast.makeText(this, "Ethernet set static ip ok", Toast.LENGTH_LONG).show();
		} else {
			mLztek.setEthDhcpMode();
			Toast.makeText(this, "Ethernet set dhcp mode ok", Toast.LENGTH_LONG).show();
		} 
	} 
}
