package com.lztek.api.demo; 

import java.io.File;

import android.app.Activity;  
import android.content.DialogInterface;
import android.os.Bundle;  
import android.widget.Toast;

public class OtaUpdateActivity extends Activity  { 
	
	  
	
	private com.lztek.toolkit.Lztek mLztek; 


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);    

		//requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);  
		setContentView(R.layout.ota_layout);   
		setTitle(R.string.ota_demo);

		mLztek = com.lztek.toolkit.Lztek.create(this);  
		onBtnUpdateOta();
	} 
	
	@Override
	protected void onDestroy() { 
		super.onDestroy(); 
	} 

	private android.app.Dialog mOtaFileDlg;
	private void onBtnUpdateOta() {  
		if (null != mOtaFileDlg) { 
			return;
		} 
		mOtaFileDlg = OtaFileDialog.createDialog(this, new OtaFileDialog.OnFileSelectListener() {
			@Override
			public void onFileSelect(File file) {  
				if (!file.exists() || !file.isFile() || file.length() == 0) { 
					throw new IllegalArgumentException("Invalid ota file");
				}  
				
				Toast.makeText(OtaUpdateActivity.this,  
						"Update ota file: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();  
				
				mLztek.updateSystem(file.getAbsolutePath()); 
			}
		}); 
		mOtaFileDlg.show();
		mOtaFileDlg.setOnDismissListener(new android.content.DialogInterface.OnDismissListener() { 
			@Override
			public void onDismiss(DialogInterface dialog) { 
				mOtaFileDlg = null;
				finish();
			}
		});
	}
} 

