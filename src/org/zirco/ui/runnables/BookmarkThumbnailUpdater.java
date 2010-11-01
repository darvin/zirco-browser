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

package org.zirco.ui.runnables;

import org.zirco.model.DbAdapter;
import org.zirco.ui.components.ZircoWebView;
import org.zirco.utils.Constants;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;

/**
 * Runnable to update a bookmark screen shot in background.
 */
public class BookmarkThumbnailUpdater implements Runnable {
	
	private Context mContext;
	private DbAdapter mDbAdapter;
	private ZircoWebView mWebView;

	/**
	 * Constructor.
	 * @param context The current context.
	 * @param view The view to take screen shot from.
	 */
	public BookmarkThumbnailUpdater(Context context, ZircoWebView view) {
		mContext = context;
		mWebView = view;
	}
	
	/**
	 * Create a screen shot for the current view.
	 * @return A bitmap of the screen shot.
	 */
	private Bitmap createScreenshot() {
		Picture thumbnail = mWebView.capturePicture();
		if (thumbnail == null) {
			return null;
		}
		
		float density = mContext.getResources().getDisplayMetrics().density;
		
		int thumbnailWidth = (int) (Constants.BOOKMARK_THUMBNAIL_WIDTH_FACTOR * density);
		int thumbnailHeight = (int) (Constants.BOOKMARK_THUMBNAIL_HEIGHT_FACTOR * density);
		
		Bitmap bm = Bitmap.createBitmap(thumbnailWidth,
				thumbnailHeight, Bitmap.Config.ARGB_4444);
		
		Canvas canvas = new Canvas(bm);
		
		if (thumbnail.getWidth() > 0) {
			float scaleFactor = (float) thumbnailWidth / (float) thumbnail.getWidth();
			canvas.scale(scaleFactor, scaleFactor);
		}
		
		thumbnail.draw(canvas);
		return bm;
	}
	
	/**
	 * Update the bookmark screen shot.
	 */
	private void updateBookmarkScreenShot() {
		
		Cursor c = mDbAdapter.getBookmarkFromUrl(mWebView.getOriginalUrl(), mWebView.getLoadedUrl());
		
		if ((c != null) &&
				(c.moveToFirst())) {
			
			long id = c.getLong(c.getColumnIndex(DbAdapter.BOOKMARKS_ROWID));
			
			Bitmap bm = createScreenshot();
			
			if (bm != null) {
				
				mDbAdapter.updateBookmarkThumbnail(id, bm);
				
			}
			
			mDbAdapter.updateBookmarkCount(id);
		}
		
		c.close();
	}
	
	@Override
	public void run() {
		mDbAdapter = new DbAdapter(mContext);
		mDbAdapter.open();
		
		updateBookmarkScreenShot();
		
		mDbAdapter.close();
	}

}
