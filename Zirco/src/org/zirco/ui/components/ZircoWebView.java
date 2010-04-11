package org.zirco.ui.components;

import org.zirco.utils.Constants;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class ZircoWebView extends WebView {
	
	private int mProgress = 100;
	
	private Context mContext;
	
	public ZircoWebView(Context context) {
		super(context);
		mContext = context;
		
		initializeOptions();
	}
	
	public ZircoWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        
        initializeOptions();
	}	
	
	public void initializeOptions() {
		WebSettings settings = getSettings();
		
		// User settings		
		settings.setJavaScriptEnabled(PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.PREFERENCES_BROWSER_ENABLE_JAVASCRIPT, true));
		settings.setLoadsImagesAutomatically(PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.PREFERENCES_BROWSER_ENABLE_IMAGES, true));
		settings.setSaveFormData(PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.PREFERENCES_BROWSER_ENABLE_FORM_DATA, true));
		settings.setSavePassword(PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.PREFERENCES_BROWSER_ENABLE_PASSWORDS, true));
		
		CookieManager.getInstance().setAcceptCookie(PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.PREFERENCES_BROWSER_ENABLE_COOKIES, true));
		
		settings.setSupportZoom(true);
		
		// Technical settings
		settings.setSupportMultipleWindows(true);						
    	setLongClickable(true);
	}
	
	public void setProgress(int progress) {
		mProgress = progress;
	}
	
	public int getProgress() {
		return mProgress;
	}
	
	public void notifyPageFinished() {
		mProgress = 100;
	}

}
