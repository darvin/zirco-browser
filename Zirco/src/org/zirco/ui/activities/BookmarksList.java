package org.zirco.ui.activities;

import org.zirco.R;
import org.zirco.model.bookmarks.BookmarksDbAdapter;
import org.zirco.utils.BookmarksUtils;
import org.zirco.utils.Constants;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class BookmarksList extends ListActivity {
	
	private static final int MENU_IMPORT_BOOKMARKS = Menu.FIRST;
	private static final int MENU_CLEAR_BOOKMARKS = Menu.FIRST + 1;
	
	private static final int MENU_OPEN_IN_TAB = Menu.FIRST + 10;
    private static final int MENU_EDIT_BOOKMARK = Menu.FIRST + 11;
    private static final int MENU_DELETE_BOOKMARK = Menu.FIRST + 12;
    
    private static final int ACTIVITY_EDIT_BOOKMARK = 0;
	
	private Cursor mCursor;
	private SimpleCursorAdapter mCursorAdapter;
	
	private BookmarksDbAdapter mDbAdapter;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmarkslist);
        
        mDbAdapter = new BookmarksDbAdapter(this);
        mDbAdapter.open();
        
        registerForContextMenu(getListView());
        
        fillData();
    }
    
    private void fillData() {
    	mCursor = mDbAdapter.fetchBookmarks();//BookmarksUtils.getAllBookmarks(this);
    	startManagingCursor(mCursor);
    	
    	String[] from = new String[] { BookmarkColumns.TITLE, BookmarkColumns.URL };
    	int[] to = new int[] { R.id.BookmarkRow_Title, R.id.BookmarkRow_Url };
    	
    	mCursorAdapter = new SimpleCursorAdapter(this, R.layout.bookmarkrow, mCursor, from, to);
        setListAdapter(mCursorAdapter);
        
        setAnimation();
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
        item = menu.add(0, MENU_IMPORT_BOOKMARKS, 0, "Import bookmarks");
        //item.setIcon(R.drawable.newnote32);
        
        item = menu.add(0, MENU_CLEAR_BOOKMARKS, 0, "Clear bookmarks");
    	
    	return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	
    	switch(item.getItemId()) {
        case MENU_IMPORT_BOOKMARKS:
            importAndroidBookmarks();
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
		
		menu.add(0, MENU_OPEN_IN_TAB, 0, R.string.BookmarksList_MenuOpenInTab);
        menu.add(0, MENU_EDIT_BOOKMARK, 0, R.string.BookmarksList_MenuEditBookmark);
        menu.add(0, MENU_DELETE_BOOKMARK, 0, R.string.BookmarksList_MenuDeleteBookmark);
    }
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	    	
    	Intent i;
    	
    	switch(item.getItemId()) {
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
    
    private void importAndroidBookmarks() {
    	Cursor cursor = BookmarksUtils.getAllBookmarks(this);
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
    			
    			fillData();
    		}
    	}    	
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

		default:
			break;
		}
    }

}
