package com.lztek.api.demo; 
 
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context; 
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView; 
import android.widget.TextView; 

public class OtaFileDialog { 
	static interface OnFileSelectListener {
		void onFileSelect(File file);
	} 
 
	public static Dialog createDialog(Context context, final OnFileSelectListener callback){  
		ListView listView = new ListView(context);  
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(listView);
		final Dialog dialog = builder.create();
		dialog.setTitle(R.string.ota_filedlg_title);

		listView.setAdapter(new ApkFileAdaper(context));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {   
				File file =  (File)parent.getItemAtPosition(position);   
				if (file.isFile()){   
					dialog.dismiss();
					callback.onFileSelect(file); 
				} else if(file.isDirectory()){ 
					((ApkFileAdaper)parent.getAdapter()).loadDirectory(file);
				} 
			}
		});
		return dialog;
	}   
	static class ApkFileAdaper extends android.widget.BaseAdapter {  
	
		private  File mRoot;   
		private  List<File> mStrages; 
		
		private Context mContext;  
		private List<File> mItemList; 
		private File mDirectory;
		private File mParentDirectory;
		
		public ApkFileAdaper(Context context) {
			mContext = context;
			mItemList = new ArrayList<File>(); 
			
			mRoot = new File("/"); 
			mStrages = new ArrayList<File>();
			
			mStrages.add(android.os.Environment.getExternalStorageDirectory());
			com.lztek.toolkit.Lztek lztck = com.lztek.toolkit.Lztek.create(context); 
			mStrages.add(new File(lztck.getStorageCardPath()));
			mStrages.add(new File(lztck.getUsbStoragePath())); 

			loadDirectory(mRoot);
		}

		@Override
		public int getCount() {
			return mItemList.size();
		}
		
		@Override
		public Object getItem(int position) { 
			return ( 0 <= position && position < mItemList.size())? 
					mItemList.get(position) : null;
		}

		@Override
		public long getItemId(int position) { 
			return position;
		} 
		
		@Override
		public View getView(int position, View convertView,  ViewGroup parent) {  
			View view = null; 
		    if (null == convertView) { 
		    	view = LayoutInflater.from(mContext).inflate(R.layout.filedlg_row, null);
		    	((ViewGroup)view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS); 
		    } else { 
		    	view = convertView;
		    }
		     
			ImageView ivIcon = (ImageView) view.findViewById(R.id.filedlg_icon);
			TextView tvName = (TextView) view.findViewById(R.id.filedlg_name);
			TextView tvPath = (TextView) view.findViewById(R.id.filedlg_path);  
	
			File file =  mItemList.get(position); 
			String fileName = file.getName();  
			
			int drawableId = R.drawable.filedlg_file_ota;
			if ( file.equals(mRoot)) { 
				drawableId = R.drawable.filedlg_root;
			} else if (null != mParentDirectory && file.equals(mParentDirectory) ) { 
				drawableId = R.drawable.filedlg_folder_up;
				fileName = "..";
			} else if (file.isDirectory()) { 
				drawableId = R.drawable.filedlg_folder;
			}  
			ivIcon.setImageResource(drawableId);
		    tvName.setText(fileName); 
		    tvPath.setText(file.getAbsolutePath());  
			
		    return view;
		} 
	
		void loadDirectory(File directory) { 
			File[] files = null; 
			if (directory.equals(mRoot)) { 
				files = mStrages.toArray(new File[mStrages.size()]);
			} else { 
				files = directory.listFiles(); 
				if(files == null){
					files = new File[]{};
				}  
			}
			
	        mItemList.clear();  
	        mDirectory = directory; 
	        
			mParentDirectory = (mDirectory.equals(mRoot) ||mStrages.contains(mDirectory))? 
					null : mDirectory.getParentFile();  

			mItemList.add(mRoot);
			if (null != mParentDirectory) {
				mItemList.add(mParentDirectory);
			}
			for (File file: files) { 
				if (file.isDirectory()) { 
					mItemList.add(file);
				} else {
					String fileName = file.getName();
					int dix = fileName.lastIndexOf('.');
					if (dix > 0 && fileName.substring(dix).toLowerCase().equals(".zip")) {
						mItemList.add(file);
					}
				}
			}  
			
			this.notifyDataSetChanged();
		}
	} 
}


