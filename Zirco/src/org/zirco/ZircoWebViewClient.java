package org.zirco;

import org.zirco.events.EventConstants;
import org.zirco.events.EventController;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ZircoWebViewClient extends WebViewClient {

	@Override
	public void onPageFinished(WebView view, String url) {
		
		EventController.getInstance().fireWebEvent(EventConstants.EVT_WEB_ON_PAGE_FINISHED, url);
		
		super.onPageFinished(view, url);
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		
		EventController.getInstance().fireWebEvent(EventConstants.EVT_WEB_ON_PAGE_STARTED, url);
		
		super.onPageStarted(view, url, favicon);
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		
		EventController.getInstance().fireWebEvent(EventConstants.EVT_WEB_ON_URL_LOADING, url);
		
		view.loadUrl(url);
		
		return true;
	}

}
