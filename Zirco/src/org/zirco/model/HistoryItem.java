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

public class HistoryItem {

	private long mId;
	private String mTitle;
	private String mUrl;
	
	public HistoryItem(long id, String title, String url) {
		mId = id;
		mTitle = title;
		mUrl = url;
	}

	public long getId() {
		return mId;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getUrl() {
		return mUrl;
	}
	
}
