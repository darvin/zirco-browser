package org.zirco.ui.runnables;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.zirco.model.DownloadItem;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class DownloadRunnable implements Runnable {
	
	private static final String DOWNLOAD_FOLFER = "zirco-downloads";
			
	private DownloadItem mParent;
	
	private boolean mAborted;
	
	public DownloadRunnable(DownloadItem parent) {
		mParent = parent;
		mAborted = false;
	}
	
	private Handler mHandler = new Handler() {				
		
		public void handleMessage(Message msg) {
			mParent.onFinished();
		}
	};
	
	private String getFileNameFromUrl() {
		return mParent.getUrl().substring(mParent.getUrl().lastIndexOf("/") + 1);
	}
	
	private File getFile() {
		File root = Environment.getExternalStorageDirectory();
		if (root.canWrite()) {
			
			File folder = new File(root, DOWNLOAD_FOLFER);
			
			if (!folder.exists()) {
				folder.mkdir();
			}
			
			return new File(folder, getFileNameFromUrl());
			
		} else {
			mParent.setErrorMessage("SD Card is not writeable.");			
			return null;
		}
	}
	
	@Override
	public void run() {
		File downloadFile = getFile();
		
		if (downloadFile != null) {
			
			if (downloadFile.exists()) {
				downloadFile.delete();
			}
			
			BufferedInputStream bis = null;
			BufferedOutputStream bos = null;
			
			try {
				
				URL url = new URL(mParent.getUrl());
				URLConnection conn = url.openConnection();
				
				InputStream is = conn.getInputStream();
								
				mParent.onStart(conn.getContentLength());
				
				bis = new BufferedInputStream( is );				
				bos = new BufferedOutputStream(new FileOutputStream(downloadFile));
				
				int i;
				int stepCount = 0;
				int delta = 0;
	            while (((i = bis.read()) != -1) &&
	            		(!mAborted)) {
	               bos.write( i );
	               
	               delta += i;
	               stepCount++;
	               
	               if (stepCount > 10) {
	            	   mParent.onProgress(delta);
	            	   stepCount = 0;
	            	   delta = 0;
	               }
	            }

			} catch (MalformedURLException mue) {
				mParent.setErrorMessage(mue.getMessage());
			} catch (IOException ioe) {
				mParent.setErrorMessage(ioe.getMessage());
			} finally {;
				
				if (bis != null) {
					try {
						bis.close();
					} catch (IOException ioe) {
						mParent.setErrorMessage(ioe.getMessage());
					}
				}
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException ioe) {
						mParent.setErrorMessage(ioe.getMessage());
					}
				}							
			}
			
		} 
		
		mHandler.sendEmptyMessage(0);
	}
	
	public void abort() {
		mAborted = true;
	}

}
