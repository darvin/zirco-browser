package org.zirco.ui.runnables;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.zirco.ui.IDownloadListener;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class DownloadRunnable implements Runnable {
	
	private static final String DOWNLOAD_FOLFER = "zirco-downloads";
			
	private IDownloadListener mParent;
	private String mUrl;
	
	private boolean mResultOk;
	private String mErrorMessage;
	
	public DownloadRunnable(IDownloadListener parent, String url) {
		mParent = parent;
		mUrl = url;
		
		mResultOk = false;
		mErrorMessage = "";
	}
	
	private Handler mHandler = new Handler() {				
		
		public void handleMessage(Message msg) {
			if (mParent != null) {
				mParent.onDownloadEnd(mUrl, mResultOk, mErrorMessage);
			}
		}
	};
	
	private String getFileNameFromUrl() {
		return mUrl.substring(mUrl.lastIndexOf("/") + 1);
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
			mResultOk = false;
			mErrorMessage = "SD Card is not writeable.";			
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
				
				URL url = new URL(mUrl);
				URLConnection conn = url.openConnection();
								
				bis = new BufferedInputStream( conn.getInputStream() );				
				bos = new BufferedOutputStream(new FileOutputStream(downloadFile));
				
				int i;
	            while ((i = bis.read()) != -1) {
	               bos.write( i );
	            }

			} catch (MalformedURLException mue) {
				mResultOk = false;
				mErrorMessage = mue.getMessage();
			} catch (IOException ioe) {
				mResultOk = false;
				mErrorMessage = ioe.getMessage();
			} finally {
				mResultOk = true;
				
				if (bis != null) {
					try {
						bis.close();
					} catch (IOException ioe) {
						mResultOk = false;
						mErrorMessage = ioe.getMessage();
					}
				}
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException ioe) {
						mResultOk = false;
						mErrorMessage = ioe.getMessage();
					}
				}							
			}
			
		} 
		
		mHandler.sendEmptyMessage(0);
	}

}
