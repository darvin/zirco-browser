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

import java.util.ArrayList;
import java.util.List;

import org.zirco.R;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

public class CustomPagerAdapter extends PagerAdapter {
	
	private Context mContext;
	private CustomViewPager mParent;
	private LayoutInflater mInflater;
	
	private List<RelativeLayout> mViews;

	public CustomPagerAdapter(Context context, CustomViewPager parent) {
		super();
		mContext = context;
		mParent = parent;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mViews = new ArrayList<RelativeLayout>();
	}
	
	public int addPage(int position) {
		RelativeLayout view = (RelativeLayout) mInflater.inflate(R.layout.webview, mParent, false);
		
		if (position == -1) {
			mViews.add(view);
		} else {
			mViews.add(position, view);
		}
		
		int index = mViews.indexOf(view);			
		
		notifyDataSetChanged();
		
		return index;
	}
	
	public void removePage(int index) {
		if (mParent.getChildCount() > 1) {
			int currentIndex = mParent.getCurrentIndex();
			
			if (index == currentIndex) {
				if (currentIndex > 0) {
					currentIndex--;
					mParent.setCurrentItem(currentIndex);
				}
			}
			
			mParent.removeView(mViews.get(index));
			mViews.remove(index);
			
			notifyDataSetChanged();
		}
	}
	
	@Override
	public Object instantiateItem(View collection, int position) {
		View view = mViews.get(position);
		((ViewPager) collection).addView(view, 0);
		
		return view;
	}
	
	@Override
	public void destroyItem(View collection, int position, Object view) {
		((ViewPager) collection).removeView((View) view);
	}
	
	@Override
	public int getCount() {
		return mViews.size();
	}
	
	@Override
	public int getItemPosition(Object object) {
		// Seems to be required as a workaround.
	    return POSITION_NONE;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((RelativeLayout) object);
	}
	
	@Override
	public void finishUpdate(View arg0) { }

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) { }

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) { }

}
