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

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * Combined bookmarks and history activity.
 */
public class BookmarksHistoryActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bookmarkshistoryactivity);
		
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		
		// Bookmarks
		intent = new Intent().setClass(this, BookmarksListActivity.class);
		
		spec = tabHost.newTabSpec("bookmarks").setIndicator(res.getString(R.string.Main_MenuShowBookmarks),
                res.getDrawable(R.drawable.ic_tab_bookmarks))
                .setContent(intent);
		tabHost.addTab(spec);
		
		// History
		intent = new Intent().setClass(this, HistoryListActivity.class);

		spec = tabHost.newTabSpec("history").setIndicator(res.getString(R.string.Main_MenuShowHistory),
                res.getDrawable(R.drawable.ic_tab_history))
                .setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
	}
}
