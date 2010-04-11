package org.zirco.ui.runnables;

import org.zirco.model.DbAdapter;

import android.content.Context;

public class HistoryUpdater implements Runnable {

	private Context mContext;
	private String mTitle;
	private String mUrl;
	
	public HistoryUpdater(Context context, String title, String url) {
		mContext = context;
		mTitle = title;
		mUrl = url;
	}
	
	@Override
	public void run() {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		
		dbAdapter.updateHistory(mTitle, mUrl);
		
		dbAdapter.close();
	}

}
