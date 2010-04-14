package org.zirco.ui.activities;

import org.zirco.R;
import org.zirco.controllers.Controller;
import org.zirco.events.EventConstants;
import org.zirco.events.EventController;
import org.zirco.events.IDownloadEventsListener;
import org.zirco.model.DownloadItem;
import org.zirco.model.DownloadListAdapter;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

public class DownloadsListActivity extends ListActivity implements IDownloadEventsListener {

	private DownloadListAdapter mAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloadslistactivity);
        
        setTitle(R.string.DownloadListActivity_Title);
        
        EventController.getInstance().addDownloadListener(this);
        
        fillData();
	}
	
	private void fillData() {
		mAdapter = new DownloadListAdapter(this, Controller.getInstance().getDownloadList());
		setListAdapter(mAdapter);
	}

	@Override
	public void onDownloadbEvent(String event, Object data) {
		if (event.equals(EventConstants.EVT_DOWNLOAD_ON_START)) {
			fillData();
		} else if (event.equals(EventConstants.EVT_DOWNLOAD_ON_PROGRESS)) {				
			if (data != null) {
				DownloadItem item = (DownloadItem) data;
				ProgressBar bar = mAdapter.getBarMap().get(item);
				if (bar != null) {
					bar.setMax(item.getTotalSize());
					bar.setProgress(item.getProgress());
				}				
			}
		}
		
	}
	
}
