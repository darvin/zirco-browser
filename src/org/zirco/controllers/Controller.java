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

package org.zirco.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zirco.model.DownloadItem;
import org.zirco.ui.components.ZircoWebView;
import org.zirco.utils.IOUtils;

import android.content.SharedPreferences;
import android.util.Log;

public class Controller {
	
	private static final String ADBLOCK_WHITELIST_FILE = "adblock-whitelist";
	
	private SharedPreferences mPreferences;

	private List<ZircoWebView> mWebViewList;
	private List<DownloadItem> mDownloadList;
	private List<String> mAdBlockWhiteList;
	
	/**
	 * Holder for singleton implementation.
	 */
	private static class ControllerHolder {
		private static final Controller INSTANCE = new Controller();
	}
	
	/**
	 * Get the unique instance of the Controller.
	 * @return The instance of the Controller
	 */
	public static Controller getInstance() {
		return ControllerHolder.INSTANCE;
	}
	
	private Controller() {
		mDownloadList = new ArrayList<DownloadItem>();
		loadAdBlockWhiteList();
	}
		
	public List<ZircoWebView> getWebViewList() {
		return mWebViewList;
	}
	
	public void setWebViewList(List<ZircoWebView> list) {
		mWebViewList = list;
	}
	
	public SharedPreferences getPreferences() {
		return mPreferences;
	}

	public void setPreferences(SharedPreferences mPreferences) {
		this.mPreferences = mPreferences;
	}
	
	public List<DownloadItem> getDownloadList() {
		return mDownloadList;
	}
	
	public void addToDownload(DownloadItem item) {
		mDownloadList.add(item);
	}
	
	public List<String> getAdBlockWhiteList() {
		return mAdBlockWhiteList;
	}
	
	/**
	 * Save the ad blocker white list.
	 */
	public void saveAdBlockWhiteList() {
		File rootFolder = IOUtils.getApplicationFolder();
		
		if (rootFolder != null) {
			
			File adBlockFile = new File(rootFolder, ADBLOCK_WHITELIST_FILE);
			
			if (adBlockFile != null) {
				
				try {
					FileWriter fstream = new FileWriter(adBlockFile);
					BufferedWriter out = new BufferedWriter(fstream);
					
					Iterator<String> iter = mAdBlockWhiteList.iterator();
					while (iter.hasNext()) {
						out.write(iter.next() + "\n");
					}
					
					out.flush();
					out.close();
					
				} catch (IOException e) {
					Log.w("AdBlockWhiteList", "Unable to save AdBlockWhiteList: " + e.getMessage());
				}								
				
			} else {
				Log.w("AdBlockWhiteList", "Unable to save AdBlockWhiteList.");
			}
		} else {
			Log.w("AdBlockWhiteList", "Unable to save AdBlockWhiteList.");
		}
	}

	/**
	 * Build a default ad blocker white list.
	 */
	private void loadDefaultAdBlockWhiteList() {
		mAdBlockWhiteList.add("google.com/reader");
		saveAdBlockWhiteList();
	}
	
	/**
	 * Load the ad blocker white list.
	 */
	private void loadAdBlockWhiteList() {
		
		mAdBlockWhiteList = new ArrayList<String>();
		
		File rootFolder = IOUtils.getApplicationFolder();
		
		if (rootFolder != null) {
			
			File adBlockFile = new File(rootFolder, ADBLOCK_WHITELIST_FILE);
			
			if (adBlockFile != null) {
				
				try {
					String line;
					InputStream is = new FileInputStream(adBlockFile);
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					
					while ((line = reader.readLine()) != null) {
						if (line.length() > 0) {
							mAdBlockWhiteList.add(line);
						}
					}
					
					is.close();
					
				} catch (FileNotFoundException e) {
					Log.w("AdBlockWhiteList", "Unable to load AdBlockWhiteList: " + e.getMessage());
					mAdBlockWhiteList.clear();
					loadDefaultAdBlockWhiteList();
				} catch (IOException e) {
					Log.w("AdBlockWhiteList", "Unable to load AdBlockWhiteList: " + e.getMessage());
					mAdBlockWhiteList.clear();
					loadDefaultAdBlockWhiteList();
				}
				
			} else {
				loadDefaultAdBlockWhiteList();
			}
			
		} else {
			loadDefaultAdBlockWhiteList();
		}
	}
	
}
