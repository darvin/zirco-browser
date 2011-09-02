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

package org.zirco.model;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.zirco.providers.WeaveColumn.WeaveColumns;
import org.zirco.utils.Constants;
import org.zirco.utils.DateUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Implementation of the database adapter.
 */
public class DbAdapter {
	
	private static final String TAG = "DbAdapter";

	private static final String DATABASE_NAME = "ZIRCO";
	private static final int DATABASE_VERSION = 5;
	
	/**
	 * Bookmarks table.
	 */
	public static final String BOOKMARKS_ROWID = "_id";
	public static final String BOOKMARKS_TITLE = "title";
	public static final String BOOKMARKS_URL = "url";
	public static final String BOOKMARKS_CREATION_DATE = "creation_date";
	public static final String BOOKMARKS_COUNT = "count";
	public static final String BOOKMARKS_THUMBNAIL = "thumbnail";	
	
    private static final String BOOKMARKS_DATABASE_TABLE = "BOOKMARKS";    
    
    private static final String BOOKMARKS_DATABASE_CREATE = "CREATE TABLE " + BOOKMARKS_DATABASE_TABLE + " (" + 
    	BOOKMARKS_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    	BOOKMARKS_TITLE + " TEXT, " +
    	BOOKMARKS_URL + " TEXT NOT NULL, " +
    	BOOKMARKS_CREATION_DATE + " DATE, " +
    	BOOKMARKS_COUNT + " INTEGER NOT NULL DEFAULT 0, " +
    	BOOKMARKS_THUMBNAIL + " BLOB);";
    
    /**
     * History table.
     */
    public static final String HISTORY_ROWID = "_id";
	public static final String HISTORY_TITLE = "title";
	public static final String HISTORY_URL = "url";
	public static final String HISTORY_LAST_VISITED_DATE = "last_visited_date";
	
	private static final String HISTORY_DATABASE_TABLE = "HISTORY";
	
	private static final String HISTORY_DATABASE_CREATE = "CREATE TABLE " + HISTORY_DATABASE_TABLE + " (" +
		HISTORY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		HISTORY_TITLE + " TEXT, " +
		HISTORY_URL + " TEXT NOT NULL, " +
		HISTORY_LAST_VISITED_DATE + " DATE);";
	
	/**
	 * Adblock white list table.
	 */
	public static final String ADBLOCK_ROWID = "_id";
	public static final String ADBLOCK_URL = "url";
	
	private static final String ADBLOCK_WHITELIST_DATABASE_TABLE = "ADBLOCK_WHITELIST";
	
	private static final String ADBLOCK_WHITELIST_DATABASE_CREATE = "CREATE TABLE " + ADBLOCK_WHITELIST_DATABASE_TABLE + " (" +
		ADBLOCK_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		ADBLOCK_URL + " TEXT NOT NULL);";
	
	/**
	 * Mobile view url table.
	 */
	public static final String MOBILE_VIEW_URL_ROWID = "_id";
	public static final String MOBILE_VIEW_URL_URL = "url";
	
	private static final String MOBILE_VIEW_DATABASE_TABLE = "MOBILE_VIEW_URL";
	
	private static final String MOBILE_VIEW_DATABASE_CREATE = "CREATE TABLE " + MOBILE_VIEW_DATABASE_TABLE + " (" +
		MOBILE_VIEW_URL_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		MOBILE_VIEW_URL_URL + " TEXT NOT NULL);";
	
	protected boolean mAdBlockListNeedPopulate = false;
	
	private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private final Context mContext;
    
    /**
     * Constructor.
     * @param ctx The current context.
     */
    public DbAdapter(Context ctx) {
        this.mContext = ctx;
    }
    
    /**
     * Open the database helper.
     * @return The current database adapter.
     */
    public DbAdapter open() {
        mDbHelper = new DatabaseHelper(mContext, this);
        mDb = mDbHelper.getWritableDatabase();
        
        if (mAdBlockListNeedPopulate) {
        	populateDefaultWhiteList();
        	mAdBlockListNeedPopulate = false;
        }
        
        return this;
    }
    
