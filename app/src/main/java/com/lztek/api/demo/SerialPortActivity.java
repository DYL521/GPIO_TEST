package com.lztek.api.demo; 

import java.text.SimpleDateFormat;

import com.lztek.api.demo.R;
import com.lztek.toolkit.SerialPort;

import android.app.Activity;
import android.app.AlertDialog; 
import android.content.DialogInterface;
import android.os.Bundle;  
import android.view.View; 
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CheckBox;
import android.widget.EditText; 
import android.widget.LinearLayout;
import android.widget.Spinner; 

public class SerialPortActivity extends Activity {  

	java.util.List<ComUnit> mComUnits = new java.util.ArrayList<ComUnit>();   
	
	com.lztek.toolkit.Lztek mLztek;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
		
		LinearLayout rootLayout = new LinearLayout(this); 
		setContentView(rootLayout);  
		setTitle(R.string.serialport_demo);  

		mLztek = com.lztek.toolkit.Lztek.create(this); 
		 
		android.view.ViewGroup.LayoutParams params = rootLayout.getLayoutParams();
		params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
		params.height = LinearLayout.LayoutParams.WRAP_CONTENT; 
		rootLayout.setLayoutParams(params);
		rootLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		for (int i=0; i<2; ++i) {
			ComUnit comNode = new ComUnit(); 
			rootLayout.addView(comNode.bindView(this));
			mComUnits.add(comNode);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();  

		for (ComUnit unit : mComUnits) {
			if (null != unit) {
				unit.release();
			} 
		}  
	}  
}

