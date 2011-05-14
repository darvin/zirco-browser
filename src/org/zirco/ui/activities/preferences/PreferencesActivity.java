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

package org.zirco.ui.activities.preferences;

import java.util.Iterator;

import org.zirco.R;
import org.zirco.controllers.Controller;
import org.zirco.model.DbAdapter;
import org.zirco.ui.activities.AboutActivity;
import org.zirco.ui.activities.AdBlockerWhiteListActivity;
import org.zirco.ui.activities.ChangelogActivity;
import org.zirco.ui.activities.MobileViewListActivity;
import org.zirco.ui.activities.ZircoMain;
import org.zirco.ui.components.ZircoWebView;
import org.zirco.utils.ApplicationUtils;
import org.zirco.utils.Constants;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.method.DigitsKeyListener;
import android.webkit.CookieManager;
import android.widget.EditText;

/**
 * Preferences activity.
 */
public class PreferencesActivity extends PreferenceActivity {
	
	private ProgressDialog mProgressDialog;
	
	private OnSharedPreferenceChangeListener mPreferenceChangeListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.layout.preferencesactivity);	
		
		EditTextPreference historySizeEditTextPreference = (EditTextPreference) findPreference(Constants.PREFERENCES_BROWSER_HISTORY_SIZE);
		
		EditText myEditText = (EditText) historySizeEditTextPreference.getEditText();
		myEditText.setKeyListener(DigitsKeyListener.getInstance(false, false)); 

		PreferenceCategory browserPreferenceCategory = (PreferenceCategory) findPreference("BrowserPreferenceCategory");
		Preference enablePluginsEclair = (Preference) findPreference(Constants.PREFERENCES_BROWSER_ENABLE_PLUGINS_ECLAIR);
		Preference enablePlugins = (Preference) findPreference(Constants.PREFERENCES_BROWSER_ENABLE_PLUGINS);
		
		if (Build.VERSION.SDK_INT <= 7) {
			browserPreferenceCategory.removePreference(enablePlugins);
		} else {
			browserPreferenceCategory.removePreference(enablePluginsEclair);
		}
		
		Preference userAgentPref = (Preference) findPreference(Constants.PREFERENCES_BROWSER_USER_AGENT);
		userAgentPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openUserAgentActivity();
				return true;
			}
		});				
		
		Preference fullScreenPref = (Preference) findPreference(Constants.PREFERENCES_SHOW_FULL_SCREEN);
		fullScreenPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				askForRestart();
				return true;
			}
		});
		
		Preference hideTitleBarPref = (Preference) findPreference(Constants.PREFERENCES_GENERAL_HIDE_TITLE_BARS);
		hideTitleBarPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				askForRestart();
				return true;
			}
		});
		
		Preference searchUrlPref = (Preference) findPreference(Constants.PREFERENCES_GENERAL_SEARCH_URL);
		searchUrlPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openSearchUrlActivity();
				return true;
			}
		});
		
		Preference homepagePref = (Preference) findPreference(Constants.PREFERENCES_GENERAL_HOME_PAGE);
		homepagePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openHomepageActivity();
				return true;
			}
		});
		
		Preference aboutPref = (Preference) findPreference("About");
		aboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openAboutActivity();
				return true;
			}
		});
		
		Preference changelogPref = (Preference) findPreference("Changelog");
		changelogPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openChangelogActivity();
				return true;
			}
		});
		
		Preference mobileViewPref = (Preference) findPreference("MobileViewList");
		mobileViewPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openMobileViewListActivity();
				return true;
			}			
		});
		
		Preference whiteListPref = (Preference) findPreference("AdBlockerWhiteList");
		whiteListPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openWhiteListActivity();
				return true;
			}			
		});
		
		Preference clearHistoryPref = (Preference) findPreference("PrivacyClearHistory");
		clearHistoryPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				clearHistory();
				return true;
			}			
		});
		
		Preference clearformDataPref = (Preference) findPreference("PrivacyClearFormData");
		clearformDataPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				clearFormData();
				return true;
			}			
		});
		
		Preference clearCachePref = (Preference) findPreference("PrivacyClearCache");
		clearCachePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				clearCache();
				return true;
			}			
		});
		
		Preference clearCookiesPref = (Preference) findPreference("PrivacyClearCookies");
		clearCookiesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				clearCookies();
				return true;
			}			
		});
		
		mPreferenceChangeListener = new OnSharedPreferenceChangeListener() {			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				ZircoMain.INSTANCE.applyPreferences();
			}
		};
		
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
	}
	
	/**
	 * Ask user to restart the app. Do it if click on "Yes".
	 */
	private void askForRestart() {
		ApplicationUtils.showYesNoDialog(this,
				android.R.drawable.ic_dialog_alert,
				R.string.PreferencesActivity_RestartDialogTitle,
				R.string.PreferencesActivity_RestartDialogMessage,
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ZircoMain.INSTANCE.restartApplication();				
			}
			
		});
	}
	
	/**
	 * Display the homepage preference dialog.
	 */
	private void openHomepageActivity() {
		Intent i = new Intent(this, HomepagePreferenceActivity.class);
		startActivity(i);
	}
	
	/**
	 * Display the search url preference dialog.
	 */
	private void openSearchUrlActivity() {
		Intent i = new Intent(this, SearchUrlPreferenceActivity.class);
		startActivity(i);
	}
	
	/**
	 * Display the user agent preference dialog.
	 */
	private void openUserAgentActivity() {
		Intent i = new Intent(this, UserAgentPreferenceActivity.class);
		startActivity(i);
	}
	
	/**
	 * Display the about dialog.
	 */
	private void openAboutActivity() {
		Intent i = new Intent(this, AboutActivity.class);
		startActivity(i);
	}
	
	/**
	 * Display the changelog dialog.
	 */
	private void openChangelogActivity() {
		Intent i = new Intent(this, ChangelogActivity.class);
		startActivity(i);
	}
	
	/**
	 * Display the mobile view list activity.
	 */
	private void openMobileViewListActivity() {
		Intent i = new Intent(this, MobileViewListActivity.class);
		startActivity(i);
	}
	
	/**
	 * Display the ad blocker white list activity.
	 */
	private void openWhiteListActivity() {
		Intent i = new Intent(this, AdBlockerWhiteListActivity.class);
		startActivity(i);
	}
	
	/**
	 * Clear the history.
	 */
	private void doClearHistory() {
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.Commons_ClearingHistory));
    	
    	new HistoryClearer(this);
    }
	
	/**
	 * Display confirmation and clear the history.
	 */
	private void clearHistory() {
		ApplicationUtils.showYesNoDialog(this,
				android.R.drawable.ic_dialog_alert,
				R.string.Commons_ClearHistory,
				R.string.Commons_NoUndoMessage,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
		    			doClearHistory();
					}			
		});
	}
	
	/**
	 * Clear form data.
	 */
	private void doClearFormData() {
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.Commons_ClearingFormData));
    	
    	new FormDataClearer();
    }
	
	/**
	 * Display confirmation and clear form data.
	 */
	private void clearFormData() {
		ApplicationUtils.showYesNoDialog(this,
				android.R.drawable.ic_dialog_alert,
				R.string.Commons_ClearFormData,
				R.string.Commons_NoUndoMessage,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						doClearFormData();
					}			
		});
	}
	
	/**
	 * Clear the cache.
	 */
	private void doClearCache() {
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.Commons_ClearingCache));
    	
    	new CacheClearer();
    }
	
	/**
	 * Display confirmation and clear the cache.
	 */
	private void clearCache() {
		ApplicationUtils.showYesNoDialog(this,
				android.R.drawable.ic_dialog_alert,
				R.string.Commons_ClearCache,
				R.string.Commons_NoUndoMessage,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						doClearCache();
					}			
		});
	}
	
	/**
	 * Clear cookies.
	 */
	private void doClearCookies() {
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.Commons_ClearingCookies));
    	
    	new CookiesClearer();
    }
	
	/**
	 * Display confirmation and clear cookies.
	 */
	private void clearCookies() {
		ApplicationUtils.showYesNoDialog(this,
				android.R.drawable.ic_dialog_alert,
				R.string.Commons_ClearCookies,
				R.string.Commons_NoUndoMessage,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						doClearCookies();
					}			
		});
	}
	
	/**
	 * History clearer thread.
	 */
	private class HistoryClearer implements Runnable {

		private Context mContext;
		
		/**
		 * Constructor.
		 * @param context The current context.
		 */
		public HistoryClearer(Context context) {
			mContext = context;
			new Thread(this).start();
		}

		@Override
		public void run() {
			// Clear DB History
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.clearHistory();
			dbAdapter.close();
			
			// Clear WebViews history
			Iterator<ZircoWebView> iter = Controller.getInstance().getWebViewList().iterator();
			while (iter.hasNext()) {
				iter.next().clearHistory();
			}
			
			handler.sendEmptyMessage(0);
		}

		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				mProgressDialog.dismiss();
			}
		};
	}
	
	/**
	 * Form data clearer thread.
	 */
	private class FormDataClearer implements Runnable {
		/**
		 * Constructor.
		 */
		public FormDataClearer() {
			new Thread(this).start();
		}
		@Override
		public void run() {
			Iterator<ZircoWebView> iter = Controller.getInstance().getWebViewList().iterator();
			while (iter.hasNext()) {
				iter.next().clearFormData();
			}

			handler.sendEmptyMessage(0);
		}
		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				mProgressDialog.dismiss();
			}
		};
	}
	
	/**
	 * Cache clearer thread.
	 */
	private class CacheClearer implements Runnable {
		/**
		 * Constructor.
		 */
		public CacheClearer() {
			new Thread(this).start();
		}
		@Override
		public void run() {
			// Only need to clear the cache from one WebView, as it is application-based.
			ZircoWebView webView = Controller.getInstance().getWebViewList().get(0);
			webView.clearCache(true);

			handler.sendEmptyMessage(0);
		}
		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				mProgressDialog.dismiss();
			}
		};
	}
	
	/**
	 * Cookies clearer thread.
	 */
	private class CookiesClearer implements Runnable {
		/**
		 * Constructor.
		 */
		public CookiesClearer() {
			new Thread(this).start();
		}
		@Override
		public void run() {
			CookieManager.getInstance().removeAllCookie();
			handler.sendEmptyMessage(0);
		}
		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				mProgressDialog.dismiss();
			}
		};
	}

}
