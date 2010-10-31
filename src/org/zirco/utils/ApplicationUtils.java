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

package org.zirco.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.zirco.R;
import org.zirco.model.DbAdapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

/**
 * Application utilities.
 */
public class ApplicationUtils {
	
	private static String mAdSweepString = null;
	
	private static String mRawStartPage = null;
	
	/**
	 * Truncate a string to a given maximum width, relative to its paint size.
	 * @param paintObject The object the text will be painted on.
	 * @param text The text to truncate.
	 * @param maxWidth The maximum width of the truncated string.
	 * @return The truncated string.
	 */
	public static String getTruncatedString(Paint paintObject, String text, int maxWidth) {
		
		boolean modified = false;
		
		while ((paintObject.measureText(text) > maxWidth) &&
				(text.length() > 0)) {
			text = text.substring(0, text.length() - 1);
			modified = true;		
		}
		
		if (modified) {
			text += "...";
		}
		
		return text;
	}
	
	/**
	 * Display a standard yes / no dialog.
	 * @param context The current context.
	 * @param icon The dialog icon.
	 * @param title The dialog title.
	 * @param message The dialog message.
	 * @param onYes The dialog listener for the yes button.
	 */
	public static void showYesNoDialog(Context context, int icon, int title, int message, DialogInterface.OnClickListener onYes) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setCancelable(true);
    	builder.setIcon(icon);
    	builder.setTitle(context.getResources().getString(title));
    	builder.setMessage(context.getResources().getString(message));

