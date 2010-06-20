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

import java.io.ByteArrayInputStream;

import org.zirco.R;
import org.zirco.utils.ApplicationUtils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Cursor adapter for bookmarks.
 */
public class BookmarksCursorAdapter extends SimpleCursorAdapter {
	
	private Cursor mCursor;

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
		mCursor = c;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View superView = super.getView(position, convertView, parent);
		
		TextView urlView = (TextView) superView.findViewById(R.id.BookmarkRow_Url);
		ImageView thumbnailView = (ImageView) superView.findViewById(R.id.BookmarkRow_Thumbnail);
		
		String url = urlView.getText().toString();		
		url = ApplicationUtils.getTruncatedString(urlView.getPaint(), url, parent.getMeasuredWidth() - 40);		
		urlView.setText(url);
		
		byte[] image = getCursor().getBlob(mCursor.getColumnIndex(DbAdapter.BOOKMARKS_THUMBNAIL)); 	
		if (image != null) {
			ByteArrayInputStream imageStream = new ByteArrayInputStream(image);
			Bitmap theImage = BitmapFactory.decodeStream(imageStream);
			thumbnailView.setImageBitmap(theImage);
		} else {
			thumbnailView.setImageBitmap(null);
		}
		
		return superView;
	}	

}
