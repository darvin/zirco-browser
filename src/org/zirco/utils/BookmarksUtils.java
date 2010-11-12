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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.Browser;
import android.provider.Browser.BookmarkColumns;

/**
 * Utilities to manage bookmarks.
 */
public class BookmarksUtils {

	/*
	public static void saveBookmark(Context c, String title, String url) {
		Intent i = new Intent(Intent.ACTION_INSERT, BOOKMARKS_URI);
		i.putExtra("title", title);
		i.putExtra("url", url);
		c.startActivity(i);
	}
	*/
	
	/*
	public static void editBookmark(Context c, String title, String url) {
		Intent i = new Intent(Intent.ACTION_INSERT, BOOKMARKS_URI);
		i.putExtra("title", title);
		i.putExtra("url", url);
		c.startActivity(i);
	}
	*/

	/**
	 * Save an Android bookmark.
	 * @param context The current context.
	 * @param title The bookmark title.
	 * @param url The bookmark url.
	 */
	public static void saveAndroidBookmark(Context context, String title, String url) {
		ContentValues values = new ContentValues();
		values.put(Browser.BookmarkColumns.TITLE, title);
		values.put(Browser.BookmarkColumns.URL, url);
		values.put(Browser.BookmarkColumns.BOOKMARK, 1);
		
		context.getContentResolver().insert(Browser.BOOKMARKS_URI, values);
	}
	
	/**
	 * Gets the Android bookmarks.
	 * @param context The current context.
	 * @return A Cursor to Android bookmarks.
	 * @throws IllegalStateException Exception.
	 */
	public static final Cursor getAllAndroidBookmarks(Context context) throws IllegalStateException {
		return context.getContentResolver().query(Browser.BOOKMARKS_URI,
				new String[] {BookmarkColumns.TITLE, BookmarkColumns.URL}, 
				"bookmark = 1", null, null);
	}

}
