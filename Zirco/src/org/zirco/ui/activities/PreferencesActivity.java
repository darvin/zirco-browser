package org.zirco.ui.activities;

import org.zirco.R;
import org.zirco.utils.Constants;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

public class PreferencesActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferencesactivity);	
		
		EditTextPreference historySizeEditTextPreference = (EditTextPreference) findPreference(Constants.PREFERENCES_BROWSER_HISTORY_SIZE);
		
		EditText myEditText = (EditText) historySizeEditTextPreference.getEditText();
		myEditText.setKeyListener(DigitsKeyListener.getInstance(false, false)); 
	}

}