    	builder.setInverseBackgroundForced(true);
    	builder.setPositiveButton(context.getResources().getString(R.string.Commons_Yes), onYes);
    	builder.setNegativeButton(context.getResources().getString(R.string.Commons_No), new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			dialog.dismiss();
    		}
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
	}
	
	/**
	 * Display a standard Ok / Cancel dialog.
	 * @param context The current context.
	 * @param icon The dialog icon.
	 * @param title The dialog title.
	 * @param message The dialog message.
	 * @param onYes The dialog listener for the yes button.
	 */
	public static void showOkCancelDialog(Context context, int icon, String title, String message, DialogInterface.OnClickListener onYes) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setCancelable(true);
    	builder.setIcon(icon);
    	builder.setTitle(title);
    	builder.setMessage(message);

    	builder.setInverseBackgroundForced(true);
    	builder.setPositiveButton(context.getResources().getString(R.string.Commons_Ok), onYes);
    	builder.setNegativeButton(context.getResources().getString(R.string.Commons_Cancel), new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			dialog.dismiss();
    		}
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
	}
	
	/**
	 * Check if the SD card is available. Display an alert if not.
	 * @param context The current context.
	 * @return True if the SD card is available, false otherwise.
	 */
	public static boolean checkCardState(Context context) {
		// Check to see if we have an SDCard
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            
        	int messageId;

            // Check to see if the SDCard is busy, same as the music app
            if (status.equals(Environment.MEDIA_SHARED)) {
                messageId = R.string.Commons_SDCardErrorSDUnavailable;
            } else {
                messageId = R.string.Commons_SDCardErrorNoSDMsg;
            }
            
            ApplicationUtils.showErrorDialog(context, R.string.Commons_SDCardErrorTitle, messageId);
            
            return false;
        }
        
        return true;
	}
	
	/**
	 * Show an error dialog.
	 * @param context The current context.
	 * @param title The title string id.
	 * @param message The message string id.
	 */
	public static void showErrorDialog(Context context, int title, int message) {
		new AlertDialog.Builder(context)
        .setTitle(title)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setMessage(message)
        .setPositiveButton(R.string.Commons_Ok, null)
        .show();
	}
	
	/**
	 * Load a raw string resource.
	 * @param context The current context.
	 * @param resourceId The resource id.
	 * @return The loaded string.
	 */
	private static String getStringFromRawResource(Context context, int resourceId) {
		String result = null;
		
		InputStream is = context.getResources().openRawResource(resourceId);
		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {					
					sb.append(line).append("\n");
				}
			} catch (IOException e) {
				Log.w("ApplicationUtils", String.format("Unable to load resource %s: %s", resourceId, e.getMessage()));
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					Log.w("ApplicationUtils", String.format("Unable to load resource %s: %s", resourceId, e.getMessage()));
				}
			}
			result = sb.toString();
		} else {        
			result = "";
		}
		
		return result;
	}
	
	/**
	 * Load the AdSweep script if necessary.
	 * @param context The current context.
	 * @return The AdSweep script.
	 */
	public static String getAdSweepString(Context context) {
		if (mAdSweepString == null) {
			InputStream is = context.getResources().openRawResource(R.raw.adsweep);
			if (is != null) {
				StringBuilder sb = new StringBuilder();
				String line;

				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
					while ((line = reader.readLine()) != null) {
						if ((line.length() > 0) &&
								(!line.startsWith("//"))) {
							sb.append(line).append("\n");
						}
					}
				} catch (IOException e) {
					Log.w("AdSweep", "Unable to load AdSweep: " + e.getMessage());
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						Log.w("AdSweep", "Unable to load AdSweep: " + e.getMessage());
					}
				}
				mAdSweepString = sb.toString();
			} else {        
				mAdSweepString = "";
			}
		}
		return mAdSweepString;
	}
	
	/**
	 * Load the changelog string.
	 * @param context The current context.
	 * @return The changelog string.
	 */
	public static String getChangelogString(Context context) {
		return getStringFromRawResource(context, R.raw.changelog);
	}
	
	/**
	 * Load the start page html.
	 * @param context The current context.
	 * @return The start page html.
	 */
	public static String getStartPage(Context context) {
		if (mRawStartPage == null) {
					
			mRawStartPage = getStringFromRawResource(context, R.raw.start);
			
		}
		
		String result = mRawStartPage;
		
		DbAdapter db = new DbAdapter(context);
		db.open();
		
		StringBuilder bookmarksSb = new StringBuilder();
		StringBuilder historySb = new StringBuilder();
		
		Cursor cursor = db.fetchBookmarksWithLimitForStartPage(5);
		
		if ((cursor != null) &&
				(cursor.moveToFirst())) {			
			
			do {
				
				bookmarksSb.append(String.format("<li><a href=\"%s\">%s</a></li>",
						cursor.getString(cursor.getColumnIndex(DbAdapter.BOOKMARKS_URL)),
						cursor.getString(cursor.getColumnIndex(DbAdapter.BOOKMARKS_TITLE))));
				
			} while (cursor.moveToNext());						
		}
		
		cursor.close();
		
		cursor = db.fetchHistoryWithLimitForStartPage(5);
		

		if ((cursor != null) &&
				(cursor.moveToFirst())) {			
			
			do {
				
				historySb.append(String.format("<li><a href=\"%s\">%s</a></li>",
						cursor.getString(cursor.getColumnIndex(DbAdapter.HISTORY_URL)),
						cursor.getString(cursor.getColumnIndex(DbAdapter.HISTORY_TITLE))));
				
			} while (cursor.moveToNext());						
		}
		
		cursor.close();
		
		db.close();
		
		result = String.format(mRawStartPage,
				context.getResources().getString(R.string.StartPage_Title),
				context.getResources().getString(R.string.StartPage_Welcome),
				context.getResources().getString(R.string.StartPage_Bookmarks),
				bookmarksSb.toString(),
				context.getResources().getString(R.string.StartPage_History),
				historySb.toString());
		
		return result;
	}
	
	/**
	 * Get the application version code.
	 * @param context The current context.
	 * @return The application version code.
	 */
	public static int getApplicationVersionCode(Context context) {
    	
		int result = -1;
		
		try {
			
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			
			result = info.versionCode;
			
		} catch (NameNotFoundException e) {
			Log.w("ApplicationUtils", "Unable to get application version: " + e.getMessage());
			result = -1;
		}
		
		return result;
	}

}
