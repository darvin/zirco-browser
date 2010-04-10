package org.zirco.model.bookmarks;

import org.zirco.utils.DateUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BookmarksDbAdapter {
	
	private static final String TAG = "BookmarksDbAdapter";

	public static final String BOOKMARKS_ROWID = "_id";
	public static final String BOOKMARKS_TITLE = "title";
	public static final String BOOKMARKS_URL = "url";
	public static final String BOOKMARKS_CREATION_DATE = "creation_date";
	
	private static final String BOOKMARKS_DATABASE_NAME = "ZIRCO";
    private static final String BOOKMARKS_DATABASE_TABLE = "BOOKMARKS";
    private static final int BOOKMARKS_DATABASE_VERSION = 1;
    
    private static final String DATABASE_CREATE = "CREATE TABLE " + BOOKMARKS_DATABASE_TABLE + " (" + 
    	BOOKMARKS_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +   
    	BOOKMARKS_TITLE + " TEXT, " +
    	BOOKMARKS_URL + " TEXT NOT NULL, " +
    	BOOKMARKS_CREATION_DATE + " TEXT);";
	
	private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private final Context mCtx;
    
    public BookmarksDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    
    public BookmarksDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    public Cursor fetchBookmarks() {
    	return mDb.query(BOOKMARKS_DATABASE_TABLE,
    			new String[] { BOOKMARKS_ROWID, BOOKMARKS_TITLE, BOOKMARKS_URL }, null, null, null, null, null);
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
    
    public String[] getBookmarkById(long rowId) {
    	Cursor cursor = mDb.query(true, BOOKMARKS_DATABASE_TABLE, new String[] { BOOKMARKS_ROWID, BOOKMARKS_TITLE, BOOKMARKS_URL },
    			BOOKMARKS_ROWID + "=" + rowId, null, null, null, null, null);
    	
    	if ((cursor != null) &&
    			(cursor.moveToFirst())) {
    		
    		String result[] = new String[2];
    		result[0] = cursor.getString(cursor.getColumnIndex(BOOKMARKS_TITLE));
    		result[1] = cursor.getString(cursor.getColumnIndex(BOOKMARKS_URL));
    		
    		cursor.close();
    		
    		return result;
    		
    	} else {
    		return null;
    	}
    }
    
    /**
     * DatabaseHelper 
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, BOOKMARKS_DATABASE_NAME, null, BOOKMARKS_DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + BOOKMARKS_DATABASE_TABLE);
            onCreate(db);
		}
    	
    }
	
}
