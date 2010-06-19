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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;

import org.zirco.R;
import org.zirco.model.BookmarksCursorAdapter;
import org.zirco.model.DbAdapter;
import org.zirco.utils.ApplicationUtils;
import org.zirco.utils.BookmarksUtils;
import org.zirco.utils.Constants;
import org.zirco.utils.DateUtils;
import org.zirco.utils.IOUtils;

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
import android.os.Environment;
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

/**
 * Bookmarks list activity.
 */
public class BookmarksListActivity extends ListActivity {
			
	private static final int MENU_SORT_MODE = Menu.FIRST;
	private static final int MENU_ADD_BOOKMARK = Menu.FIRST + 1;
	private static final int MENU_IMPORT_BOOKMARKS = Menu.FIRST + 2;
	private static final int MENU_EXPORT_BOOKMARKS = Menu.FIRST + 3;
	private static final int MENU_CLEAR_BOOKMARKS = Menu.FIRST + 4;
	
	private static final int MENU_OPEN_IN_TAB = Menu.FIRST + 10;
    private static final int MENU_EDIT_BOOKMARK = Menu.FIRST + 11;
    private static final int MENU_DELETE_BOOKMARK = Menu.FIRST + 12;
    
    private static final int ACTIVITY_ADD_BOOKMARK = 0;
    private static final int ACTIVITY_EDIT_BOOKMARK = 1;    
	
	private DbAdapter mDbAdapter;
	
	private Cursor mCursor;
	private BookmarksCursorAdapter mCursorAdapter;
	
	private ProgressDialog mProgressDialog;
	
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

    /**
     * Fill the bookmark to the list UI. 
     */
	private void fillData() {
    	mCursor = mDbAdapter.fetchBookmarks();
    	startManagingCursor(mCursor);
    	
    	String[] from = new String[] {BookmarkColumns.TITLE, BookmarkColumns.URL};
    	int[] to = new int[] {R.id.BookmarkRow_Title, R.id.BookmarkRow_Url};
    	
    	mCursorAdapter = new BookmarksCursorAdapter(this, R.layout.bookmarkrow, mCursor, from, to);
        setListAdapter(mCursorAdapter);
        
        setAnimation();
        
        //mCursor.close();
    }
    
	/**
	 * Set the list loading animation.
	 */
    private void setAnimation() {
    	AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(100);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(100);
        set.addAnimation(animation);

        LayoutAnimationController controller =
                new LayoutAnimationController(set, 0.5f);
        ListView listView = getListView();        
        listView.setLayoutAnimation(controller);
    }
    
    /**
     * Display the add bookmark dialog.
     */
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
        
        item = menu.add(0, MENU_EXPORT_BOOKMARKS, 0, R.string.BookmarksListActivity_ExportBookmarks);
        item.setIcon(R.drawable.export32);
        
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
            
        case MENU_EXPORT_BOOKMARKS:
        	exportBookmarks();
        	return true;
            
