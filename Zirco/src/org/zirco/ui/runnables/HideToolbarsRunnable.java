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

package org.zirco.ui.runnables;

import org.zirco.ui.activities.IToolbarsContainer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HideToolbarsRunnable implements Runnable {
	
	private final static String TAG = "HideToolbarsRunnable";
	
	private IToolbarsContainer mParent;
	private boolean mDisabled;
	private int mDelay;
	
	public HideToolbarsRunnable(IToolbarsContainer parent, int delay) {
		mParent = parent;
		mDisabled = false;
		mDelay = delay;
	}
	
	private Handler mHandler = new Handler() {				
		
		public void handleMessage(Message msg) {
			if ((mParent != null) &&
					(!mDisabled)) {
				mParent.hideToolbars();
			}
		}
	};
	
	public void setDisabled() {
		mDisabled = true;
	}
	
	public void run() {
		try {
			
			Thread.sleep(mDelay);
			
			mHandler.sendEmptyMessage(0);
			
		} catch (InterruptedException e) {
			Log.w(TAG, "Exception in thread: " + e.getMessage());
			
			mHandler.sendEmptyMessage(0);
		}
	}

}
