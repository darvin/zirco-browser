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

import java.util.List;

import org.zirco.R;
import org.zirco.model.DbAdapter;
import org.zirco.model.HistoryItem;
import org.zirco.utils.ApplicationUtils;
import org.zirco.utils.Constants;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

/**
 * history list activity.
 */
public class HistoryListActivity extends ExpandableListActivity {
	
	private static final int MENU_CLEAR_HISTORY = Menu.FIRST;
	
	private static final int MENU_OPEN_IN_TAB = Menu.FIRST + 10;
	private static final int MENU_DELETE_FROM_HISTORY = Menu.FIRST + 11;
	
	private DbAdapter mDbAdapter;	
	private ExpandableListAdapter mAdapter;
	private List<List<HistoryItem>> mData;
	
	private ProgressDialog mProgressDialog;
	
	private LayoutInflater mInflater = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTitle(R.string.HistoryListActivity_Title);
        
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mDbAdapter = new DbAdapter(this);
        mDbAdapter.open();
        
        registerForContextMenu(getExpandableListView());
        
        fillData();
	}	
	
	@Override
	protected void onDestroy() {
		mDbAdapter.close();
		super.onDestroy();
	}

	/**
	 * Fill the history list.
	 */
	private void fillData() {
		mData = mDbAdapter.fetchHistory();
		mAdapter = new HistoryExpandableListAdapter();
        setListAdapter(mAdapter);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		ExpandableListView.ExpandableListContextMenuInfo info =
			(ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int child =	ExpandableListView.getPackedPositionChild(info.packedPosition);
		
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			menu.setHeaderTitle(mData.get(group).get(child).getTitle());
			menu.add(0, MENU_OPEN_IN_TAB, 0, R.string.HistoryListActivity_MenuOpenInTab);
			menu.add(0, MENU_DELETE_FROM_HISTORY, 0, R.string.HistoryListActivity_MenuDelete);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuItem.getMenuInfo();
		
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int child =	ExpandableListView.getPackedPositionChild(info.packedPosition);
			
			switch (menuItem.getItemId()) {
			case MENU_OPEN_IN_TAB:
				doNavigateToUrl(mData.get(group).get(child).getUrl(), true);
				break;
			case MENU_DELETE_FROM_HISTORY:
				mDbAdapter.deleteFromHistory(mData.get(group).get(child).getId());
				fillData();
				break;
			default:
				break;
			}
		}
		
		return super.onContextItemSelected(menuItem);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem item;
    	item = menu.add(0, MENU_CLEAR_HISTORY, 0, R.string.Commons_ClearHistory);
        item.setIcon(R.drawable.ic_menu_delete);
        
        return true;
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		switch(item.getItemId()) {
		case MENU_CLEAR_HISTORY:
        	clearHistory();
        	return true;
        default: return super.onMenuItemSelected(featureId, item);
		}
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,	int groupPosition, int childPosition, long id) {
		
        doNavigateToUrl(mData.get(groupPosition).get(childPosition).getUrl(), false);
        
		return super.onChildClick(parent, v, groupPosition, childPosition, id);
	}
	
	/**
	 * Load the given url.
	 * @param url The url.
	 * @param newTab If True, will open a new tab. If False, the current tab is used.
	 */
	private void doNavigateToUrl(String url, boolean newTab) {
		Intent result = new Intent();
        result.putExtra(Constants.EXTRA_ID_NEW_TAB, newTab);
        result.putExtra(Constants.EXTRA_ID_URL,  url);
        
        if (getParent() != null) {
        	getParent().setResult(RESULT_OK, result);
        } else {
        	setResult(RESULT_OK, result);
        }
        finish();
	}

	/**
	 * Clear history.
	 */
	private void doClearHistory() {
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.Commons_ClearingHistory));
    	
    	new HistoryClearer();
    }
	
	/**
	 * Display confirmation and clear history.
	 */
	private void clearHistory() {
		ApplicationUtils.showYesNoDialog(this,
				android.R.drawable.ic_dialog_alert,
				R.string.Commons_ClearHistory,
				R.string.Commons_NoUndoMessage,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
		    			doClearHistory();
					}			
		});
    }
	
	/**
	 * Runnable to clear history.
	 */
	private class HistoryClearer implements Runnable {

		/**
		 * Constructor.
		 */
		public HistoryClearer() {
			new Thread(this).start();
		}

		@Override
		public void run() {
			mDbAdapter.clearHistory();
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
	 * Custom expandable adapter for this view.
	 */
	private class HistoryExpandableListAdapter extends BaseExpandableListAdapter {				
		
		/**
		 * Create a new view.
		 * @return The created view.
		 */
		private TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, (int) (35 * getResources().getDisplayMetrics().density));

            TextView textView = new TextView(HistoryListActivity.this);
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding((int) (35 * getResources().getDisplayMetrics().density), 0, 0, 0);
            return textView;
        }
		
		/**
		 * Create a new child view.
		 * @return The created view.
		 */
		private View getChildView() {
			LinearLayout view = (LinearLayout) mInflater.inflate(R.layout.historyrow, null, false);
			
			return view;
		}
		
		@Override
		public Object getChild(int groupPosition, int childPosition) {								
			return mData.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			
			View view = getChildView();
            
			TextView titleView = (TextView) view.findViewById(R.id.HistoryRow_Title);
			titleView.setText(((HistoryItem) getChild(groupPosition, childPosition)).getTitle());
			
			TextView urlView = (TextView) view.findViewById(R.id.HistoryRow_Url);
			
			String url = ((HistoryItem) getChild(groupPosition, childPosition)).getUrl(); 
			
			url = ApplicationUtils.getTruncatedString(urlView.getPaint(), url, (int) (parent.getMeasuredWidth() - (60 * getResources().getDisplayMetrics().density)));
			
			urlView.setText(url);
            
            return view;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mData.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {						
			switch (groupPosition) {
			case 0:
				return String.format(getResources().getString(R.string.Commons_Today), getChildrenCount(groupPosition));

			case 1:
				return String.format(getResources().getString(R.string.Commons_Yesterday), getChildrenCount(groupPosition));
				
			default:
				return String.format(getResources().getString(R.string.Commons_DaysAgo, groupPosition, getChildrenCount(groupPosition)));
			}
		}

		@Override
		public int getGroupCount() {
			return mData.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			
			TextView textView = getGenericView();
            textView.setText(getGroup(groupPosition).toString());
            return textView;
		}

		@Override
		public boolean hasStableIds() {			
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
		
	}

}