        case MENU_CLEAR_BOOKMARKS:
        	clearBookmarks();
        	return true;
        default: return super.onMenuItemSelected(featureId, item);
    	}
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
    	default: return super.onContextItemSelected(item);
    	}
    }
    
    /**
     * Change list sort mode. Update list.
     * @param sortMode The new sort mode.
     */
    private void doChangeSortMode(int sortMode) {
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
    	editor.putInt(Constants.PREFERENCES_BOOKMARKS_SORT_MODE, sortMode);
    	editor.commit();
    	
    	fillData();
    }
    
    /**
     * Show a dialog for choosing the sort mode.
     * Perform the change if required.
     */
    private void changeSortMode() {
    	
    	int currentSort = PreferenceManager.getDefaultSharedPreferences(this).getInt(Constants.PREFERENCES_BOOKMARKS_SORT_MODE, 0);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setInverseBackgroundForced(true);
    	builder.setIcon(android.R.drawable.ic_dialog_info);
    	builder.setTitle(getResources().getString(R.string.BookmarksListActivity_MenuSortMode));
    	builder.setSingleChoiceItems(new String[] {getResources().getString(R.string.BookmarksListActivity_AlphaSortMode),
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
    
    /**
     * Perform the android bookmarks import.
     */
    private void importAndroidBookmarks() {
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.BookmarksListActivity_ImportingBookmarks));
    	
    	new AndroidImporter(this);
    	
    }
    
    /**
     * Perform the bookmarks export.
     */
    private void exportBookmarks() {
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.BookmarksListActivity_ExportingBookmarks));
    	
    	new XmlBookmarksExporter(this);
    }
    
    /**
     * Clear all the bookmarks.
     */
    private void doClearBookmarks() {
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.BookmarksListActivity_ClearingBookmarks));
    	
    	new BookmarksCleaner();
    }
    
    /**
     * Show a confirmation dialog for bookmarks clearing.
     * Perform the clear if required.
     */
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
    
    /**
     * Runnable for import of Android bookmarks.
     */
    private class AndroidImporter implements Runnable {
    	
    	private Context mContext;

    	/**
    	 * Constructor.
    	 * @param context The current context.
    	 */
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
    
    /**
     * Runnable for bookmarks export to xml.
     */
    private class XmlBookmarksExporter implements Runnable {

    	private Context mContext;
    	
    	/**
    	 * Constructor.
    	 * @param context The current context.
    	 */
    	public XmlBookmarksExporter(Context context) {
    		mContext = context;
    		
    		new Thread(this).start();
    	}
    	
    	/**
    	 * Check if the SD card is available. Display an alert if not.
    	 * @return True if the SD card is available, false otherwise.
    	 */
    	private boolean checkCardState() {
    		// Check to see if we have an SDCard
            String status = Environment.getExternalStorageState();
            if (!status.equals(Environment.MEDIA_MOUNTED)) {
                String msg;

                // Check to see if the SDCard is busy, same as the music app
                if (status.equals(Environment.MEDIA_SHARED)) {
                    msg = getString(R.string.Main_SDCardErrorSDUnavailable);
                } else {
                    msg = getString(R.string.Main_SDCardErrorNoSDMsg);
                }

                new AlertDialog.Builder(mContext)
                    .setTitle(R.string.Main_SDCardErrorTitle)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(msg)
                    .setPositiveButton(R.string.Commons_Ok, null)
                    .show();
                
                return false;
            }
            
            return true;
    	}
    	
		@Override
		public void run() {
			
			if (checkCardState()) {
				
				try {
					
					FileWriter writer = new FileWriter(new File(IOUtils.getBookmarksExportFolder(), DateUtils.getNowForFileName() + ".xml"));
				
					writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					writer.write("<bookmarkslist>\n");
					
					if (mCursor.moveToFirst()) {
					
						do {
							
							writer.write("<bookmark>\n");
							
							writer.write(String.format("<title>%s</title>\n", mCursor.getString(mCursor.getColumnIndex(DbAdapter.BOOKMARKS_TITLE))));
							writer.write(String.format("<url>%s</url>\n", URLEncoder.encode(mCursor.getString(mCursor.getColumnIndex(DbAdapter.BOOKMARKS_URL)))));
							writer.write(String.format("<creationdate>%s</creationdate>\n", mCursor.getString(mCursor.getColumnIndex(DbAdapter.BOOKMARKS_CREATION_DATE))));
							
							writer.write("</bookmark>\n");
							
						} while (mCursor.moveToNext());
						
					}
					
					writer.write("</bookmarkslist>\n");
					
					writer.flush();
					writer.close();
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
			}
			
			handler.sendEmptyMessage(0);
		}
		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				mProgressDialog.dismiss();
			}
		};
    	
    }
    
    /**
     * Runnable for bookmark clearing.
     */
    private class BookmarksCleaner implements Runnable {
    	
    	/**
    	 * Constructor.
    	 */
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