    /**
     * Close the database helper.
     */
    public void close() {
        mDbHelper.close();
    }
    
    public SQLiteDatabase getDatabase() {
		return mDb;
	}
    
    /*******************************************************************************************************************************************************    
     * Bookmarks.
     */
    
    /**
     * Get the bookmarks.
     * @return A cursor to the bookmarks.
     */    
    public Cursor fetchBookmarks() {
    	String orderClause;
    	switch (PreferenceManager.getDefaultSharedPreferences(mContext).getInt(Constants.PREFERENCES_BOOKMARKS_SORT_MODE, 0)) {
    	case 0:
    		orderClause = BOOKMARKS_COUNT + " DESC, " + BOOKMARKS_TITLE + " COLLATE NOCASE";
    		break;
    	case 1:
    		orderClause = BOOKMARKS_TITLE + " COLLATE NOCASE";
    		break;
    	case 2:
    		orderClause = BOOKMARKS_CREATION_DATE + " DESC";
    		break;    	
    	default:
    		orderClause = BOOKMARKS_TITLE + " COLLATE NOCASE";
    		break;
    	}
    	
    	return mDb.query(BOOKMARKS_DATABASE_TABLE,
    			new String[] {BOOKMARKS_ROWID, BOOKMARKS_TITLE, BOOKMARKS_URL, BOOKMARKS_CREATION_DATE, BOOKMARKS_THUMBNAIL, BOOKMARKS_COUNT}, null, null, null, null, orderClause);
    }
    
    /**
     * Get a limited list of most used bookmarks.
     * @param limit The number of records.
     * @return A cursor with BOOKMARKS_TITLE and BOOKMARKS_URL columns.
     */
    public Cursor fetchBookmarksWithLimitForStartPage(int limit) {
    	return mDb.query(BOOKMARKS_DATABASE_TABLE,
    			new String[] {BOOKMARKS_TITLE, BOOKMARKS_URL}, null, null, null, null,
    			BOOKMARKS_COUNT + " DESC, " + BOOKMARKS_TITLE + " COLLATE NOCASE",
    			Integer.toString(limit));
    }
    
    /**
     * Get a cursor on a specific bookmark, given its url.
     * @param originalUrl The original url to search for.
     * @param loadedUrl The loaded url to search for.
     * @return The query result.
     */
    public Cursor getBookmarkFromUrl(String originalUrl, String loadedUrl) {
    	String whereClause = "(" + BOOKMARKS_URL + " = \"" + originalUrl + "\") OR ("
    		+ BOOKMARKS_URL + " = \"" + loadedUrl + "\")";
    	
    	return mDb.query(BOOKMARKS_DATABASE_TABLE,
    			new String[] {BOOKMARKS_ROWID, BOOKMARKS_TITLE, BOOKMARKS_URL, BOOKMARKS_CREATION_DATE},
    			whereClause,
    			null, null, null, null);
    }
    
    /**
     * Add a bookmark to database.
     * @param title The title.
     * @param url The url.
     * @return The new bookmark id.
     */
    public long addBookmark(String title, String url) {
        return addBookmark(title, url, DateUtils.getNow(mContext), 0);
    }
    
    /**
     * Add a bookmark to database.
     * @param title The title.
     * @param url The url.
     * @param creationDate The creation date.
     * @param count The usage count.
     * @return The new bookmark id.
     */
    public long addBookmark(String title, String url, String creationDate, int count) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(BOOKMARKS_TITLE, title);
        initialValues.put(BOOKMARKS_URL, url);
        initialValues.put(BOOKMARKS_CREATION_DATE, creationDate);
        initialValues.put(BOOKMARKS_COUNT, count);
        
