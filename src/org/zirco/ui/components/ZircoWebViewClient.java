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

import org.zirco.R;
import org.zirco.controllers.Controller;
import org.zirco.events.EventConstants;
import org.zirco.events.EventController;
import org.zirco.utils.ApplicationUtils;
import org.zirco.utils.Constants;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebView.HitTestResult;

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
	public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(view.getResources().getString(R.string.Commons_SslWarningsHeader));
		sb.append("\n\n");
		
		if (error.hasError(SslError.SSL_UNTRUSTED)) {
			sb.append(" - ");
			sb.append(view.getResources().getString(R.string.Commons_SslUntrusted));
			sb.append("\n");
		}
		
		if (error.hasError(SslError.SSL_IDMISMATCH)) {
			sb.append(" - ");
			sb.append(view.getResources().getString(R.string.Commons_SslIDMismatch));
			sb.append("\n");
		}
		
		if (error.hasError(SslError.SSL_EXPIRED)) {
			sb.append(" - ");
			sb.append(view.getResources().getString(R.string.Commons_SslExpired));
			sb.append("\n");
		}
		
		if (error.hasError(SslError.SSL_NOTYETVALID)) {
			sb.append(" - ");
			sb.append(view.getResources().getString(R.string.Commons_SslNotYetValid));
			sb.append("\n");
		}
		
		ApplicationUtils.showContinueCancelDialog(view.getContext(),
				android.R.drawable.ic_dialog_info,
				view.getResources().getString(R.string.Commons_SslWarning),
				sb.toString(),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						handler.proceed();
					}

				},
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						handler.cancel();
					}
		});
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		
		if (url.startsWith("vnd.")) {
			
			EventController.getInstance().fireWebEvent(EventConstants.EVT_VND_URL, url);						
			return true;
			
		} else if (url.startsWith(Constants.URL_ACTION_SEARCH)) {
			String searchTerm = url.replace(Constants.URL_ACTION_SEARCH, "");
			
			String searchUrl = Controller.getInstance().getPreferences().getString(Constants.PREFERENCES_GENERAL_SEARCH_URL, Constants.URL_SEARCH_GOOGLE);
			String newUrl = String.format(searchUrl, searchTerm);
			
			view.loadUrl(newUrl);
			return true;
			
		} else if (view.getHitTestResult().getType() == HitTestResult.EMAIL_TYPE) {
			
			EventController.getInstance().fireWebEvent(EventConstants.EVT_MAILTO_URL, url);
			return true;
			
		} else {
			((ZircoWebView) view).resetLoadedUrl();
			EventController.getInstance().fireWebEvent(EventConstants.EVT_WEB_ON_URL_LOADING, url);				
			return false;
		}
	}

}