class ComUnit extends Thread implements  
	View.OnClickListener, 
	CompoundButton.OnCheckedChangeListener, 
	java.util.Comparator<String> { 
	private static final java.util.Map<String, String> PATH_MAP; 
	private static final Integer[] BAUDRATES;  
	static {
		PATH_MAP = new java.util.LinkedHashMap<String, String>();  
		PATH_MAP.put("ttyS0", "/dev/ttyS0");
		PATH_MAP.put("ttyS1", "/dev/ttyS1");
		PATH_MAP.put("ttyS2", "/dev/ttyS2");
		PATH_MAP.put("ttyS3", "/dev/ttyS3");
		PATH_MAP.put("ttyS4", "/dev/ttyS4"); 
		
		PATH_MAP.put("ttyP0", "/dev/ttyP0");
		PATH_MAP.put("ttyP1", "/dev/ttyP1");
		PATH_MAP.put("ttyP2", "/dev/ttyP2");
		PATH_MAP.put("ttyP3", "/dev/ttyP3"); 

		BAUDRATES = new Integer[] { 
				4800, 9600, 19200, 38400, 57600, 115200, 230400, 
				460800, 500000, 576000, 921600, 1000000,
		}; 
	}

	
	private volatile boolean mThreadRun = false; 
	
	private volatile SerialPort mSerialPort;
	
	private EditText mEditView;
	
	private Button mBtOpen; 
	private Button mBtClose; 
	private Button mBtSend; 
	private CheckBox mChbHex; 
	private Spinner mCmbCom;
	private Spinner mCmbBaudRate; 
	
	private ArrayAdapter<String> mAdapter;
	
	private SerialPortActivity mActivity; 

	public View bindView(SerialPortActivity activity) {  
		mActivity = activity;
		View rootView = mActivity.getLayoutInflater().inflate(R.layout.serialport_layout, null);
		
		mCmbCom = (Spinner)rootView.findViewById(R.id.combox_com); 
		mCmbBaudRate = (Spinner)rootView.findViewById(R.id.combox_baudrate); 
		mBtOpen = (Button)rootView.findViewById(R.id.button_open); 
		mBtClose = (Button)rootView.findViewById(R.id.button_close); 
		
		mEditView = (EditText)rootView.findViewById(R.id.eidt_receive);  
		mBtSend = (Button)rootView.findViewById(R.id.button_send); 
		mChbHex = (CheckBox)rootView.findViewById(R.id.chb_hex); 

		// mEditView.setHorizontallyScrolling(true);
		mBtClose.setEnabled(false);
		mBtSend.setEnabled(false);
		mChbHex.setEnabled(false);
		mEditView.setEnabled(false);  
		mEditView.setFocusable(false); 
		
		mBtOpen.setOnClickListener(this);
		mBtClose.setOnClickListener(this);
		mBtSend.setOnClickListener(this);    
		mChbHex.setOnCheckedChangeListener(this);
		
		mAdapter =  new android.widget.ArrayAdapter<String>(mActivity, 
				android.R.layout.simple_spinner_item);  
		mAdapter.addAll(PATH_MAP.keySet()); 
		
		mCmbCom.setAdapter(mAdapter);
		mCmbBaudRate.setAdapter(new android.widget.ArrayAdapter<Integer>(mActivity, 
				android.R.layout.simple_spinner_item, BAUDRATES));  
		for (int i=0; i<BAUDRATES.length; ++i) {
			if (115200 == BAUDRATES[i]) { 
				mCmbBaudRate.setSelection(i); // 115200
				break;
			}
		}
		
		return rootView;
	}
	
	@Override
	public void run() { 
		while (mThreadRun) { 
			try {
				//final String text = null != mSerialPort? read() : "";  
				//if (null != text && text.length() > 0) {  
				final byte[] data = null != mSerialPort? read() : null;  
				if (null != data && data.length > 0) { 
					mActivity.runOnUiThread(new Runnable() { 
						@Override
						public void run() {  
							if (mChbHex.isChecked()) { 
								/*
								StringBuilder buffer = new StringBuilder( 
										mEditView.getLineCount() >= 32? "" : mEditView.getText().toString());
								int lineNum = 3*16; // 16byte + \n  
								for (byte b : data) { 
									byte h = (byte) (0x0F & (b >> 4));
									byte l = (byte) (0x0F & b); 
									if (buffer.length() % lineNum != 0) {
										buffer.append(' '); 
									}
									buffer.append((char) (h > 9 ? 'A' + (h - 10) : '0' + h));
									buffer.append((char) (l > 9 ? 'A' + (l - 10) : '0' + l)); 
									if (buffer.length() % lineNum == (lineNum-1)) {
										buffer.append('\n'); 
									}
								}
								mEditView.setText(buffer);
								*/ 
								if (mEditView.getText().length() > 4094) {
									mEditView.setText("");
								} 
								for (byte b : data) { 
									byte h = (byte) (0x0F & (b >> 4));
									byte l = (byte) (0x0F & b);  
									mEditView.append(""+(char) (h > 9 ? 'A' + (h - 10) : '0' + h));
									mEditView.append(""+(char) (l > 9 ? 'A' + (l - 10) : '0' + l));  
								}
							} else {
								String text = new String(data); 
								if (mEditView.getLineCount() >= 32) {
									mEditView.setText(text);
								} else {
									mEditView.append(text);
								}
							}
						}
					}); 
				} else {
					Thread.sleep(50); 
				} 
			} catch (Exception e) { 
			}
		}  
	} 
	
	synchronized void release() {
		if (mThreadRun) {
			mThreadRun = false; 
			try {
				this.join(2000);
			} catch (Exception e) { 
			} 
		}
		if (null != mSerialPort) {
			SerialPort.close(mSerialPort);
			mSerialPort = null;
		}
	}
	
	
	@Override
	public void onClick(View v) { 
		if (v == null) {  
		} else if (v == mBtOpen) { 
			onBtOpen();
		} else if (v == mBtClose) { 
			onBtClose();
		} else if (v == mBtSend) { 
			onBtSend();
		} 
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
		mEditView.setText("");
	} 

	@Override
	public int compare(String lhs, String rhs) { 
		return lhs.compareTo(rhs);
	} 
	
	private void onBtOpen() {  
		if (null != mSerialPort) {
			return;
		}
		
		String com = mCmbCom.getSelectedItem().toString();
		Integer baudrate = (Integer)mCmbBaudRate.getSelectedItem();
		String path = PATH_MAP.get(com);
		if (null == (mSerialPort=mActivity.mLztek.openSerialPort(path, baudrate, 8, 0, 1, 0))) { 
			String message = com  + "[" + baudrate + "]" +  
					mActivity.getString(R.string.serialport_open_failed) + ", " + path;
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setTitle(R.string.common_error);
			builder.setMessage(message);
			builder.setNegativeButton(R.string.common_ok, null);
			builder.create();
			builder.show(); 
			return;
		} 
		
		// disable other
		for (ComUnit unit : mActivity.mComUnits) {
			if (null != unit && this != unit) {
				String comSelected = unit.mCmbCom.getSelectedItem().toString(); 
				unit.mAdapter.remove(com);
				int index = unit.mAdapter.getPosition(comSelected);
				if (index >= 0) {
					unit.mCmbCom.setSelection(index);
				}
			} 
		}   
		
		mCmbCom.setEnabled(false);  
		mCmbBaudRate.setEnabled(false);
		mBtOpen.setEnabled(false); 
		mBtClose.setEnabled(true); 
		mBtSend.setEnabled(true); 
		mChbHex.setEnabled(true); 
		mEditView.setEnabled(true);
		mEditView.setFocusable(true); 

		if (!mThreadRun) {
			mThreadRun = true;
			this.start();
		}
	}
	
	private void onBtClose() { 
		if (null == mSerialPort) {  
			return;
		}
		SerialPort.close(mSerialPort);
		mSerialPort = null;
		
		String com = mCmbCom.getSelectedItem().toString(); 
		// add other
		for (ComUnit unit : mActivity.mComUnits) {
			if (null != unit && this != unit) {
				String comSelected = unit.mCmbCom.getSelectedItem().toString(); 
				unit.mAdapter.add(com); 
				unit.mAdapter.sort(this);
				unit.mCmbCom.setSelection(unit.mAdapter.getPosition(comSelected));
			} 
		}   
		
		mCmbCom.setEnabled(true);  
		mCmbBaudRate.setEnabled(true);
		mBtOpen.setEnabled(true);  
		mBtClose.setEnabled(false); 
		mBtSend.setEnabled(false); 
		mChbHex.setEnabled(false); 
		mEditView.setEnabled(false);
		mEditView.setFocusable(false);  
	}
	
	private void onBtSend() { 
		if (null == mSerialPort) {
			return;
		} 
		
		if (!mChbHex.isChecked()) { 
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			String com = mCmbCom.getSelectedItem().toString(); 
			String hex = "0123456789abcdefABCDEF";
			String text = com  + ": " + format.format(new java.util.Date()) + " " + hex + "\n";
			write(text.getBytes()); 
			return;
		}
 
    	AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final android.widget.EditText edtHex = new android.widget.EditText(mActivity); 
        final LinearLayout layout = new LinearLayout(mActivity);
        layout.setOrientation(LinearLayout.VERTICAL); 
        layout.addView(edtHex); 

        edtHex.setHint("HEX                   "
        		+ "                           "
        		+ "                      "
        		+ "      ..."); 
        //edtHex.setKeyListener(android.text.method.DigitsKeyListener.getInstance("0123456789abcdefABCDEF")); 
        builder.setTitle("  ");
        builder.setView(layout); 
		builder.setPositiveButton(android.R.string.ok, 
				new DialogInterface.OnClickListener() { 
					@Override
					public void onClick(DialogInterface dialog, int id) { 
						String hex = edtHex.getText().toString(); 
						//dialog.dismiss(); 
						byte[] data = hexBytes(hex);
						if (null != data && data.length > 0) {
							write(data);
						}
					}
				});
		builder.setNegativeButton(android.R.string.cancel, 
				new DialogInterface.OnClickListener() { 
					@Override
					public void onClick(DialogInterface dialog, int id) {   
						dialog.dismiss();  
					} 
				}); 
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show(); 
	}
	
	private int write( byte[] data) { 
		if (null == mSerialPort || null == data || data.length == 0) {
			return -1;
		} 
		
		java.io.OutputStream output = null; 
		
		try {  
			output = mSerialPort.getOutputStream(); 
			output.write(data);
			return data.length;
		} catch (Exception e) {  
			android.util.Log.d("#ERROR#", "[COM]Write Faild: " + e.getMessage(), e);
			return -1;
		} 
	}
 
	
	private byte[] read() { 
		if (null == mSerialPort) {
			return null;
		} 
		
		java.io.InputStream input = null;   
		try { 
			input = mSerialPort.getInputStream(); 
			
			byte[] buffer = new byte[1024]; 
			int len = input.read(buffer); 
			// return len > 0? new String(buffer, 0, len) : ""; 
			return len > 0? java.util.Arrays.copyOfRange(buffer, 0, len) : null;
		} catch (Exception e) {  
			android.util.Log.d("#ERROR#", "[COM]Read Faild: " + e.getMessage(), e);
			return null;
		}   
	}
	

	private static byte[] hexBytes(String hexString) { 
		int length;
		byte h;
		byte l;
		byte[] byteArray;
		 
		length = null != hexString? hexString.length() : 0; 
		length = (length - (length%2))/2;
		if (length < 1) {
			return null;
		}
		
		byteArray = new byte[length];
		for (int i = 0; i < length; i++) {
			h = (byte)hexString.charAt(i*2);
			l = (byte)hexString.charAt(i*2 + 1);

			l = (byte)('0' <= l && l <= '9'? l - '0' : 
					'A' <= l && l <= 'F'? (l - 'A') + 10 : 
						'a' <= l && l <= 'f'? (l - 'a') + 10 : 0); 
			h = (byte)('0' <= h && h <= '9'? h - '0' : 
					'A' <= h && h <= 'F'? (h - 'A') + 10 : 
						'a' <= h && h <= 'f'? (h - 'a') + 10 : 3);
			byteArray[i] = (byte)(0x0FF & ((h << 4) | l));
		}
		return byteArray;
	}
}
