package org.zirco.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class ZircoWebView extends WebView {
	
	private int mProgress = 100;
	
	public ZircoWebView(Context context) {
		super(context);	
	}
	
	public ZircoWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
	}		
	
	public void setProgress(int progress) {
		mProgress = progress;
	}
	
	public int getProgress() {
		return mProgress;
	}
	
	public void notifyPageFinished() {
		mProgress = 100;
	}
	
	/*
	
	private void initializeProgress() {
		mProgress = 0;
	} 
	
	@Override
	public void loadUrl(String url) {
		initializeProgress();
		super.loadUrl(url);
	}

	@Override
	public void loadData(String data, String mimeType, String encoding) {
		initializeProgress();
		super.loadData(data, mimeType, encoding);
	}

	@Override
	public void loadDataWithBaseURL(String baseUrl, String data,
			String mimeType, String encoding, String failUrl) {
		initializeProgress();
		super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, failUrl);
	}
	*/

}
