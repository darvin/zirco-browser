package org.zirco.model;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.zirco.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadListAdapter extends BaseAdapter {

	private Context mContext;
	private List<DownloadItem> mDownloads;
		
	private Map<DownloadItem, TextView> mTitleMap;
	private Map<DownloadItem, ProgressBar> mBarMap;
	private Map<DownloadItem, ImageButton> mButtonMap;
	
	public DownloadListAdapter(Context context, List<DownloadItem> downloads) {
		mContext = context;
		mDownloads = downloads;
		mTitleMap = new Hashtable<DownloadItem, TextView>();
		mBarMap = new Hashtable<DownloadItem, ProgressBar>();
		mButtonMap = new Hashtable<DownloadItem, ImageButton>();
	}
	
	public Map<DownloadItem, TextView> getTitleMap() {
		return mTitleMap;
	}
	
	public Map<DownloadItem, ProgressBar> getBarMap() {
		return mBarMap;
	}
	
	public Map<DownloadItem, ImageButton> getButtonMap() {
		return mButtonMap;
	}
	
	@Override
	public int getCount() {
		return mDownloads.size();
	}

	@Override
	public Object getItem(int position) {		
		return mDownloads.get(position);
	}

	@Override
	public long getItemId(int position) {		
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.downloadrow, null);
		}
		
		final DownloadItem item = mDownloads.get(position);
		
		final ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.DownloadRow_ProgressBar);
		final TextView fileNameView = (TextView) convertView.findViewById(R.id.DownloadRow_FileName);
		TextView urlView = (TextView) convertView.findViewById(R.id.DownloadRow_Url);
		final ImageButton stopButton = (ImageButton) convertView.findViewById(R.id.DownloadRow_StopBtn);
		
		progressBar.setIndeterminate(false);
		progressBar.setMax(item.getTotalSize());
		progressBar.setProgress(item.getProgress());		
		
		if (item.isAborted()) {
			fileNameView.setText(String.format(mContext.getResources().getString(R.string.DownloadListActivity_Aborted), item.getFileName()));
			stopButton.setEnabled(false);
		} else if (item.isFinished()) {
			fileNameView.setText(String.format(mContext.getResources().getString(R.string.DownloadListActivity_Finished), item.getFileName()));
			stopButton.setEnabled(false);
		} else {
			fileNameView.setText(item.getFileName());
		}
		urlView.setText(item.getUrl());
				
		
		stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				item.abortDownload();
				stopButton.setEnabled(false);
				fileNameView.setText(String.format(mContext.getResources().getString(R.string.DownloadListActivity_Aborted), item.getFileName()));
				progressBar.setProgress(progressBar.getMax());
			}
			
		});				
		
		mTitleMap.put(item, fileNameView);
		mBarMap.put(item, progressBar);
		mButtonMap.put(item, stopButton);
		
		return convertView;
	}

}
