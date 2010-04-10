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

	public static final Cursor getAllBookmarks(Context context) throws IllegalStateException {
		return context.getContentResolver().query(BOOKMARKS_URI,
				new String[] { BookmarkColumns.TITLE, BookmarkColumns.URL }, 
				"bookmark = 1", null, null);
	}

}
