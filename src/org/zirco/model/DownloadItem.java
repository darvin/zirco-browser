/*
 * Zirco Browser for Android
 * 
 * Copyright (C) 2010 J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.zirco.model;

import org.zirco.events.EventConstants;
import org.zirco.events.EventController;
import org.zirco.ui.runnables.DownloadRunnable;

/**
 * Represent a download item.
 */
public class DownloadItem {
	
	private String mUrl;
	private String mFileName;
	
	private int mProgress;
	private int mTotal;
	
	private String mErrorMessage;
	
	private DownloadRunnable mRunnable;
	
	private boolean mIsFinished;
	private boolean mIsAborted;
	
	/**
	 * Constructor.
	 * @param url The download url.
	 */
	public DownloadItem(String url) {
		mUrl = url;
		mFileName = mUrl.substring(mUrl.lastIndexOf("/") + 1);
		
		mProgress = 0;
		mTotal = 0;
	
		mRunnable = null;
		mErrorMessage = null;
		
		mIsFinished = false;
		mIsAborted = false;
	}
	
	/**
	 * Gets the download url.
	 * @return The download url.
	 */
	public String getUrl() {
		return mUrl;
	}
	
	/**
	 * Gets the filename on disk.
	 * @return The filename on disk.
	 */
	public String getFileName() {
		return mFileName;
	}
	
	/**
	 * Gets the download progress.
	 * @return The download progress.
	 */
	public int getProgress() {
		return mProgress;
	}
	
	/**
	 * Gets the total size.
	 * @return The total siez.
	 */
	public int getTotalSize() {
		return mTotal;
	}
	
	/**
	 * Set the current error message for this download.
	 * @param errorMessage The error message.
	 */
	public void setErrorMessage(String errorMessage) {
		mErrorMessage = errorMessage;
	}
	
	/**
	 * Gets the error message for this download.
	 * @return The error message.
	 */
	public String getErrorMessage() {
		return mErrorMessage;
	}
	
	/**
	 * Trigger a start download event.
	 */
	public void onStart() {				
		EventController.getInstance().fireDownloadEvent(EventConstants.EVT_DOWNLOAD_ON_START, this);
	}
	
	/**
	 * Initialize progress and total size.
	 * @param size The total size.
	 */
	public void onSetSize(int size) {
		mProgress = 0;
		mTotal = size;
	}
	
	/**
	 * Set this item is download finished state. Trigger a finished download event.
	 */
	public void onFinished() {
		mProgress = mTotal;
		mRunnable = null;
		
		mIsFinished = true;
		
		EventController.getInstance().fireDownloadEvent(EventConstants.EVT_DOWNLOAD_ON_FINISHED, this);
	}
	
	/**
	 * Set the current progress. Trigger a progress download event.
	 * @param progress The current progress.
	 */
	public void onProgress(int progress) {
		mProgress = progress;
		
		EventController.getInstance().fireDownloadEvent(EventConstants.EVT_DOWNLOAD_ON_PROGRESS, this);
	}
	
	/**
	 * Start the current download.
	 */
	public void startDownload() {
		if (mRunnable != null) {
			mRunnable.abort();
		}
		mRunnable = new DownloadRunnable(this);
		new Thread(mRunnable).start();
	}
	
	/**
	 * Abort the current download.
	 */
	public void abortDownload() {
		if (mRunnable != null) {
			mRunnable.abort();
		}
		mIsAborted = true;
	}
	
	/**
	 * Check if the download is finished.
	 * @return True if the download is finished.
	 */
	public boolean isFinished() {
		return mIsFinished;
	}
	
	/**
	 * Check if the download is aborted.
	 * @return True if the download is aborted.
	 */
	public boolean isAborted() {
		return mIsAborted;
	}

}
