/*
 * Zirco Browser for Android
 * 
 * Copyright (C) 2010 - 2011 J. Devauchelle and contributors.
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

package org.zirco.ui.components;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {
	
	/**
	 * If true, ViewPager has default behaviour: switch views by fling gesture.
	 */
	private boolean mFlingAllowed;
	
	/**
	 * Maintain our own current index, as ViewPager hide its own :'(.
	 */
	private int mCurrentIndex = 0;
	
	public CustomViewPager(Context context) {
		super(context);
		init();
	}
	
	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				mCurrentIndex = arg0;
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) { }
			
			@Override
			public void onPageScrollStateChanged(int arg0) { }
		});
	}
	
	@Override
	public void setCurrentItem(int item) {
		mCurrentIndex = item;
		super.setCurrentItem(item);
	}
	
	public int getCurrentIndex() {
		return mCurrentIndex;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {		
		if (mFlingAllowed) {
			return super.onInterceptTouchEvent(arg0);
		} else {
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {		
		if (mFlingAllowed) {
			return super.onTouchEvent(arg0);
		} else {
			return false;
		}
	}
	
	public void setFlingAllowed(boolean value) {
		mFlingAllowed = value;
	}

}
