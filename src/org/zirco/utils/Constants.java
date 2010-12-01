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

package org.zirco.utils;

import org.zirco.R;

import android.content.Context;

/**
 * Defines constants.
 */
public class Constants {
	
	public static final String EXTRA_ID_NEW_TAB = "EXTRA_ID_NEW_TAB";
	public static final String EXTRA_ID_URL = "EXTRA_ID_URL";
	
	public static final String EXTRA_ID_BOOKMARK_ID = "EXTRA_ID_BOOKMARK_ID";
	public static final String EXTRA_ID_BOOKMARK_URL = "EXTRA_ID_BOOKMARK_URL";
	public static final String EXTRA_ID_BOOKMARK_TITLE = "EXTRA_ID_BOOKMARK_TITLE";
	
	public static final int BOOKMARK_THUMBNAIL_WIDTH_FACTOR = 70;
	public static final int BOOKMARK_THUMBNAIL_HEIGHT_FACTOR = 60;
	
	/**
	 * Specials urls.
	 */
	public static final String URL_ABOUT_BLANK = "about:blank";
	public static final String URL_ABOUT_START = "about:start";
	public static final String URL_ACTION_SEARCH = "action:search?q=";
	
	/**
	 * Search urls.
	 */	
	public static String URL_SEARCH_GOOGLE = "http://www.google.com/search?ie=UTF-8&sourceid=navclient&gfns=1&q=%s";
	public static String URL_SEARCH_WIKIPEDIA = "http://en.wikipedia.org/w/index.php?search=%s&go=Go";
	
	/**
	 * User agents.
	 */
	public static String USER_AGENT_DEFAULT = "";
	public static String USER_AGENT_DESKTOP = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.7 (KHTML, like Gecko) Chrome/7.0.517.44 Safari/534.7";
	
	/**
	 * Preferences.
	 */
	public static final String PREFERENCES_GENERAL_HOME_PAGE = "GeneralHomePage";
	public static final String PREFERENCES_GENERAL_SEARCH_URL = "GeneralSearchUrl";
	public static final String PREFERENCES_GENERAL_BARS_DURATION = "GeneralBarsDuration";
	public static final String PREFERENCES_GENERAL_BUBBLE_POSITION = "GeneralBubblePosition";
	public static final String PREFERENCES_SHOW_FULL_SCREEN = "GeneralFullScreen";
	public static final String PREFERENCES_SHOW_TOAST_ON_TAB_SWITCH = "GeneralShowToastOnTabSwitch";
	
	public static final String PREFERENCES_UI_SHOW_HOME_BUTTON = "UIShowHomeButton";
	
	public static final String PREFERENCES_DEFAULT_ZOOM_LEVEL = "DefaultZoomLevel";
	
	public static final String PREFERENCES_BROWSER_HISTORY_SIZE = "BrowserHistorySize";
	public static final String PREFERENCES_BROWSER_ENABLE_JAVASCRIPT = "BrowserEnableJavascript";
	public static final String PREFERENCES_BROWSER_ENABLE_IMAGES = "BrowserEnableImages";
	public static final String PREFERENCES_BROWSER_ENABLE_FORM_DATA = "BrowserEnableFormData";
	public static final String PREFERENCES_BROWSER_ENABLE_PASSWORDS = "BrowserEnablePasswords";
	public static final String PREFERENCES_BROWSER_ENABLE_COOKIES = "BrowserEnableCookies";
	public static final String PREFERENCES_BROWSER_USER_AGENT = "BrowserUserAgent";
	
	public static final String PREFERENCES_PRIVACY_CLEAR_CACHE_ON_EXIT = "PrivacyClearCacheOnExit";
	
	public static final String PREFERENCES_ADBLOCKER_ENABLE = "AdBlockerEnable";
	
	public static final String PREFERENCES_BOOKMARKS_SORT_MODE = "BookmarksSortMode";
	
	public static final String PREFERENCES_LAST_VERSION_CODE = "LastVersionCode";
	
	public static final String PREFERENCES_START_PAGE_SHOW_SEARCH = "StartPageEnableSearch";
	public static final String PREFERENCES_START_PAGE_SHOW_BOOKMARKS = "StartPageEnableBookmarks";
	public static final String PREFERENCES_START_PAGE_SHOW_HISTORY = "StartPageEnableHistory";
	public static final String PREFERENCES_START_PAGE_BOOKMARKS_LIMIT = "StartPageBookmarksLimit";
	public static final String PREFERENCES_START_PAGE_HISTORY_LIMIT = "StartPageHistoryLimit";	
	
	/**
	 * Methods.
	 */
	
	/**
	 * Initialize the search url "constants", which depends on the user local.
	 * @param context The current context.
	 */
	public static void initializeConstantsFromResources(Context context) {
		URL_SEARCH_GOOGLE = context.getResources().getString(R.string.Constants_SearchUrlGoogle);
		URL_SEARCH_WIKIPEDIA = context.getResources().getString(R.string.Constants_SearchUrlWikipedia);
	}

}