        return mDb.insert(BOOKMARKS_DATABASE_TABLE, null, initialValues);
    }
    
    /**
     * Update a bookmark to database.
     * @param rowId The bookmark id.
     * @param title The new title.
     * @param url The new url.
     * @return True if the update succeeded.
     */
    public boolean updateBookmark(long rowId, String title, String url) {
        ContentValues args = new ContentValues();
        args.put(BOOKMARKS_TITLE, title);
        args.put(BOOKMARKS_URL, url);

        return mDb.update(BOOKMARKS_DATABASE_TABLE, args, BOOKMARKS_ROWID + "=" + rowId, null) > 0;
    }
    
    /**
     * Update the hit count for the given bookmark.
     * @param rowId The bookmark id.
     */
    public void updateBookmarkCount(long rowId) {
    	String query = String.format("UPDATE %s SET %s = %s + 1 WHERE %s = %s;",
    			BOOKMARKS_DATABASE_TABLE,
    			BOOKMARKS_COUNT,
    			BOOKMARKS_COUNT,
    			BOOKMARKS_ROWID,
    			rowId);
    	
    	mDb.execSQL(query);
    }
    
    /**
     * Update the thumbnail for the given bookmark.
     * @param rowId The bookmark id.
     * @param bm The thumbnail.
     * @return True if the update succeeded.
     */
    public boolean updateBookmarkThumbnail(long rowId, Bitmap bm) {
    	
    	ByteArrayOutputStream os = new ByteArrayOutputStream();    	
    	bm.compress(Bitmap.CompressFormat.PNG, 100, os);
    	
    	ContentValues args = new ContentValues();
    	args.put(BOOKMARKS_THUMBNAIL, os.toByteArray());
    	
    	return mDb.update(BOOKMARKS_DATABASE_TABLE, args, BOOKMARKS_ROWID + "=" + rowId, null) > 0;
    }
    
    /**
     * Delete a bookmark.
     * @param rowId The bookmark id.
     * @return True if the deletion succeeded.
     */
    public boolean deleteBookmark(long rowId) { 
        return mDb.delete(BOOKMARKS_DATABASE_TABLE, BOOKMARKS_ROWID + "=" + rowId, null) > 0;
    }
    
    /**
     * Delete all bookmarks.
     */
    public void clearBookmarks() {
    	mDb.execSQL("DELETE FROM " + BOOKMARKS_DATABASE_TABLE + ";");
    }
    
    /**
     * Get a bookmark by its id.
     * @param rowId The bookmark id.
     * @return An array of string, with bookmark title at index 0 and bookmark url at index 1.
     */
    public String[] getBookmarkById(long rowId) {
    	Cursor cursor = mDb.query(true, BOOKMARKS_DATABASE_TABLE, new String[] {BOOKMARKS_ROWID, BOOKMARKS_TITLE, BOOKMARKS_URL},
    			BOOKMARKS_ROWID + "=" + rowId, null, null, null, null, null);
    	
    	if (cursor.moveToFirst()) {
    		
    		String[] result = new String[2];
    		result[0] = cursor.getString(cursor.getColumnIndex(BOOKMARKS_TITLE));
    		result[1] = cursor.getString(cursor.getColumnIndex(BOOKMARKS_URL));
    		
    		cursor.close();
    		
    		return result;
    		
    	} else {
    		cursor.close();
    		return null;
    	}
    }
    
    /*******************************************************************************************************************************************************    
     * History.
     */
    
    /**
     * Get a cursor to suggestions from history..
     * @param pattern The pattern to match.
     * @return A Cursor to suggestions.
     */    
    public Cursor getSuggestionsFromHistory(String pattern) {
    	Cursor cursor;
    	
    	if ((pattern != null) &&
    			(pattern.length() > 0)) {	
    		pattern = "%" + pattern + "%";    	
    		cursor = mDb.query(HISTORY_DATABASE_TABLE, new String[] {HISTORY_ROWID, HISTORY_URL}, HISTORY_URL + " LIKE '" + pattern + "'", null, null, null, null);
    	} else {
    		cursor = mDb.query(HISTORY_DATABASE_TABLE, new String[] {HISTORY_ROWID, HISTORY_URL}, null, null, null, null, null);
    	}
    	
    	return cursor;
    }
    
    /**
     * Get a cursor for suggections, given a search pattern.
     * Search on history and bookmarks, on title and url.
     * The result list is sorted based on each result note.
     * @see UrlSuggestionItem for how a note is computed.
     * @param pattern The pattern to search for.
     * @return A cursor of suggections.
     */
    public Cursor getUrlSuggestions(String pattern) {
    	MatrixCursor cursor = new MatrixCursor(new String[] {UrlSuggestionCursorAdapter.URL_SUGGESTION_ID,
    			UrlSuggestionCursorAdapter.URL_SUGGESTION_TITLE,
    			UrlSuggestionCursorAdapter.URL_SUGGESTION_URL,
    			UrlSuggestionCursorAdapter.URL_SUGGESTION_TYPE});
    	
    	if ((pattern != null) &&
    			(pattern.length() > 0)) {
    		
    		String sqlPattern = "%" + pattern + "%";
    		
    		List<UrlSuggestionItem> results = new ArrayList<UrlSuggestionItem>();
    		
    		// Get history results.
    		Cursor historyCursor = mDb.query(HISTORY_DATABASE_TABLE,
    				new String[] {HISTORY_ROWID, HISTORY_TITLE, HISTORY_URL},
    				HISTORY_TITLE + " LIKE '" + sqlPattern + "' OR " + HISTORY_URL  + " LIKE '" + sqlPattern + "'",
    				null, null, null, null);
    		
    		if (historyCursor != null) {
    			
    			if (historyCursor.moveToFirst()) {
    				
    				int historyTitleId = historyCursor.getColumnIndex(HISTORY_TITLE);
    				int historyUrlId = historyCursor.getColumnIndex(HISTORY_URL);
    				
    				do {    					
    					results.add(new UrlSuggestionItem(pattern,
    							historyCursor.getString(historyTitleId),
    							historyCursor.getString(historyUrlId),
    							1));
    					
    				} while (historyCursor.moveToNext());
    			}
    			
    			historyCursor.close();
    		}
    		
    		// Get bookmarks results.
    		Cursor bookmarksCursor = mDb.query(BOOKMARKS_DATABASE_TABLE,
    				new String[] {BOOKMARKS_ROWID, BOOKMARKS_TITLE, BOOKMARKS_URL},
    				BOOKMARKS_TITLE + " LIKE '" + sqlPattern + "' OR " + BOOKMARKS_URL  + " LIKE '" + sqlPattern + "'",
    				null, null, null, null);
    		
    		if (bookmarksCursor != null) {
    			
    			if (bookmarksCursor.moveToFirst()) {
    				
    				int bookmarksTitleId = bookmarksCursor.getColumnIndex(BOOKMARKS_TITLE);
    				int boomarksUrlId = bookmarksCursor.getColumnIndex(BOOKMARKS_URL);
    				
    				do {
    					results.add(new UrlSuggestionItem(pattern,
    							bookmarksCursor.getString(bookmarksTitleId),
    							bookmarksCursor.getString(boomarksUrlId),
    							2));
    					
    				} while (bookmarksCursor.moveToNext());
    			}
    			
    			bookmarksCursor.close();
    		}
    		
    		if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.PREFERENCE_USE_WEAVE, false)) {    			
    			Cursor weaveCursor = mContext.getContentResolver().query(WeaveColumns.CONTENT_URI,
    					null,
    					WeaveColumns.WEAVE_BOOKMARKS_FOLDER + " = 0 AND (" +  WeaveColumns.WEAVE_BOOKMARKS_TITLE + " LIKE '" + sqlPattern + "' OR " + WeaveColumns.WEAVE_BOOKMARKS_URL  + " LIKE '" + sqlPattern + "')",
    					null, null);

    			if (weaveCursor != null) {
    				if (weaveCursor.moveToFirst()) {
    					
    					int weaveTitleId = weaveCursor.getColumnIndex(WeaveColumns.WEAVE_BOOKMARKS_TITLE);
        				int weaveUrlId = weaveCursor.getColumnIndex(WeaveColumns.WEAVE_BOOKMARKS_URL);
    					
    					do {
    						results.add(new UrlSuggestionItem(pattern,
    								weaveCursor.getString(weaveTitleId),
    								weaveCursor.getString(weaveUrlId),
    								3));
    					} while (weaveCursor.moveToNext());
    				}

    				weaveCursor.close();
    			}
    		}
    		
    		// Sort results.
    		Collections.sort(results, new UrlSuggestionItemComparator());
    		
    		//Log.d("Results", Integer.toString(results.size()));
    		
    		// Copy results to the output MatrixCursor.
    		int idCounter = -1;
    		for (UrlSuggestionItem item : results) {
    			idCounter++;
				
				String[] row = new String[4];
				row[0] = Integer.toString(idCounter);
				row[1] = item.getTitle();
				row[2] = item.getUrl();
				row[3] = Integer.toString(item.getType());
				
				cursor.addRow(row);
    		}
    		
    	}
    	
    	return cursor;
    }
    
    /**
     * Get a limited list of last visited websites.
     * @param limit The number of records.
     * @return A cursor with HISTORY_TITLE and HISTORY_URL columns.
     */
    public Cursor fetchHistoryWithLimitForStartPage(int limit) {
    	return mDb.query(HISTORY_DATABASE_TABLE,
    			new String[] {HISTORY_TITLE, HISTORY_URL}, null, null, null, null,
    			HISTORY_LAST_VISITED_DATE + " DESC, " + HISTORY_TITLE + " COLLATE NOCASE",
    			Integer.toString(limit));
    }
    
    /**
     * Get the history.
     * @return A list of lists of HistoryItem. Each top-level list represents a day of history.
     */
    public Cursor fetchHistory() {
    	
    	int historyLimit;
    	try {
    		historyLimit = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.PREFERENCES_BROWSER_HISTORY_SIZE, "90"));
		} catch (Exception e) {
			historyLimit = 90;
		}
		
		Date limit = DateUtils.getDateAtMidnight(-historyLimit);
		
		String whereClause = "(" + HISTORY_LAST_VISITED_DATE + " <= \"" + DateUtils.getDateAsUniversalString(mContext, new Date()) + "\") AND " +
			"(" + HISTORY_LAST_VISITED_DATE + " > \"" + DateUtils.getDateAsUniversalString(mContext, limit) + "\")";
    	
		return mDb.query(HISTORY_DATABASE_TABLE,
				new String[] {HISTORY_ROWID, HISTORY_TITLE, HISTORY_URL, HISTORY_LAST_VISITED_DATE},
				whereClause,
				null,
				null,
				null,
				HISTORY_LAST_VISITED_DATE + " DESC");
    }
    
    /**
     * Get the id for an history record identified by its url.
     * @param url The url to look for.
     * @return The history record index, or -1 if not found.
     */
    private long getHistoryItemIdByUrl(String url) {
    	long result = -1;
    	
    	Cursor cursor = mDb.query(HISTORY_DATABASE_TABLE, new String[] {HISTORY_ROWID}, HISTORY_URL + " = \"" + url + "\"", null, null, null, null);
    	
    	if (cursor.moveToFirst()) {
    		
    		result = cursor.getLong(cursor.getColumnIndex(HISTORY_ROWID));    		    		    		
    	}
    	
    	cursor.close();
    	
    	return result;
    }
    
    /**
     * Update the history. If the url is already present, the visited date is updated.
     * If not, a new record is created.
     * @param title The page title.
     * @param url The page url.
     */
    public void updateHistory(String title, String url) {
    	
    	long existingId = getHistoryItemIdByUrl(url);
    	
    	if (existingId != -1) {
    		ContentValues args = new ContentValues();
            args.put(HISTORY_TITLE, title);            
            args.put(HISTORY_LAST_VISITED_DATE, DateUtils.getNow(mContext));

            mDb.update(HISTORY_DATABASE_TABLE, args, HISTORY_ROWID + "=" + existingId, null);
    	} else {
    		ContentValues initialValues = new ContentValues();
        	initialValues.put(HISTORY_TITLE, title);
            initialValues.put(HISTORY_URL, url);
            initialValues.put(HISTORY_LAST_VISITED_DATE, DateUtils.getNow(mContext));
            
            mDb.insert(HISTORY_DATABASE_TABLE, null, initialValues);
    	}
    }
    
    /**
     * Delete an history record given its id.
     * @param id The id to delete.
     */
    public void deleteFromHistory(long id) {
    	mDb.execSQL("DELETE FROM " + HISTORY_DATABASE_TABLE + " WHERE " + HISTORY_ROWID + " = " + id + ";");
    }
    
    /**
     * Delete all records from history.
     */
    public void clearHistory() {
    	mDb.execSQL("DELETE FROM " + HISTORY_DATABASE_TABLE + ";");
    }
    
    /**
     * Delete all records from history witch have a visited date prior to the history max age.
     */
    public void truncateHistory() {
    	mDb.execSQL("DELETE FROM " + HISTORY_DATABASE_TABLE + " WHERE " + HISTORY_LAST_VISITED_DATE + " < \"" + DateUtils.getHistoryLimit(mContext) + "\";");
    }
    
    /*******************************************************************************************************************************************************    
     * Adblock white list.
     */
    
    /**
     * Get the white list url given its id.
     * @param rowId The id.
     * @return The white list url. 
     */
    public String getWhiteListItemById(long rowId) {
    	Cursor cursor = mDb.query(true, ADBLOCK_WHITELIST_DATABASE_TABLE, new String[] {ADBLOCK_ROWID, ADBLOCK_URL},
    			ADBLOCK_ROWID + "=" + rowId, null, null, null, null, null);
    	
    	if (cursor.moveToFirst()) {
    		
    		String result;
    		result = cursor.getString(cursor.getColumnIndex(ADBLOCK_URL));
    		
    		cursor.close();
    		
    		return result;
    		
    	} else {
    		cursor.close();
    		return null;
    	}
    }
    
    /**
     * Get the list of url presents in white list.
     * @return The list of url presents in white list.
     */
    public List<String> getWhiteList() {
    	List<String> result = new ArrayList<String>();
    	
    	Cursor cursor = getWhiteListCursor();
    	
    	if (cursor.moveToFirst()) {
    		do {
    			
    			result.add(cursor.getString(cursor.getColumnIndex(ADBLOCK_URL)));
    			
    		} while (cursor.moveToNext());
    	}
    	
    	cursor.close();
    	
    	return result;
    }
    
    /**
     * Get a cursor to the list of url presents in white list.
     * @return A cursor to the list of url presents in white list.
     */
    public Cursor getWhiteListCursor() {    	
    	return mDb.query(ADBLOCK_WHITELIST_DATABASE_TABLE, new String[] {ADBLOCK_ROWID, ADBLOCK_URL}, null, null, null, null, null);
    }
    
    /**
     * Insert an item in the white list.
     * @param url The url to insert.
     */
    public void insertInWhiteList(String url) {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(ADBLOCK_URL, url);
        
        mDb.insert(ADBLOCK_WHITELIST_DATABASE_TABLE, null, initialValues);
    }
    
    /**
     * Delete an item in white list given its id.
     * @param id The id to delete.
     */
    public void deleteFromWhiteList(long id) {
    	mDb.execSQL("DELETE FROM " + ADBLOCK_WHITELIST_DATABASE_TABLE + " WHERE " + ADBLOCK_ROWID + " = " + id + ";");
    }
    
    /**
     * Delete all records from the white list.
     */
    public void clearWhiteList() {
    	mDb.execSQL("DELETE FROM " + ADBLOCK_WHITELIST_DATABASE_TABLE + ";");
    }
    
    /*******************************************************************************************************************************************************    
     * Mobile view list.
     */
    
    /**
     * Get an url from the mobile view list from its id.
     * @param rowId The id.
     * @return The url.
     */
    public String getMobileViewUrlItemById(long rowId) {
    	Cursor cursor = mDb.query(true, MOBILE_VIEW_DATABASE_TABLE, new String[] {MOBILE_VIEW_URL_ROWID, MOBILE_VIEW_URL_URL},
    			MOBILE_VIEW_URL_ROWID + "=" + rowId, null, null, null, null, null);
    	
    	if (cursor.moveToFirst()) {
    		
    		String result;
    		result = cursor.getString(cursor.getColumnIndex(MOBILE_VIEW_URL_URL));
    		
    		cursor.close();
    		
    		return result;
    		
    	} else {
    		cursor.close();
    		return null;
    	}
    }
    
    /**
     * Get a list of all urls in mobile view list.
     * @return A list of url.
     */
    public List<String> getMobileViewUrlList() {
    	List<String> result = new ArrayList<String>();
    	
    	Cursor cursor = getMobileViewUrlCursor();
    	
    	if (cursor.moveToFirst()) {
    		do {
    			
    			result.add(cursor.getString(cursor.getColumnIndex(MOBILE_VIEW_URL_URL)));
    			
    		} while (cursor.moveToNext());
    	}
    	
    	cursor.close();
    	
    	return result;
    }
    
    /**
     * Get a Cursor to the mobile view url list.
     * @return A Cursor to the mobile view url list.
     */
    public Cursor getMobileViewUrlCursor() {
    	return mDb.query(MOBILE_VIEW_DATABASE_TABLE, new String[] {MOBILE_VIEW_URL_ROWID, MOBILE_VIEW_URL_URL}, null, null, null, null, null);
    }
    
    /**
     * Insert an url in the mobile view url list.
     * @param url The new url.
     */
    public void insertInMobileViewUrlList(String url) {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(MOBILE_VIEW_URL_URL, url);
        
        mDb.insert(MOBILE_VIEW_DATABASE_TABLE, null, initialValues);
    }
    
    /**
     * Delete an url from the mobile view url list.
     * @param id The id of the url to delete.
     */
    public void deleteFromMobileViewUrlList(long id) {
    	mDb.execSQL("DELETE FROM " + MOBILE_VIEW_DATABASE_TABLE + " WHERE " + MOBILE_VIEW_URL_ROWID + " = " + id + ";");
    }
    
    /**
     * Clear the mobile view url list.
     */
    public void clearMobileViewUrlList() {
    	mDb.execSQL("DELETE FROM " + MOBILE_VIEW_DATABASE_TABLE + ";");
    }
    
    /**
     * Populate the white list with default values.
     */
    private void populateDefaultWhiteList() {
    	insertInWhiteList("google.com/reader");    	
    }
    
    /**
     * DatabaseHelper.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

    	private DbAdapter mParent;
    	
    	/**
    	 * Constructor.
    	 * @param context The current context.
    	 * @param parent The DbAdapter parent.
    	 */
		public DatabaseHelper(Context context, DbAdapter parent) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mParent = parent;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(BOOKMARKS_DATABASE_CREATE);
			db.execSQL(HISTORY_DATABASE_CREATE);
			db.execSQL(ADBLOCK_WHITELIST_DATABASE_CREATE);
			db.execSQL(MOBILE_VIEW_DATABASE_CREATE);
			mParent.mAdBlockListNeedPopulate = true;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {			
			
			Log.d(TAG, "Upgrading database.");
			
			switch (oldVersion) {
			case 1: db.execSQL("ALTER TABLE " + BOOKMARKS_DATABASE_TABLE + " ADD " + BOOKMARKS_THUMBNAIL + " BLOB;");
			case 2: db.execSQL("ALTER TABLE " + BOOKMARKS_DATABASE_TABLE + " ADD " + BOOKMARKS_COUNT + " INTEGER NOT NULL DEFAULT 0;");
			case 3:
				db.execSQL(ADBLOCK_WHITELIST_DATABASE_CREATE);
				mParent.mAdBlockListNeedPopulate = true;
			case 4: db.execSQL(MOBILE_VIEW_DATABASE_CREATE);
			default: break;
			}
		}
    	
    }
	
}
