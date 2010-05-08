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

import java.io.File;

import android.os.Environment;

public class IOUtils {
	
	private static final String APPLICATION_FOLDER = "zirco";
	private static final String DOWNLOAD_FOLDER = "downloads";
	
	/**
	 * Get the application folder on the SD Card. Create it if not present.
	 * @return The application folder.
	 */
	public static File getApplicationFolder() {
		File root = Environment.getExternalStorageDirectory();
		if (root.canWrite()) {
			
			File folder = new File(root, APPLICATION_FOLDER);
			
			if (!folder.exists()) {
				folder.mkdir();
			}
			
			return folder;
			
		} else {
			return null;
		}
	}
	
	/**
	 * Get the application download folder on the SD Card. Create it if not present.
	 * @return The application download folder.
	 */
	public static File getDownloadFolder() {
		File root = getApplicationFolder();
		
		if (root != null) {
			
			File folder = new File(root, DOWNLOAD_FOLDER);
			
			if (!folder.exists()) {
				folder.mkdir();
			}
			
			return folder;
			
		} else {
			return null;
		}
	}

}
