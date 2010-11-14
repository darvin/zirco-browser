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
import java.io.InputStream;

import org.zirco.R;
import org.zirco.utils.ApplicationUtils;
import org.zirco.utils.Constants;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Cursor adapter for bookmarks.
 */
public class BookmarksCursorAdapter extends SimpleCursorAdapter {

	private Bitmap mDefaultImage;
	
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

		createDefaultImage(context);
	}
	
	/**
	 * Create the default image for bookmarks that do not have a screen shot.
	 * @param context The current context.
	 */
	private void createDefaultImage(Context context) {
		float density = context.getResources().getDisplayMetrics().density;
		
		int thumbnailWidth = (int) (Constants.BOOKMARK_THUMBNAIL_WIDTH_FACTOR * density);
		int thumbnailHeight = (int) (Constants.BOOKMARK_THUMBNAIL_HEIGHT_FACTOR * density);
		
		InputStream is = context.getResources().openRawResource(R.drawable.bookmarkthumbnail64);
		
		Bitmap inputImage = BitmapFactory.decodeStream(is);
	
		float scaleWidth = ((float) thumbnailWidth) / inputImage.getWidth();
		float scaleHeight = ((float) thumbnailHeight) / inputImage.getHeight();
		
		Matrix matrix = new Matrix();	
		matrix.postScale(scaleWidth, scaleHeight);
		
		mDefaultImage = Bitmap.createBitmap(inputImage, 0, 0,
				inputImage.getWidth(), inputImage.getHeight(), matrix, true); 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View superView = super.getView(position, convertView, parent);
		
		TextView urlView = (TextView) superView.findViewById(R.id.BookmarkRow_Url);
		ImageView thumbnailView = (ImageView) superView.findViewById(R.id.BookmarkRow_Thumbnail);
		
		String url = urlView.getText().toString();
		url = ApplicationUtils.getTruncatedString(urlView.getPaint(), url, (int) (2 * (parent.getMeasuredWidth() - (130 * parent.getContext().getResources().getDisplayMetrics().density))));
		urlView.setText(url);
		
		byte[] image = getCursor().getBlob(getCursor().getColumnIndex(DbAdapter.BOOKMARKS_THUMBNAIL)); 	
		if (image != null) {
			ByteArrayInputStream imageStream = new ByteArrayInputStream(image);
			Bitmap theImage = BitmapFactory.decodeStream(imageStream);
			thumbnailView.setImageBitmap(theImage);
		} else {
			thumbnailView.setImageBitmap(mDefaultImage);
		}
		
		return superView;
	}	

}
