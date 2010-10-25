/*
 * Zirco Browser for Android
 * 
 * Copyright (C) 2010 J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.zirco.ui.components;

import org.zirco.events.EventConstants;
import org.zirco.events.EventController;
import org.zirco.utils.ApplicationUtils;
import org.zirco.utils.Constants;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Convenient extension of WebViewClient.
 */
public class ZircoWebViewClient extends WebViewClient {
	
	@Override
	public void onPageFinished(WebView view, String url) {
		
		((ZircoWebView) view).notifyPageFinished();
		
		EventController.getInstance().fireWebEvent(EventConstants.EVT_WEB_ON_PAGE_FINISHED, url);		
		
		super.onPageFinished(view, url);
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		
		// Some magic here: when performing WebView.loadDataWithBaseURL, the url is "file:///android_asset/startpage,
		// whereas when the doing a "previous" or "next", the url is "about:start", and we need to perform the
		// loadDataWithBaseURL here, otherwise it won't load.
		if (url.equals(Constants.URL_ABOUT_START)) {
			view.loadDataWithBaseURL("file:///android_asset/startpage/",
					ApplicationUtils.getStartPage(view.getContext()), "text/html", "UTF-8", "about:start");
		}
		
		((ZircoWebView) view).notifyPageStarted();
		
		EventController.getInstance().fireWebEvent(EventConstants.EVT_WEB_ON_PAGE_STARTED, url);
		
		super.onPageStarted(view, url, favicon);
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		
		if (url.startsWith("vnd.")) {
			
			EventController.getInstance().fireWebEvent(EventConstants.EVT_VND_URL, url);						
			return true;
			
		} else {
		
			((ZircoWebView) view).resetLoadedUrl();
			EventController.getInstance().fireWebEvent(EventConstants.EVT_WEB_ON_URL_LOADING, url);				
			return false;
		}
	}

}
