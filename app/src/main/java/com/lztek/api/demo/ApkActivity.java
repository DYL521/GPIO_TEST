package com.lztek.api.demo; 

import java.io.File;

import android.app.Activity;  
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;  
import android.os.Handler; 
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View; 
import android.view.ViewGroup; 
import android.widget.BaseAdapter;
import android.widget.Button; 
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView; 
import android.widget.TextView;
import android.widget.Toast;

public class ApkActivity extends Activity implements 
	View.OnClickListener, 
	View.OnKeyListener, 
	CompoundButton.OnCheckedChangeListener { 
	
	 
	private ListView mListView;  
	private Button mBtnInstallApk;
	private Button mBtnUninstall; 
	private CheckBox mChbSystemApp;  
	
	private Handler mHandler = null; 
	
	private BroadcastReceiver mPackageChangedReceiver = null;
	
	private com.lztek.toolkit.Lztek mLztek; 


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);    

		//requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);  
		setContentView(R.layout.apk_layout);   
		setTitle(R.string.apk_demo);

		mLztek = com.lztek.toolkit.Lztek.create(this); 
		
		mHandler = new Handler(); 
		
		mListView = (ListView)findViewById(R.id.app_list);
		mBtnInstallApk = (Button)findViewById(R.id.install_apk);
		mBtnUninstall = (Button)findViewById(R.id.uninstall_apk);
		mChbSystemApp = (CheckBox)findViewById(R.id.checkbox_systemapp);
 
		mBtnInstallApk.setOnClickListener(this);
		mBtnUninstall.setOnClickListener(this);
		mChbSystemApp.setOnCheckedChangeListener(this);  
		
		mListView.setAdapter(new PackageInfoAdaper(this, mListView));  
		mListView.setOnKeyListener(this); 
		
		mPackageChangedReceiver = new  BroadcastReceiver() { 
			@Override
			public void onReceive(android.content.Context context, Intent intent) {
				onPackageChanged(intent);  
			}
		};

		android.content.IntentFilter filter = new android.content.IntentFilter(); 
	    filter.addAction(Intent.ACTION_PACKAGE_ADDED);   
	    filter.addAction(Intent.ACTION_PACKAGE_REPLACED);  
	    filter.addAction(Intent.ACTION_PACKAGE_REMOVED);  
	    filter.addDataScheme("package");
	    this.registerReceiver(mPackageChangedReceiver, filter);
	} 
	
	@Override
	protected void onDestroy() { 
		super.onDestroy(); 
		if (null != mPackageChangedReceiver) { 
			this.unregisterReceiver(mPackageChangedReceiver);
		}
		mHandler.removeCallbacksAndMessages(null); 
	} 
	
	@Override
	public void onClick(View v) {  
		if (v == null) { 
		} else if (v == mBtnInstallApk) { 
			onBtnInstallApk();
		} else if (v == mBtnUninstall) { 
			onBtnUninstall(); 
		} 
	}  

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
		if (buttonView == mChbSystemApp) {
			((PackageInfoAdaper)mListView.getAdapter()).showSystemApp(isChecked); 
		}
	}  

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (v == mListView && keyCode == KeyEvent.KEYCODE_ENTER && 
				event.getAction() == KeyEvent.ACTION_DOWN) {  
			View rowView = mListView.getSelectedView(); 
			CheckBox rbSelect = null == rowView? 
					null : (CheckBox)rowView.findViewById(R.id.app_checkbox);
			if (null != v) {
				rbSelect.setChecked(!rbSelect.isChecked());
			}
		}
		return false;
	}

	private android.app.Dialog mApkFileDlg;
	private void onBtnInstallApk() {  
		if (null != mApkFileDlg) { 
			return;
		} 
		mApkFileDlg = ApkFileDialog.createDialog(this, new ApkFileDialog.OnFileSelectListener() {
			@Override
			public void onFileSelect(File file) {  
				if (!file.exists() || !file.isFile() || file.length() == 0) { 
					throw new IllegalArgumentException("Invalid apk file");
				}   
				if (null == ApkActivity.this.getPackageManager().getPackageArchiveInfo(
						file.getAbsolutePath(), 0)) { 
					Toast.makeText(ApkActivity.this, 
							"Invalid apk file, cannot read package info", Toast.LENGTH_LONG).show(); 
					return;
				}  
				
				mLztek.installApplication(file.getAbsolutePath()); 
			}
		}); 
		mApkFileDlg.show();
		mApkFileDlg.setOnDismissListener(new android.content.DialogInterface.OnDismissListener() { 
			@Override
			public void onDismiss(DialogInterface dialog) { 
				mApkFileDlg = null;
			}
		});
	} 
	
	private void onBtnUninstall() {   
		String packageName = ((PackageInfoAdaper)mListView.getAdapter()).getSelectPackageName(); 
		if (null != packageName && packageName.length() > 0) {  
			mLztek.uninstallApplication(packageName); 
		} 
	} 
	
	private void onPackageChanged(Intent intent) {   
		((PackageInfoAdaper)mListView.getAdapter()).reloadPackageInfo();
		
		String action = intent.getAction(); 
		String packageName = intent.getData().getSchemeSpecificPart();
		
		String message = "Package changed!";  
		if (action.equals(Intent.ACTION_PACKAGE_ADDED) || 
				action.equals(Intent.ACTION_PACKAGE_REPLACED)) {  
			message = "Package " + packageName + " install ok"; 
		} else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {  
			message = "Package " + packageName + " removed";  
		} 
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();  
	}
} 

