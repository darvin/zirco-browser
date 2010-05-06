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

public class ApplicationUtils {
	
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

}
