package org.zirco.model;

public class HistoryItem {

	private long mId;
	private String mTitle;
	private String mUrl;
	
	public HistoryItem(long id, String title, String url) {
		mId = id;
		mTitle = title;
		mUrl = url;
	}

	public long getId() {
		return mId;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getUrl() {
		return mUrl;
	}
	
}
