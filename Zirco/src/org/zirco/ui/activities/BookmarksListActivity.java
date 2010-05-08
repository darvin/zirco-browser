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

package org.zirco.ui.activities;

import org.zirco.R;
import org.zirco.model.BookmarksCursorAdapter;
import org.zirco.model.DbAdapter;
import org.zirco.utils.ApplicationUtils;
import org.zirco.utils.BookmarksUtils;
import org.zirco.utils.Constants;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Browser.BookmarkColumns;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class BookmarksListActivity extends ListActivity {
			
	private static final int MENU_SORT_MODE = Menu.FIRST;
	private static final int MENU_ADD_BOOKMARK = Menu.FIRST + 1;
	private static final int MENU_IMPORT_BOOKMARKS = Menu.FIRST + 2;
	private static final int MENU_CLEAR_BOOKMARKS = Menu.FIRST + 3;
	
	private static final int MENU_OPEN_IN_TAB = Menu.FIRST + 10;
    private static final int MENU_EDIT_BOOKMARK = Menu.FIRST + 11;
    private static final int MENU_DELETE_BOOKMARK = Menu.FIRST + 12;
    
    private static final int ACTIVITY_ADD_BOOKMARK = 0;
    private static final int ACTIVITY_EDIT_BOOKMARK = 1;    
	
	private DbAdapter mDbAdapter;
	
	private Cursor mCursor;
	private BookmarksCursorAdapter mCursorAdapter;
	
	private ProgressDialog mProgressDialog;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmarkslistactivity);
        
        setTitle(R.string.BookmarksListActivity_Title);
        
        mDbAdapter = new DbAdapter(this);
        mDbAdapter.open();
        
        registerForContextMenu(getListView());
        
        fillData();
    }
    
    @Override
	protected void onDestroy() {
		mDbAdapter.close();
		super.onDestroy();
	}

	private void fillData() {
    	mCursor = mDbAdapter.fetchBookmarks();
    	startManagingCursor(mCursor);
    	
    	String[] from = new String[] { BookmarkColumns.TITLE, BookmarkColumns.URL };
    	int[] to = new int[] { R.id.BookmarkRow_Title, R.id.BookmarkRow_Url };
    	
    	mCursorAdapter = new BookmarksCursorAdapter(this, R.layout.bookmarkrow, mCursor, from, to);
        setListAdapter(mCursorAdapter);
        
        setAnimation();
        
        //mCursor.close();
    }
    
    private void setAnimation() {
    	AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(100);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(100);
        set.addAnimation(animation);

        LayoutAnimationController controller =
                new LayoutAnimationController(set, 0.5f);
        ListView listView = getListView();        
        listView.setLayoutAnimation(controller);
    }
    
    private void openAddBookmarkDialog() {
		Intent i = new Intent(this, EditBookmarkActivity.class);
		
		i.putExtra(Constants.EXTRA_ID_BOOKMARK_ID, (long) -1);
		i.putExtra(Constants.EXTRA_ID_BOOKMARK_TITLE, "");
		i.putExtra(Constants.EXTRA_ID_BOOKMARK_URL, "");
		
		startActivityForResult(i, ACTIVITY_ADD_BOOKMARK);
	}
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);            
        
        Intent result = new Intent();
        result.putExtra(Constants.EXTRA_ID_NEW_TAB, false);
        result.putExtra(Constants.EXTRA_ID_URL,  mDbAdapter.getBookmarkById(id)[1]);
        
        setResult(RESULT_OK, result);
        finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem item;
    	item = menu.add(0, MENU_SORT_MODE, 0, R.string.BookmarksListActivity_MenuSortMode);
        item.setIcon(R.drawable.sortmode32);
    	
    	item = menu.add(0, MENU_ADD_BOOKMARK, 0, R.string.BookmarksListActivity_MenuAddBookmark);
        item.setIcon(R.drawable.addbookmark32);
        
        item = menu.add(0, MENU_IMPORT_BOOKMARKS, 0, R.string.BookmarksListActivity_ImportBookmarks);
        item.setIcon(R.drawable.import32);
        
        item = menu.add(0, MENU_CLEAR_BOOKMARKS, 0, R.string.BookmarksListActivity_ClearBookmarks);
        item.setIcon(R.drawable.clear32);
    	
    	return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	
    	switch(item.getItemId()) {
    	case MENU_SORT_MODE:
    		changeSortMode();
    		return true;
    		
    	case MENU_ADD_BOOKMARK:    		
    		openAddBookmarkDialog();
            return true;
            
        case MENU_IMPORT_BOOKMARKS:
            importAndroidBookmarks();
            return true;
            
        case MENU_CLEAR_BOOKMARKS:
        	clearBookmarks();
        	return true;
    	}
    	
    	return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		long id = ((AdapterContextMenuInfo) menuInfo).id;
		if (id != -1) {
			menu.setHeaderTitle(mDbAdapter.getBookmarkById(id)[0]);
		}
		
		menu.add(0, MENU_OPEN_IN_TAB, 0, R.string.BookmarksListActivity_MenuOpenInTab);
        menu.add(0, MENU_EDIT_BOOKMARK, 0, R.string.BookmarksListActivity_MenuEditBookmark);
        menu.add(0, MENU_DELETE_BOOKMARK, 0, R.string.BookmarksListActivity_MenuDeleteBookmark);
    }
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	    	
    	Intent i;
    	
    	switch (item.getItemId()) {
    	case MENU_OPEN_IN_TAB:    	
            i = new Intent();
            i.putExtra(Constants.EXTRA_ID_NEW_TAB, true);
            i.putExtra(Constants.EXTRA_ID_URL, mDbAdapter.getBookmarkById(info.id)[1]);
            setResult(RESULT_OK, i);
            finish();
            return true;
            
    	case MENU_EDIT_BOOKMARK:    		
    		i = new Intent(this, EditBookmarkActivity.class);
    		i.putExtra(Constants.EXTRA_ID_BOOKMARK_ID, info.id);
            i.putExtra(Constants.EXTRA_ID_BOOKMARK_TITLE, mDbAdapter.getBookmarkById(info.id)[0]);
            i.putExtra(Constants.EXTRA_ID_BOOKMARK_URL, mDbAdapter.getBookmarkById(info.id)[1]);
            
            startActivityForResult(i, ACTIVITY_EDIT_BOOKMARK);
            return true;
            
    	case MENU_DELETE_BOOKMARK:
    		mDbAdapter.deleteBookmark(info.id);
    		fillData();
    		return true;
    	}
    	
    	return super.onContextItemSelected(item);
    }
    
    private void doChangeSortMode(int sortMode) {
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
    	editor.putInt(Constants.PREFERENCES_BOOKMARKS_SORT_MODE, sortMode);
    	editor.commit();
    	
    	fillData();
    }
    
    private void changeSortMode() {
    	
    	int currentSort = PreferenceManager.getDefaultSharedPreferences(this).getInt(Constants.PREFERENCES_BOOKMARKS_SORT_MODE, 0);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setInverseBackgroundForced(true);
    	builder.setIcon(android.R.drawable.ic_dialog_info);
    	builder.setTitle(getResources().getString(R.string.BookmarksListActivity_MenuSortMode));
    	builder.setSingleChoiceItems(new String[] { getResources().getString(R.string.BookmarksListActivity_AlphaSortMode),
    													getResources().getString(R.string.BookmarksListActivity_RecentSortMode) },
    			currentSort,
    			new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				doChangeSortMode(which);
				dialog.dismiss();				
			}    		
    	});    	
    	
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    private void importAndroidBookmarks() {    	
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.BookmarksListActivity_ImportingBookmarks));
    	
    	new AndroidImporter(this);
    	
    }
    
    private void doClearBookmarks() {
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.BookmarksListActivity_ClearingBookmarks));
    	
    	new BookmarksCleaner();
    }
    
    private void clearBookmarks() {
    	ApplicationUtils.showYesNoDialog(this,
				android.R.drawable.ic_dialog_alert,
				R.string.BookmarksListActivity_ClearBookmarks,
				R.string.Commons_NoUndoMessage,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						doClearBookmarks();
					}			
		}); 
    }
    
    private class AndroidImporter implements Runnable {
    	
    	private Context mContext;

    	public AndroidImporter(Context context) {
    		mContext = context;
    		
    		new Thread(this).start();
    	}
    	
		@Override
		public void run() {
			Cursor cursor = BookmarksUtils.getAllBookmarks(mContext);
	    	startManagingCursor(cursor);
	    	
	    	if (cursor != null) {
	    		if (cursor.moveToFirst()) {
	    			
	    			String title;
	    			String url;
	    			
	    			do {
	    				
	    				title = cursor.getString(cursor.getColumnIndex(BookmarkColumns.TITLE));
	    				url = cursor.getString(cursor.getColumnIndex(BookmarkColumns.URL));
	    				
	    				mDbAdapter.addBookmark(title, url);
	    				
	    			} while (cursor.moveToNext());	    				    			
	    		}
	    	}
	    	
	    	cursor.close();
	    	
	    	handler.sendEmptyMessage(0);
		}
		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				mProgressDialog.dismiss();
				fillData();
			}
		};
    }
    
    private class BookmarksCleaner implements Runnable {
    	
    	public BookmarksCleaner() {	
    		new Thread(this).start();
    	}
    	
		@Override
		public void run() {
			mDbAdapter.clearBookmarks();
	    	handler.sendEmptyMessage(0);
		}
		
		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				mProgressDialog.dismiss();
				fillData();
			}
		};
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        switch (requestCode) {
		case ACTIVITY_EDIT_BOOKMARK:
			if (resultCode == RESULT_OK) {
				fillData();
			}
			break;
		case ACTIVITY_ADD_BOOKMARK:
			if (resultCode == RESULT_OK) {
				fillData();
			}
			break;
			
		default:
			break;
		}
    }

}
