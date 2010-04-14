package org.zirco.model;

import org.zirco.events.EventConstants;
import org.zirco.events.EventController;
import org.zirco.ui.runnables.DownloadRunnable;

public class DownloadItem {
	
	private String mUrl;
	private String mFileName;
	
	private int mProgress;
	private int mTotal;
	
	private String mErrorMessage;
	
	private DownloadRunnable mRunnable;
	
	public DownloadItem(String url) {
		mUrl = url;
		mFileName = mUrl.substring(mUrl.lastIndexOf("/") + 1);
		
		mProgress = 0;
		mTotal = 0;
	
		mRunnable = null;
		mErrorMessage = null;
	}
	
	public String getUrl() {
		return mUrl;
	}
	
	public String getFileName() {
		return mFileName;
	}
	
	public int getProgress() {
		return mProgress;
	}
	
	public int getTotalSize() {
		return mTotal;
	}
	
	public void setErrorMessage(String errorMessage) {
		mErrorMessage = errorMessage;
	}
	
	public String getErrorMessage() {
		return mErrorMessage;
	}
	
	public void onStart() {				
		EventController.getInstance().fireDownloadEvent(EventConstants.EVT_DOWNLOAD_ON_START, this);
	}
	
	public void onSetSize(int size) {
		mProgress = 0;
		mTotal = size;
	}
	
	public void onFinished() {
		mProgress = mTotal;
		mRunnable = null;
		
		EventController.getInstance().fireDownloadEvent(EventConstants.EVT_DOWNLOAD_ON_FINISHED, this);
	}
	
	public void onProgress(int progress) {
		mProgress = progress;
		
		EventController.getInstance().fireDownloadEvent(EventConstants.EVT_DOWNLOAD_ON_PROGRESS, this);
	}
	
	public void startDownload() {
		if (mRunnable != null) {
			mRunnable.abort();
		}
		mRunnable = new DownloadRunnable(this);
		new Thread(mRunnable).start();
	}

}
