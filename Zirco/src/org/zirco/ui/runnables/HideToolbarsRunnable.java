package org.zirco.ui.runnables;

import org.zirco.ui.IToolbarsContainer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HideToolbarsRunnable implements Runnable {
	
	private final static String TAG = "HideToolbarsThread";
	
	private static final int DELAY = 3000;
	
	private IToolbarsContainer mParent;
	private boolean mDisabled;
	
	public HideToolbarsRunnable(IToolbarsContainer parent) {
		mParent = parent;
		mDisabled = false;
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
			
			Thread.sleep(DELAY);
			
			mHandler.sendEmptyMessage(0);
			
		} catch (InterruptedException e) {
			Log.w(TAG, "Exception in thread: " + e.getMessage());
			
			mHandler.sendEmptyMessage(0);
		}
	}

}
