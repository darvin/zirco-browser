package org.zirco.model;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.zirco.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadListAdapter extends BaseAdapter {

	private Context mContext;
	private List<DownloadItem> mDownloads;
	
	private Map<DownloadItem, ProgressBar> mBarMap;
	
	public DownloadListAdapter(Context context, List<DownloadItem> downloads) {
		mContext = context;
		mDownloads = downloads;
		mBarMap = new Hashtable<DownloadItem, ProgressBar>();
	}
	
	public Map<DownloadItem, ProgressBar> getBarMap() {
		return mBarMap;
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
		
		DownloadItem item = mDownloads.get(position);
		
		ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.DownloadRow_ProgressBar);
		TextView fileNameView = (TextView) convertView.findViewById(R.id.DownloadRow_FileName);
		TextView urlView = (TextView) convertView.findViewById(R.id.DownloadRow_Url);
		
		progressBar.setIndeterminate(false);
		progressBar.setMax(item.getTotalSize());
		progressBar.setProgress(item.getProgress());		
		
		fileNameView.setText(item.getFileName());
		urlView.setText(item.getUrl());
		
		mBarMap.put(item, progressBar);
		
		return convertView;
	}

}
