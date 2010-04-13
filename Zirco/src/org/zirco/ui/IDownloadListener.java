package org.zirco.ui;

public interface IDownloadListener {
	
	public void onDownloadEnd(String url, boolean resultOk, String errorMessage);

}
