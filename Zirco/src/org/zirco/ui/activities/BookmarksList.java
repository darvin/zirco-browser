package org.zirco.ui.activities;

import org.zirco.R;
import org.zirco.utils.BookmarksUtils;
import org.zirco.utils.Constants;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Browser.BookmarkColumns;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class BookmarksList extends ListActivity {
	
	private Cursor mCursor;
	private SimpleCursorAdapter mCursorAdapter;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmarkslist);
        
        fillData();
    }
    
    private void fillData() {
    	mCursor = BookmarksUtils.getAllBookmarks(this);
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
             
        mCursor.moveToPosition(position);
        String selectedUrl =  mCursor.getString(mCursor.getColumnIndex(BookmarkColumns.URL));
        
        Intent result = new Intent();
        result.putExtra(Constants.EXTRA_ID_URL, selectedUrl);
        
        setResult(RESULT_OK, result);
        finish();
    }

}
