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
		
		((ZircoWebView) view).notifyPageStarted();
		
		EventController.getInstance().fireWebEvent(EventConstants.EVT_WEB_ON_PAGE_STARTED, url);
		
		super.onPageStarted(view, url, favicon);
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		
		if (url.startsWith("vnd.youtube")) {
			
			EventController.getInstance().fireWebEvent(EventConstants.EVT_YOUTUBE_VIDEO, url);						
			return true;
			
		} else {
		
			EventController.getInstance().fireWebEvent(EventConstants.EVT_WEB_ON_URL_LOADING, url);				
			return false;
		}
	}

}
