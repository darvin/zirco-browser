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

package org.zirco.ui.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.emergent.android.weave.client.WeaveAccountInfo;
import org.zirco.R;
import org.zirco.model.DbAdapter;
import org.zirco.model.WeaveBookmarkItem;
import org.zirco.sync.ISyncListener;
import org.zirco.sync.WeaveSyncTask;
import org.zirco.ui.activities.preferences.WeavePreferencesActivity;
import org.zirco.utils.ApplicationUtils;
import org.zirco.utils.Constants;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class WeaveBookmarksListActivity extends Activity implements ISyncListener {
	
	private static final int MENU_SYNC = Menu.FIRST;
	private static final int MENU_CLEAR = Menu.FIRST + 1;
	
	private static final String ROOT_FOLDER = "places";
	
	private LinearLayout mNavigationView;
	private TextView mNavigationText;
	private ImageButton mNavigationBack;
	private ListView mListView;
	
	private Button mSetupButton;
	private Button mSyncButton;	
	
	private List<WeaveBookmarkItem> mNavigationList;
	
	private ProgressDialog mProgressDialog;
	
	private DbAdapter mDbAdapter;
	private Cursor mCursor = null;
	
	private WeaveSyncTask mSyncTask;
	
	private static final AtomicReference<AsyncTask<WeaveAccountInfo, Integer, Throwable>> mSyncThread =
	      new AtomicReference<AsyncTask<WeaveAccountInfo, Integer, Throwable>>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weave_bookmarks_list_activity);
        
        mNavigationView = (LinearLayout) findViewById(R.id.WeaveBookmarksNavigationView);
        mNavigationText = (TextView) findViewById(R.id.WeaveBookmarksNavigationText);
        mNavigationBack = (ImageButton) findViewById(R.id.WeaveBookmarksNavigationBack);
        mListView = (ListView) findViewById(R.id.WeaveBookmarksList);
        
        mNavigationBack.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				mNavigationList.remove(mNavigationList.size() - 1);
				if (mNavigationList.size() == 0) {
					mNavigationList.add(new WeaveBookmarkItem(getResources().getString(R.string.WeaveBookmarksListActivity_WeaveRootFolder), null, ROOT_FOLDER, true));
				}
				
				fillData();	
			}
		});
        
        mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
				WeaveBookmarkItem selectedItem = mDbAdapter.getWeaveBookmarkById(id);

				if (selectedItem != null) {
					if (selectedItem.isFolder()) {		
						mNavigationList.add(selectedItem);
						fillData();			
					} else {		
						String url = selectedItem.getUrl();
						
						if (url != null) {				
							Intent result = new Intent();
							result.putExtra(Constants.EXTRA_ID_NEW_TAB, false);
							result.putExtra(Constants.EXTRA_ID_URL, url);

							if (getParent() != null) {
				            	getParent().setResult(RESULT_OK, result);
				            } else {
				            	setResult(RESULT_OK, result);            
				            }
							
							finish();
						}
					}
				}
			}
        });
        
        mListView.setEmptyView(findViewById(R.id.WeaveBookmarksEmptyView));
        
        mSetupButton = (Button) findViewById(R.id.WeaveBookmarksEmptyViewSetupButton);
        mSetupButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(WeaveBookmarksListActivity.this, WeavePreferencesActivity.class));
			}
		});
        
        mSyncButton = (Button) findViewById(R.id.WeaveBookmarksEmptyViewSyncButton);
        mSyncButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				doSync();
			}
		});
        
        mNavigationList = new ArrayList<WeaveBookmarkItem>();
        mNavigationList.add(new WeaveBookmarkItem(getResources().getString(R.string.WeaveBookmarksListActivity_WeaveRootFolder), null, ROOT_FOLDER, true));
        
        mDbAdapter = new DbAdapter(this);
        mDbAdapter.open();
        
        fillData();
	}
	
	@Override
	protected void onDestroy() {
		if (mCursor != null) {
			mCursor.close();
		}
		mDbAdapter.close();		
		super.onDestroy();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem item = menu.add(0, MENU_SYNC, 0, R.string.WeaveBookmarksListActivity_MenuSync);
    	item.setIcon(R.drawable.ic_menu_sync);
    	
    	item = menu.add(0, MENU_CLEAR, 0, R.string.WeaveBookmarksListActivity_MenuSync);
    	item.setIcon(R.drawable.ic_menu_delete);
    	
    	return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch(item.getItemId()) {
		case MENU_SYNC:
			doSync();
			return true;
		case MENU_CLEAR:
			doClear();
			return true;
		default: return super.onMenuItemSelected(featureId, item);
		}
	}
	
	private void doSync() {
		String authToken = ApplicationUtils.getWeaveAuthToken(this);
		
		if (authToken != null) {
			WeaveAccountInfo info = WeaveAccountInfo.createWeaveAccountInfo(authToken);
			mSyncTask = new WeaveSyncTask(this, this);
			
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setTitle(R.string.WeaveSync_SyncTitle);
			mProgressDialog.setMessage(getString(R.string.WeaveSync_GenericSync));
			mProgressDialog.setCancelable(true);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					mSyncTask.cancel(true);
				}
			});
			
			mProgressDialog.show();
			
			boolean retVal = mSyncThread.compareAndSet(null, mSyncTask);
			if (retVal) {
				mSyncTask.execute(info);
			}
			
			
		} else {
			ApplicationUtils.showErrorDialog(this, R.string.Errors_WeaveSyncFailedTitle, R.string.Errors_WeaveAuthFailedMessage);
		}
		
	}
	
	private void doClear() {
		mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.BookmarksListActivity_ClearingBookmarks));
		
		new Clearer();
	}
	
	private void fillData() {
		
		String[] from = { DbAdapter.WEAVE_BOOKMARKS_TITLE, DbAdapter.WEAVE_BOOKMARKS_URL };
		int[] to = { R.id.BookmarkRow_Title, R.id.BookmarkRow_Url };
		
		mCursor = mDbAdapter.getWeaveBookmarksByParentId(mNavigationList.get(mNavigationList.size() - 1).getWeaveId());
		
		ListAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.weave_bookmark_row,
				mCursor,
				from,
				to);	
		
		mListView.setAdapter(adapter);
		
		if (adapter.isEmpty()) {
			mNavigationView.setVisibility(View.GONE);
		} else {
			mNavigationView.setVisibility(View.VISIBLE);
		}
		
		mNavigationText.setText(getNavigationText());
		
		if (mNavigationList.size() > 1) {
			mNavigationBack.setEnabled(true);
		} else {
			mNavigationBack.setEnabled(false);
		}
	}
	
	private String getNavigationText() {
		StringBuilder sb = new StringBuilder();
		
		for (WeaveBookmarkItem navigationItem : mNavigationList) {
			if (sb.length() != 0) {
				sb.append(" > ");
			}
			
			sb.append(navigationItem.getTitle());
		}
		
		return sb.toString();
	}

	@Override
	public void onSyncCancelled() {
		mProgressDialog.dismiss();	
		fillData();
	}

	@Override
	public void onSyncEnd(Throwable result) {
		mSyncThread.compareAndSet(mSyncTask, null);
		if (result != null) {
			String msg = String.format(getResources().getString(R.string.Errors_WeaveSyncFailedMessage), result.getMessage());
			Log.e("MainActivity: Sync failed.", msg);
			
			ApplicationUtils.showErrorDialog(this, R.string.Errors_WeaveSyncFailedTitle, msg);
		}
		
		mProgressDialog.dismiss();
		fillData();
	}

	@Override
	public void onSyncProgress(int done, int total) {
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(total);
		mProgressDialog.setProgress(done);
	}
	
	private class Clearer implements Runnable {

		public Clearer() {
			new Thread(this).start();
		}
		
		@Override
		public void run() {
			mDbAdapter.clearWeaveBookmarks();			
			
			mHandler.sendEmptyMessage(0);
		}
		
		private Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {				
				mProgressDialog.dismiss();
				fillData();
			}
		};
		
	}

}