class PackageInfoAdaper extends BaseAdapter implements   
	CompoundButton.OnCheckedChangeListener {  
 
	private Context mContext; 
	private PackageManager mPackageManager;
	private java.util.List<ResolveInfo> mItemList;  
	private boolean mShowSystemApp;
	private String mSelectPackageName;
	
	ListView mListView;
	
	public PackageInfoAdaper(Context context, ListView listView) { 
		mContext = context;
		mListView = listView;
		mPackageManager = mContext.getPackageManager();  
		
        mItemList = new java.util.ArrayList<ResolveInfo>(); 
        
    	mShowSystemApp = false;
    	mSelectPackageName = "";  
    	
    	loadPackageInfo();
	} 
	
	@Override
	public int getCount() {
		return mItemList.size();
	}

	@Override
	public Object getItem(int position) { 
		return ( 0 <= position && position < mItemList.size())? 
				mItemList.get(position).activityInfo.packageName : null;
	}

	public long getItemId(int position) { 
		return position;
	} 

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {  
		View view = null; 
        if (null == convertView) {   
        	view = LayoutInflater.from(mContext).inflate(R.layout.apk_row, null);
        	((ViewGroup)view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS); 
        } else { 
        	view = convertView;
        } 
        
    	ImageView ivIcon = (ImageView) view.findViewById(R.id.app_icon);
    	TextView tvAppName = (TextView) view.findViewById(R.id.app_name);
    	TextView tvPackageName = (TextView) view.findViewById(R.id.app_package_name); 
		CheckBox rbSelect = (CheckBox) view.findViewById(R.id.app_checkbox); 
		
		rbSelect.setOnCheckedChangeListener(null);
		rbSelect.setTag(null);
		
		ResolveInfo info = ( 0 <= position && position < mItemList.size())? 
				mItemList.get(position) : null; 
		
		String packageName = info.activityInfo.packageName;
		
	    ivIcon.setImageDrawable(info.activityInfo.loadIcon(mPackageManager));
		tvAppName.setText(info.loadLabel(mPackageManager).toString()); 
		tvPackageName.setText(packageName); 
		
		rbSelect.setEnabled((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0);
		
		rbSelect.setChecked(null != mSelectPackageName && mSelectPackageName.equals(packageName));
		rbSelect.setOnCheckedChangeListener(this);
		rbSelect.setTag(info);
		
        return view;  
	} 

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {  
		Object obj = null == buttonView? null : buttonView.getTag();
		if (isChecked && null != obj && obj instanceof ResolveInfo && 
				(((ResolveInfo)obj).activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {  
			mSelectPackageName = ((ResolveInfo)obj).activityInfo.packageName;  
			 
			int childCount = mListView.getChildCount(); 
			for (int i=0; i<childCount; ++i) {
				View view = mListView.getChildAt(i);
				View checkBox = view.findViewById(R.id.app_checkbox);
				if (null != checkBox && checkBox != buttonView && 
						checkBox instanceof CheckBox && ((CheckBox)checkBox).isChecked()) { 
					((CheckBox)checkBox).setChecked(false);
				}
			}
		}  
	}
	
	private void loadPackageInfo() { 
        mItemList.clear(); 
        
	    Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        java.util.List<ResolveInfo> infoList = mPackageManager.queryIntentActivities(intent, 0); 
        for (ResolveInfo info : infoList) {
        	if (mShowSystemApp || 
        			(info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { 
                mItemList.add(info); 
            } 
        } 
        java.util.Collections.sort(mItemList, new java.util.Comparator<ResolveInfo>(){
        	@Override
        	public int compare(ResolveInfo lhs, ResolveInfo rhs) { 
        		//int ls = (lhs.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM);
        		//int rs = (rhs.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM); 
        		String lname = lhs.loadLabel(mPackageManager).toString();
        		String rname = rhs.loadLabel(mPackageManager).toString();
        		
        		// return ls != rs? (0 != ls? 1 : -1) : lname.compareToIgnoreCase(rname);
        		return  lname.compareToIgnoreCase(rname);
        	}
        });  
	}

	public void reloadPackageInfo( ) {  
		loadPackageInfo();
		if (null != mSelectPackageName && mSelectPackageName.length() > 0) {  
			int size = mItemList.size();
			int i = 0;
	        for (i=0; i<size; ++i) {  
                if (mSelectPackageName.equals(mItemList.get(i).activityInfo.packageName)) { 
                	break;
                } 
	        }
	        if (i >= size) {
	        	mSelectPackageName = "";
	        }
		} 
        this.notifyDataSetChanged();
	}
	
	public void showSystemApp(boolean showSystemApp) {
		if (mShowSystemApp != showSystemApp) {
			mShowSystemApp = showSystemApp;
			reloadPackageInfo();
		}
	}
	
	public String getSelectPackageName() {
		return mSelectPackageName;
	}  
} 
