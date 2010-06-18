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

import org.zirco.controllers.Controller;
import org.zirco.utils.Constants;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * A convenient extension of WebView.
 */
public class ZircoWebView extends WebView {
	
	private int mProgress = 100;
	
	private boolean mIsLoading = false;
	
	/**
	 * Constructor.
	 * @param context The current context.
	 */
	public ZircoWebView(Context context) {
		super(context);
		
		initializeOptions();
	}
	
	/**
	 * Constructor.
	 * @param context The current context.
	 * @param attrs The attribute set.
	 */
	public ZircoWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        initializeOptions();
	}	
	
	/**
	 * Initialize the WebView with the options set by the user through preferences.
	 */
	public void initializeOptions() {
		WebSettings settings = getSettings();
		
		// User settings		
		settings.setJavaScriptEnabled(Controller.getInstance().getPreferences().getBoolean(Constants.PREFERENCES_BROWSER_ENABLE_JAVASCRIPT, true));
		settings.setLoadsImagesAutomatically(Controller.getInstance().getPreferences().getBoolean(Constants.PREFERENCES_BROWSER_ENABLE_IMAGES, true));
		settings.setSaveFormData(Controller.getInstance().getPreferences().getBoolean(Constants.PREFERENCES_BROWSER_ENABLE_FORM_DATA, true));
		settings.setSavePassword(Controller.getInstance().getPreferences().getBoolean(Constants.PREFERENCES_BROWSER_ENABLE_PASSWORDS, true));
		
		CookieManager.getInstance().setAcceptCookie(Controller.getInstance().getPreferences().getBoolean(Constants.PREFERENCES_BROWSER_ENABLE_COOKIES, true));
		
		settings.setSupportZoom(true);
		
		// Technical settings
		settings.setSupportMultipleWindows(true);						
    	setLongClickable(true);
	}
	
	/**
	 * Set the current loading progress of this view.
	 * @param progress The current loading progress.
	 */
	public void setProgress(int progress) {
		mProgress = progress;
	}
	
	/**
	 * Get the current loading progress of the view.
	 * @return The current loading progress of the view.
	 */
	public int getProgress() {
		return mProgress;
	}
	
	/**
	 * Triggered when a new page loading is requested.
	 */
	public void notifyPageStarted() {
		mIsLoading = true;
	}
	
	/**
	 * Triggered when the page has finished loading.
	 */
	public void notifyPageFinished() {
		mProgress = 100;
		mIsLoading = false;
	}
	
	/**
	 * Check if the view is currently loading.
	 * @return True if the view is currently loading.
	 */
	public boolean isLoading() {
		return mIsLoading;
	}

}
