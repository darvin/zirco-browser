package org.zirco.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.zirco.utils.Constants;
import org.zirco.utils.DateUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

public class DbAdapter {
	
	private static final String TAG = "DbAdapter";

	private static final String DATABASE_NAME = "ZIRCO";
	private static final int DATABASE_VERSION = 1;
	
	public static final String BOOKMARKS_ROWID = "_id";
	public static final String BOOKMARKS_TITLE = "title";
	public static final String BOOKMARKS_URL = "url";
	public static final String BOOKMARKS_CREATION_DATE = "creation_date";	
	
    private static final String BOOKMARKS_DATABASE_TABLE = "BOOKMARKS";    
    
    private static final String BOOKMARKS_DATABASE_CREATE = "CREATE TABLE " + BOOKMARKS_DATABASE_TABLE + " (" + 
    	BOOKMARKS_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +   
    	BOOKMARKS_TITLE + " TEXT, " +
    	BOOKMARKS_URL + " TEXT NOT NULL, " +
    	BOOKMARKS_CREATION_DATE + " DATE);";
    
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
	
	private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private final Context mCtx;
    
    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    
    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    /**
     * Bookmarks
     */    
    public Cursor fetchBookmarks() {
    	String orderClause;
    	switch (PreferenceManager.getDefaultSharedPreferences(mCtx).getInt(Constants.PREFERENCES_BOOKMARKS_SORT_MODE, 0)) {
    	case 0:
    		orderClause = BOOKMARKS_TITLE + " COLLATE NOCASE";
    		break;
    	case 1:
    		orderClause = BOOKMARKS_CREATION_DATE + " DESC";
    		break;
    	default:
    		orderClause = BOOKMARKS_TITLE + " COLLATE NOCASE";
    		break;
    	}
    	
    	return mDb.query(BOOKMARKS_DATABASE_TABLE,
    			new String[] { BOOKMARKS_ROWID, BOOKMARKS_TITLE, BOOKMARKS_URL }, null, null, null, null, orderClause);
    }
    
    public long addBookmark(String title, String url) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(BOOKMARKS_TITLE, title);
        initialValues.put(BOOKMARKS_URL, url);
        initialValues.put(BOOKMARKS_CREATION_DATE, DateUtils.getNow(mCtx));
        
        return mDb.insert(BOOKMARKS_DATABASE_TABLE, null, initialValues);
    }
    
    public boolean updateBookmark(long rowId, String title, String url) {
        ContentValues args = new ContentValues();
        args.put(BOOKMARKS_TITLE, title);
        args.put(BOOKMARKS_URL, url);

        return mDb.update(BOOKMARKS_DATABASE_TABLE, args, BOOKMARKS_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean deleteBookmark(long rowId) { 
        return mDb.delete(BOOKMARKS_DATABASE_TABLE, BOOKMARKS_ROWID + "=" + rowId, null) > 0;
    }
    
    public void clearBookmarks() {
    	mDb.execSQL("DELETE FROM " + BOOKMARKS_DATABASE_TABLE + ";");
    }
    
    public String[] getBookmarkById(long rowId) {
    	Cursor cursor = mDb.query(true, BOOKMARKS_DATABASE_TABLE, new String[] { BOOKMARKS_ROWID, BOOKMARKS_TITLE, BOOKMARKS_URL },
    			BOOKMARKS_ROWID + "=" + rowId, null, null, null, null, null);
    	
    	if (cursor.moveToFirst()) {
    		
    		String result[] = new String[2];
    		result[0] = cursor.getString(cursor.getColumnIndex(BOOKMARKS_TITLE));
    		result[1] = cursor.getString(cursor.getColumnIndex(BOOKMARKS_URL));
    		
    		cursor.close();
    		
    		return result;
    		
    	} else {
    		cursor.close();
    		return null;
    	}
    }
    
    /**
     * History
     */
    
    public List<List<HistoryItem>> fetchHistory() {
    	List<List<HistoryItem>> result = new ArrayList<List<HistoryItem>>();
    	
    	Date lastDate = new Date();
    	Date currentDate;
    	List<HistoryItem> items;
    	Cursor cursor;
    	
    	long id;
    	String title;
    	String url;
    	
    	for (int i = 0; i < 5; i++) {
    		currentDate = DateUtils.getDateAtMidnight(-i);
    		
    		cursor = mDb.query(HISTORY_DATABASE_TABLE,
        			new String[] { HISTORY_ROWID, HISTORY_TITLE, HISTORY_URL, HISTORY_LAST_VISITED_DATE },
        			"(" + HISTORY_LAST_VISITED_DATE + " <= \"" + DateUtils.getDateAsUniversalString(mCtx, lastDate) + "\") AND " + 
        			"(" + HISTORY_LAST_VISITED_DATE + " > \"" + DateUtils.getDateAsUniversalString(mCtx, currentDate) + "\")",
        			null, null, null, HISTORY_LAST_VISITED_DATE + " DESC");
    		
    		if (cursor.moveToFirst()) {
    			
    			items = new ArrayList<HistoryItem>();
    			
    			do {
    				
    				id = cursor.getLong(cursor.getColumnIndex(HISTORY_ROWID));
    				title = cursor.getString(cursor.getColumnIndex(HISTORY_TITLE));
    				url = cursor.getString(cursor.getColumnIndex(HISTORY_URL));
    				
    				items.add(new HistoryItem(id, title, url));
    				
    			} while (cursor.moveToNext());
    			    			    		
    			result.add(items);
    		}
    		
    		cursor.close();
    		
    		lastDate = currentDate;
    	}
    	
    	return result;
    }
    
    private long getIdByUrl(String url) {
    	long result = -1;
    	
    	Cursor cursor = mDb.query(HISTORY_DATABASE_TABLE, new String[] { HISTORY_ROWID }, HISTORY_URL + " = \"" + url + "\"", null, null, null, null);
    	
    	if (cursor.moveToFirst()) {
    		
    		result = cursor.getLong(cursor.getColumnIndex(HISTORY_ROWID));    		    		    		
    	}
    	
    	cursor.close();
    	
    	return result;
    }
    
    public void updateHistory(String title, String url) {
    	
    	long existingId = getIdByUrl(url);
    	
    	if (existingId != -1) {
    		ContentValues args = new ContentValues();
            args.put(HISTORY_TITLE, title);            
            args.put(HISTORY_LAST_VISITED_DATE, DateUtils.getNow(mCtx));

            mDb.update(HISTORY_DATABASE_TABLE, args, HISTORY_ROWID + "=" + existingId, null);
    	} else {
    		ContentValues initialValues = new ContentValues();
        	initialValues.put(HISTORY_TITLE, title);
            initialValues.put(HISTORY_URL, url);
            initialValues.put(HISTORY_LAST_VISITED_DATE, DateUtils.getNow(mCtx));
            
            mDb.insert(HISTORY_DATABASE_TABLE, null, initialValues);
    	}
    }
    
    public void clearHistory() {
    	//mDb.execSQL("DELETE FROM " + HISTORY_DATABASE_TABLE + " WHERE " + HISTORY_LAST_VISITED_DATE + " < " + DateUtils.getHistoryLimit(mCtx) + ";");
    	mDb.execSQL("DELETE FROM " + HISTORY_DATABASE_TABLE + ";");
    }
    
    /**
     * DatabaseHelper 
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(BOOKMARKS_DATABASE_CREATE);
			db.execSQL(HISTORY_DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
			
            db.execSQL("DROP TABLE IF EXISTS " + BOOKMARKS_DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + HISTORY_DATABASE_TABLE);
            
            onCreate(db);
		}
    	
    }
	
}
