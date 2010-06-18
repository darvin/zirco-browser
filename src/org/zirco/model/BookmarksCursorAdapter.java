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

package org.zirco.model;

import org.zirco.R;
import org.zirco.utils.ApplicationUtils;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Cursor adapter for bookmarks.
 */
public class BookmarksCursorAdapter extends SimpleCursorAdapter {

	/**
	 * Constructor.
	 * @param context The context.
	 * @param layout The layout.
	 * @param c The Cursor. 
	 * @param from Input array.
	 * @param to Output array.
	 */
	public BookmarksCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View superView = super.getView(position, convertView, parent);
		
		TextView urlView = (TextView) superView.findViewById(R.id.BookmarkRow_Url);
		
		String url = urlView.getText().toString();
		
		url = ApplicationUtils.getTruncatedString(urlView.getPaint(), url, parent.getMeasuredWidth() - 40);
		
		urlView.setText(url);
		
		return superView;
	}	

}
