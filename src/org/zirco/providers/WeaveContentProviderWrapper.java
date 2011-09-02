/*
 * Zirco Browser for Android
 * 
 * Copyright (C) 2010 - 2011 J. Devauchelle and contributors.
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

package org.zirco.providers;

import org.zirco.model.WeaveBookmarkItem;
import org.zirco.providers.WeaveColumn.WeaveColumns;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class WeaveContentProviderWrapper {	
	
	public static Cursor getWeaveBookmarksByParentId(ContentResolver contentResolser, String parentId) {
		String whereClause = WeaveColumns.WEAVE_BOOKMARKS_WEAVE_PARENT_ID + " = \"" + parentId + "\"";
		String orderClause = WeaveColumns.WEAVE_BOOKMARKS_FOLDER + " DESC, " + WeaveColumns.WEAVE_BOOKMARKS_TITLE + " COLLATE NOCASE";
		
		return contentResolser.query(WeaveColumns.CONTENT_URI, WeaveColumns.WEAVE_BOOKMARKS_PROJECTION, whereClause, null, orderClause);
	}
	
	public static WeaveBookmarkItem getWeaveBookmarkById(ContentResolver contentResolver, long id) {
		WeaveBookmarkItem result = null;
		
		Uri uri = ContentUris.withAppendedId(WeaveColumns.CONTENT_URI, id);
		Cursor c = contentResolver.query(uri, null, null, null, null);
		
		if (c != null) {
			if (c.moveToFirst()) {
				String title = c.getString(c.getColumnIndex(WeaveColumns.WEAVE_BOOKMARKS_TITLE));
				String url = c.getString(c.getColumnIndex(WeaveColumns.WEAVE_BOOKMARKS_URL));
				String weaveId = c.getString(c.getColumnIndex(WeaveColumns.WEAVE_BOOKMARKS_WEAVE_ID));
				boolean isFolder = c.getInt(c.getColumnIndex(WeaveColumns.WEAVE_BOOKMARKS_FOLDER)) > 0 ? true : false;
				
				result = new WeaveBookmarkItem(title, url, weaveId, isFolder);
			}
			
			c.close();
		}
		
		return result;
	}
	
	public static long getIdByWeaveId(ContentResolver contentResolver, String weaveId) {
		long result = -1;
		String whereClause = WeaveColumns.WEAVE_BOOKMARKS_WEAVE_ID + " = \"" + weaveId + "\"";
		
		Cursor c = contentResolver.query(WeaveColumns.CONTENT_URI, null, whereClause, null, null);
		if (c != null) {
			if (c.moveToFirst()) {
				result = c.getLong(c.getColumnIndex(WeaveColumns.WEAVE_BOOKMARKS_ID));
			}
			
			c.close();
		}
		
		return result;
	}
	
	public static void insert(ContentResolver contentResolver, ContentValues values) {
		contentResolver.insert(WeaveColumns.CONTENT_URI, values);
	}
	
	public static void update(ContentResolver contentResolver, long id, ContentValues values) {
		String whereClause = WeaveColumns.WEAVE_BOOKMARKS_ID + " = " + id;
		contentResolver.update(WeaveColumns.CONTENT_URI, values, whereClause, null);
		
	}
	
	public static void deleteByWeaveId(ContentResolver contentResolver, String weaveId) {
		String whereClause = WeaveColumns.WEAVE_BOOKMARKS_WEAVE_ID + " = \"" + weaveId + "\"";
		contentResolver.delete(WeaveColumns.CONTENT_URI, whereClause, null);
	}
	
	public static void clearWeaveBookmarks(ContentResolver contentResolver) {
		contentResolver.delete(WeaveColumns.CONTENT_URI, null, null);
	}

}
