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

package org.zirco.ui.activities;

import org.zirco.R;
import org.zirco.utils.Constants;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Home page preference chooser activity.
 */
public class HomepagePreferenceActivity extends Activity {
	
	private Spinner mSpinner;
	private EditText mUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		Window w = getWindow();
		w.requestFeature(Window.FEATURE_LEFT_ICON);
		
		setContentView(R.layout.homepagepreferenceactivity);
		
		w.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,	android.R.drawable.ic_dialog_map);
		
		mUrl = (EditText) findViewById(R.id.HomepagePreferenceUrl);
		
		mSpinner = (Spinner) findViewById(R.id.HomepagePreferenceSpinner);
		
		String currentHomepage = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PREFERENCES_GENERAL_HOME_PAGE, Constants.URL_ABOUT_START);
		
		if (currentHomepage.equals(Constants.URL_ABOUT_START)) {
			mSpinner.setSelection(0);
			mUrl.setEnabled(false);
			mUrl.setText(Constants.URL_ABOUT_START);
		} else if (currentHomepage.equals(Constants.URL_ABOUT_BLANK)) {
			mSpinner.setSelection(1);
			mUrl.setEnabled(false);
			mUrl.setText(Constants.URL_ABOUT_BLANK);
		} else {
			mSpinner.setSelection(2);
			mUrl.setEnabled(true);
			mUrl.setText(currentHomepage);					
		}
		
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
				switch (position) {
				case 0: mUrl.setEnabled(false); mUrl.setText(Constants.URL_ABOUT_START); break;
				case 1: mUrl.setEnabled(false); mUrl.setText(Constants.URL_ABOUT_BLANK); break;
				case 2: {
					mUrl.setEnabled(true);
					
					if ((mUrl.getText().toString().equals(Constants.URL_ABOUT_START)) ||
							(mUrl.getText().toString().equals(Constants.URL_ABOUT_BLANK))) {					
						mUrl.setText(null);
					}
					break;
				}
				default: mUrl.setEnabled(false); mUrl.setText(Constants.URL_ABOUT_START); break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) { }
		});
		
		Button okBtn = (Button) findViewById(R.id.HomepagePreferenceOk);
		okBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				savePreference();				
				finish();
			}
		});
		
		Button cancelBtn = (Button) findViewById(R.id.HomepagePreferenceCancel);
		cancelBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	/**
	 * Save the current preference.
	 */
	private void savePreference() {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
    	editor.putString(Constants.PREFERENCES_GENERAL_HOME_PAGE, mUrl.getText().toString());
    	editor.commit();
	}

}
