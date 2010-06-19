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

import org.zirco.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Environment;

/**
 * Application utilities.
 */
public class ApplicationUtils {
	
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

}
