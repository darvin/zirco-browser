package org.zirco.ui.activities;

import java.util.List;

import org.zirco.R;
import org.zirco.model.DbAdapter;
import org.zirco.model.HistoryItem;
import org.zirco.utils.Constants;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HistoryListActivity extends ExpandableListActivity {
	
	private static final int MENU_CLEAR_HISTORY = Menu.FIRST;
	
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
        
        fillData();
	}	
	
	private void fillData() {
		mData = mDbAdapter.fetchHistory();
		mAdapter = new HistoryExpandableListAdapter();
        setListAdapter(mAdapter);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem item;
    	item = menu.add(0, MENU_CLEAR_HISTORY, 0, R.string.HistoryListActivity_ClearHistory);
        item.setIcon(R.drawable.clear32);
        
        return true;
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		switch(item.getItemId()) {
		case MENU_CLEAR_HISTORY:
        	clearHistory();
        	return true;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,	int groupPosition, int childPosition, long id) {
		
		Intent result = new Intent();
        result.putExtra(Constants.EXTRA_ID_NEW_TAB, false);
        result.putExtra(Constants.EXTRA_ID_URL,  mData.get(groupPosition).get(childPosition).getUrl());
        
        setResult(RESULT_OK, result);
        finish();
		
		return super.onChildClick(parent, v, groupPosition, childPosition, id);
	}

	private void doClearHistory() {
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.HistoryListActivity_ClearingHistory));
    	
    	new HistoryClearer();
    }
	
	private void clearHistory() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setCancelable(true);
    	builder.setIcon(android.R.drawable.ic_dialog_alert);
    	builder.setTitle(getResources().getString(R.string.HistoryListActivity_ClearHistory));
    	builder.setMessage(getResources().getString(R.string.Commons_NoUndoMessage));

    	builder.setInverseBackgroundForced(true);
    	builder.setPositiveButton(getResources().getString(R.string.Commons_Yes), new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			dialog.dismiss();
    			doClearHistory();    			
    		}
    	});
    	builder.setNegativeButton(getResources().getString(R.string.Commons_No), new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			dialog.dismiss();
    		}
    	});
    	AlertDialog alert = builder.create();
    	alert.show();        
    }
	
	private class HistoryClearer implements Runnable {

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
	
	private class HistoryExpandableListAdapter extends BaseExpandableListAdapter {				
		
		private TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 48);

            TextView textView = new TextView(HistoryListActivity.this);
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }
		
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
			urlView.setText(((HistoryItem) getChild(groupPosition, childPosition)).getUrl());
            
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
