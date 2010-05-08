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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Browser.BookmarkColumns;

public class BookmarksUtils {

	private static final Uri BOOKMARKS_URI = Uri.parse("content://browser/bookmarks");

	public static void saveBookmark(Context c, String title, String url) {
		Intent i = new Intent(Intent.ACTION_INSERT, BOOKMARKS_URI);
		i.putExtra("title", title);
		i.putExtra("url", url);
		c.startActivity(i);
	}
	
	public static void editBookmark(Context c, String title, String url) {
		Intent i = new Intent(Intent.ACTION_INSERT, BOOKMARKS_URI);
		i.putExtra("title", title);
		i.putExtra("url", url);
		c.startActivity(i);
	}

	public static final Cursor getAllBookmarks(Context context) throws IllegalStateException {
		return context.getContentResolver().query(BOOKMARKS_URI,
				new String[] { BookmarkColumns.TITLE, BookmarkColumns.URL }, 
				"bookmark = 1", null, null);
	}

}
