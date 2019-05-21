package com.lztek.api.demo;
 

import com.lztek.api.demo.R;

import android.app.Activity; 
import android.os.Bundle; 
import android.view.View;
import android.widget.Button;

public class DensityActivity extends Activity {

	private android.util.SparseIntArray mBtnIdMap; 
	
	private com.lztek.toolkit.Lztek mLztek;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.density_layout);
		setTitle(R.string.density_demo); 
		
		mLztek = com.lztek.toolkit.Lztek.create(this); 
		

		mBtnIdMap = new android.util.SparseIntArray();
		mBtnIdMap.put(R.id.dpi_120, 120);
		mBtnIdMap.put(R.id.dpi_160, 160);
		mBtnIdMap.put(R.id.dpi_240, 240);
		mBtnIdMap.put(R.id.dpi_320, 320);
		mBtnIdMap.put(R.id.dpi_480, 480);  
		

		int density = mLztek.getDisplayDensity();
		for (int i=0; i<mBtnIdMap.size(); ++i) { 
			Button button = (Button)findViewById(mBtnIdMap.keyAt(i));
			button.setOnClickListener(new View.OnClickListener() { 
				@Override
				public void onClick(View v) { 
					int value = mBtnIdMap.get(v.getId());
					mLztek.setDisplayDensity(value);
					
					int density = mLztek.getDisplayDensity();
					for (int i=0; i<mBtnIdMap.size(); ++i) { 
						((Button)findViewById(mBtnIdMap.keyAt(i))).setEnabled(
								density != mBtnIdMap.valueAt(i)); 
					}
				}
			});
			button.setEnabled(density != mBtnIdMap.valueAt(i)); 
		}  
	} 
	
	@Override
	protected void onDestroy() { 
		super.onDestroy();
	}
	
	@Override
	protected void onPause() { 
		super.onPause(); 
	} 
}
