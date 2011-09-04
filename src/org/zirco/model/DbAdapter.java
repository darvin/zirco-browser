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

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Implementation of the database adapter.
 */
public class DbAdapter {
	
	private static final String TAG = "DbAdapter";

	private static final String DATABASE_NAME = "ZIRCO";
	private static final int DATABASE_VERSION = 6;
	
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
			//db.execSQL(BOOKMARKS_DATABASE_CREATE);
			//db.execSQL(HISTORY_DATABASE_CREATE);
			db.execSQL(ADBLOCK_WHITELIST_DATABASE_CREATE);
			db.execSQL(MOBILE_VIEW_DATABASE_CREATE);
			mParent.mAdBlockListNeedPopulate = true;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {			
			
			Log.d(TAG, "Upgrading database.");
			
			switch (oldVersion) {
			case 1: //db.execSQL("ALTER TABLE " + BOOKMARKS_DATABASE_TABLE + " ADD " + BOOKMARKS_THUMBNAIL + " BLOB;");
			case 2: //db.execSQL("ALTER TABLE " + BOOKMARKS_DATABASE_TABLE + " ADD " + BOOKMARKS_COUNT + " INTEGER NOT NULL DEFAULT 0;");
			case 3:
				db.execSQL(ADBLOCK_WHITELIST_DATABASE_CREATE);
				mParent.mAdBlockListNeedPopulate = true;
			case 4: db.execSQL(MOBILE_VIEW_DATABASE_CREATE);
			case 5:
				db.execSQL("DROP TABLE IF EXISTS BOOKMARKS;");
				db.execSQL("DROP TABLE IF EXISTS HISTORY;");
			default: break;
			}
		}
    	
    }
	
}
