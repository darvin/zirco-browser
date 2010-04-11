package org.zirco.ui.activities;

import org.zirco.R;
import org.zirco.model.bookmarks.BookmarksDbAdapter;
import org.zirco.utils.Constants;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class EditBookmarkActivity extends Activity {
	
	private EditText mTitleEditText;
	private EditText mUrlEditText;
	
	private Button mOkButton;
	private Button mCancelButton;
	
	private long mRowId = -1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Window w = getWindow();
		 w.requestFeature(Window.FEATURE_LEFT_ICON);
        
        setContentView(R.layout.editbookmarkactivity);
        
        w.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,	android.R.drawable.ic_input_add);
        
        mTitleEditText = (EditText) findViewById(R.id.EditBookmarkActivity_TitleValue);
        mUrlEditText = (EditText) findViewById(R.id.EditBookmarkActivity_UrlValue);
        
        mOkButton = (Button) findViewById(R.id.EditBookmarkActivity_BtnOk);
        mCancelButton = (Button) findViewById(R.id.EditBookmarkActivity_BtnCancel);
        
        mOkButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mRowId != -1) {
					updateBookmark();
				} else {
					saveBookmark();
				}
				setResult(RESULT_OK);
				finish();
			}
		});
        
        mCancelButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
        
        Bundle extras = getIntent().getExtras();
    	if (extras != null) {
    		
    		String title = extras.getString(Constants.EXTRA_ID_BOOKMARK_TITLE);
    		if ((title != null) &&
    				(title.length() > 0)) {
    			mTitleEditText.setText(title);
    		}
    		
    		String url = extras.getString(Constants.EXTRA_ID_BOOKMARK_URL);
    		if ((url != null) &&
    				(url.length() > 0)) {
    			mUrlEditText.setText(url);
    		} else {
    			mUrlEditText.setHint("http://");
    		}
    		
    		mRowId = extras.getLong(Constants.EXTRA_ID_BOOKMARK_ID);
    		
    	}
    	
    	if (mRowId == -1) {
    		setTitle(R.string.EditBookmarkActivity_TitleAdd);
    	}
	}
	
	private void updateBookmark() {
		if (mUrlEditText.getText().toString() != null) {
			BookmarksDbAdapter dbAdapter = new BookmarksDbAdapter(this);
			dbAdapter.open();

			dbAdapter.updateBookmark(mRowId, mTitleEditText.getText().toString(), mUrlEditText.getText().toString());

			dbAdapter.close();
		}
	}
	
	private void saveBookmark() {		
		if (mUrlEditText.getText().toString() != null) {
			BookmarksDbAdapter dbAdapter = new BookmarksDbAdapter(this);
			dbAdapter.open();

			dbAdapter.addBookmark(mTitleEditText.getText().toString(), mUrlEditText.getText().toString());

			dbAdapter.close();
		}
	}

}
