package org.zirco.ui;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HideToolbarsRunnable implements Runnable {
	
	private final static String TAG = "HideToolbarsThread";
	
	private static final int DELAY = 3000;
	
	private IToolbarsContainer mParent;
	
	public HideToolbarsRunnable(IToolbarsContainer parent) {
		mParent = parent;
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (mParent != null) {
				mParent.hideToolbars();
			}
		}
	};
	
	public void run() {
		try {
			
			Thread.sleep(DELAY);
			
			mHandler.sendEmptyMessage(0);
			
		} catch (InterruptedException e) {
			Log.w(TAG, "Exception in thread: " + e.getMessage());
			
			mHandler.sendEmptyMessage(0);
		}
	}

}
