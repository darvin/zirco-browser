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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zirco.R;
import org.zirco.controllers.Controller;
import org.zirco.events.EventConstants;
import org.zirco.events.EventController;
import org.zirco.events.IDownloadEventsListener;
import org.zirco.events.IWebEventListener;
import org.zirco.model.DbAdapter;
import org.zirco.model.DownloadItem;
import org.zirco.model.UrlSuggestionCursorAdapter;
import org.zirco.ui.activities.preferences.PreferencesActivity;
import org.zirco.ui.components.ZircoWebView;
import org.zirco.ui.components.ZircoWebViewClient;
import org.zirco.ui.runnables.BookmarkThumbnailUpdater;
import org.zirco.ui.runnables.HideToolbarsRunnable;
import org.zirco.ui.runnables.HistoryUpdater;
import org.zirco.utils.AnimationManager;
import org.zirco.utils.ApplicationUtils;
import org.zirco.utils.Constants;
import org.zirco.utils.UrlUtils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.FloatMath;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;

/**
 * The application main activity.
 */
public class ZircoMain extends Activity implements IWebEventListener, IToolbarsContainer, OnTouchListener, IDownloadEventsListener {
	
	public static ZircoMain INSTANCE = null;
	
	private static final int FLIP_PIXEL_THRESHOLD = 200;
	private static final int FLIP_TIME_THRESHOLD = 400;
	
	private static final int MENU_ADD_BOOKMARK = Menu.FIRST;
	private static final int MENU_SHOW_BOOKMARKS = Menu.FIRST + 1;
	private static final int MENU_SHOW_DOWNLOADS = Menu.FIRST + 2;	
	private static final int MENU_PREFERENCES = Menu.FIRST + 3;
	private static final int MENU_EXIT = Menu.FIRST + 4;
	private static final int MENU_SELECT_TEXT = Menu.FIRST + 5;
	private static final int MENU_MOBILE_VIEW = Menu.FIRST + 6;
	private static final int MENU_SHARE_PAGE = Menu.FIRST + 7;
	
	private static final int CONTEXT_MENU_OPEN = Menu.FIRST + 10;
	private static final int CONTEXT_MENU_OPEN_IN_NEW_TAB = Menu.FIRST + 11;
	private static final int CONTEXT_MENU_DOWNLOAD = Menu.FIRST + 12;
	private static final int CONTEXT_MENU_COPY = Menu.FIRST + 13;
	private static final int CONTEXT_MENU_SEND_MAIL = Menu.FIRST + 14;
	private static final int CONTEXT_MENU_SHARE = Menu.FIRST + 15;
	
	private static final int OPEN_BOOKMARKS_HISTORY_ACTIVITY = 0;
	private static final int OPEN_DOWNLOADS_ACTIVITY = 1;
	
	private long mDownDateValue;
	private float mDownXValue;
	
	protected LayoutInflater mInflater = null;
	
	private LinearLayout mTopBar;
	private LinearLayout mBottomBar;
	
	private ImageButton mHomeButton;
	private AutoCompleteTextView mUrlEditText;
	private ImageButton mGoButton;
	private ProgressBar mProgressBar;	
	
	private ImageView mBubbleRightView;
	private ImageView mBubbleLeftView;
	
	private ZircoWebView mCurrentWebView;
	private List<ZircoWebView> mWebViews;
	
	private ImageButton mPreviousButton;
	private ImageButton mNextButton;
	
	private ImageButton mNewTabButton;
	private ImageButton mRemoveTabButton;
	
	private ImageButton mQuickButton;
	
	private Drawable mCircularProgress;
	
	private boolean mUrlBarVisible;
	
	private HideToolbarsRunnable mHideToolbarsRunnable;
	
	private ViewFlipper mViewFlipper;
	
	private DbAdapter mDbAdapter = null;
	
	private float mOldDistance;
	
	private GestureMode mGestureMode;
	private long mLastDownTimeForDoubleTap = -1;
	
	/**
	 * Gesture mode.
	 */
	private enum GestureMode {
		SWIP,
		ZOOM
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);              

        INSTANCE = this;
        
        Constants.initializeConstantsFromResources(this);
        
        Controller.getInstance().setPreferences(PreferenceManager.getDefaultSharedPreferences(this));       
        
        if (Controller.getInstance().getPreferences().getBoolean(Constants.PREFERENCES_SHOW_FULL_SCREEN, true)) {
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setProgressBarVisibility(true);
        
        setContentView(R.layout.main);                        
        
        mCircularProgress = getResources().getDrawable(R.drawable.spinner);
        
        EventController.getInstance().addDownloadListener(this);                
                
        mHideToolbarsRunnable = null;
        
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        buildComponents();
        
        EventController.getInstance().addWebListener(this);
        
        mViewFlipper.removeAllViews();      
        
        Intent i = getIntent();
        if (i.getData() != null) {
        	// App first launch from another app.
        	addTab(false);
        	navigateToUrl(i.getDataString());
        } else {
        	// Normal start.
        	int currentVersionCode = ApplicationUtils.getApplicationVersionCode(this);
        	int savedVersionCode = PreferenceManager.getDefaultSharedPreferences(this).getInt(Constants.PREFERENCES_LAST_VERSION_CODE, -1);
        	
        	// If currentVersionCode and savedVersionCode are different, the application has been updated.
        	if (currentVersionCode != savedVersionCode) {
        		// Save current version code.
        		Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            	editor.putInt(Constants.PREFERENCES_LAST_VERSION_CODE, currentVersionCode);
            	editor.commit();
            	
            	// Display changelog dialog.
            	Intent changelogIntent = new Intent(this, ChangelogActivity.class);
        		startActivity(changelogIntent);
        	}
        	
        	addTab(true);
        }
        
        initializeWebIconDatabase();
        
        startToolbarsHideRunnable();
        
    }

    /**
     * Initialize the Web icons database.
     */
    private void initializeWebIconDatabase() {
        
    	final WebIconDatabase db = WebIconDatabase.getInstance();
    	db.open(getDir("icons", 0).getPath());   
    }

    @Override
	protected void onDestroy() {
		if (mDbAdapter != null) {
			mDbAdapter.close();
		}
		
		WebIconDatabase.getInstance().close();
		
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.PREFERENCES_PRIVACY_CLEAR_CACHE_ON_EXIT, false)) {
			mCurrentWebView.clearCache(true);
		}

		super.onDestroy();
	}

	/**
     * Handle url request from external apps.
     * @param intent The intent.
     */
    @Override
	protected void onNewIntent(Intent intent) {
    	if (intent.getData() != null) {
    		addTab(false);
    		navigateToUrl(intent.getDataString());
    	}
		
		setIntent(intent);
		
		super.onNewIntent(intent);
	}
    
    /**
     * Restart the application.
     */
    public void restartApplication() {
    	PendingIntent intent = PendingIntent.getActivity(this.getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags());
		AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, intent);
		System.exit(2);
    }
    
    /**
     * Create main UI.
     */
	private void buildComponents() {
    	
    	mUrlBarVisible = true;
    	
    	mWebViews = new ArrayList<ZircoWebView>();
    	Controller.getInstance().setWebViewList(mWebViews);
    	
    	mBubbleRightView = (ImageView) findViewById(R.id.BubbleRightView);
    	mBubbleRightView.setOnClickListener(new View.OnClickListener() {
    		@Override
			public void onClick(View v) {
				setToolbarsVisibility(true);				
			}
		});    	
    	mBubbleRightView.setVisibility(View.GONE);
    	
    	mBubbleLeftView = (ImageView) findViewById(R.id.BubbleLeftView);
    	mBubbleLeftView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				setToolbarsVisibility(true);
			}
		});
    	mBubbleLeftView.setVisibility(View.GONE);
    	
    	mViewFlipper = (ViewFlipper) findViewById(R.id.ViewFlipper);
    	
    	mTopBar = (LinearLayout) findViewById(R.id.BarLayout);    	
    	mTopBar.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// Dummy event to steel it from the WebView, in case of clicking between the buttons.				
			}
		});
    	
    	mBottomBar = (LinearLayout) findViewById(R.id.BottomBarLayout);    	
    	mBottomBar.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// Dummy event to steel it from the WebView, in case of clicking between the buttons.				
			}
		});   	
    	
    	mDbAdapter = new DbAdapter(this);
    	mDbAdapter.open();

    	String[] from = new String[] {UrlSuggestionCursorAdapter.URL_SUGGESTION_TITLE, UrlSuggestionCursorAdapter.URL_SUGGESTION_URL};
    	int[] to = new int[] {R.id.AutocompleteTitle, R.id.AutocompleteUrl};
    	
    	UrlSuggestionCursorAdapter adapter = new UrlSuggestionCursorAdapter(this, R.layout.url_autocomplete_line, null, from, to);
    	      	
    	adapter.setCursorToStringConverter(new CursorToStringConverter() {			
			@Override
			public CharSequence convertToString(Cursor cursor) {
				String aColumnString = cursor.getString(cursor.getColumnIndex(UrlSuggestionCursorAdapter.URL_SUGGESTION_URL));
                return aColumnString;
			}
		});
    	
    	adapter.setFilterQueryProvider(new FilterQueryProvider() {		
			@Override
			public Cursor runQuery(CharSequence constraint) {
				if ((constraint != null) &&
						(constraint.length() > 0)) {
					return mDbAdapter.getUrlSuggestions(constraint.toString());
				} else {
					return mDbAdapter.getUrlSuggestions(null);
				}
			}
		});
    	
    	mHomeButton = (ImageButton) findViewById(R.id.HomeBtn);
    	mHomeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				navigateToHome();				
			}
		});
    	
    	if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.PREFERENCES_UI_SHOW_HOME_BUTTON, true)) {
    		mHomeButton.setVisibility(View.GONE);
    	}
    	
    	mUrlEditText = (AutoCompleteTextView) findViewById(R.id.UrlText);
    	mUrlEditText.setThreshold(1);
    	mUrlEditText.setAdapter(adapter);    	
    	
    	mUrlEditText.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					navigateToUrl();
					return true;
				}
				return false;
			}
    		
    	});
    	
    	mUrlEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

    		@Override
    		public void onFocusChange(View v, boolean hasFocus) {
    			// Select all when focus gained.
    			if (hasFocus) {
    				mUrlEditText.setSelection(0, mUrlEditText.getText().length());
    			}
    		}
    	});
    	
    	mUrlEditText.setCompoundDrawablePadding(5);
    	    	
    	mGoButton = (ImageButton) findViewById(R.id.GoBtn);    	
    	mGoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	
            	if (mCurrentWebView.isLoading()) {
            		mCurrentWebView.stopLoading();
            	} else {
            		navigateToUrl();
            	}
            }          
        });
    	
    	mProgressBar = (ProgressBar) findViewById(R.id.WebViewProgress);
    	mProgressBar.setMax(100);
    	
    	mPreviousButton = (ImageButton) findViewById(R.id.PreviousBtn);
    	mNextButton = (ImageButton) findViewById(R.id.NextBtn);
    	
    	mPreviousButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	navigatePrevious();
            }          
        });
		
		mNextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	navigateNext();
            }          
        });
    	
		mNewTabButton = (ImageButton) findViewById(R.id.NewTabBtn);
		mNewTabButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	addTab(true);
            }          
        });
		
		mRemoveTabButton = (ImageButton) findViewById(R.id.RemoveTabBtn);
		mRemoveTabButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	removeCurrentTab();
            }          
        });
		
		mQuickButton = (ImageButton) findViewById(R.id.QuickBtn);
		mQuickButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {            	
            	onQuickButton();
            }          
        });

    }
    
    /**
     * Apply preferences to the current UI objects.
     */
    public void applyPreferences() {    	
    	// To update to Bubble position.
    	setToolbarsVisibility(false);
    	
    	if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.PREFERENCES_UI_SHOW_HOME_BUTTON, true)) {
    		mHomeButton.setVisibility(View.GONE);
    	} else {
    		mHomeButton.setVisibility(View.VISIBLE);
    	}
    	
    	for (ZircoWebView view : mWebViews) {
    		view.initializeOptions();
    	}
    }
    
    /**
     * Initialize a newly created WebView.
     */
    private void initializeCurrentWebView() {
    	
    	mCurrentWebView.setWebViewClient(new ZircoWebViewClient());
    	mCurrentWebView.setOnTouchListener((OnTouchListener) this);
    	
    	mCurrentWebView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				HitTestResult result = ((WebView) v).getHitTestResult();
				
				int resultType = result.getType();
				if ((resultType == HitTestResult.ANCHOR_TYPE) ||
						(resultType == HitTestResult.IMAGE_ANCHOR_TYPE) ||
						(resultType == HitTestResult.SRC_ANCHOR_TYPE) ||
						(resultType == HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
					
					Intent i = new Intent();
					i.putExtra(Constants.EXTRA_ID_URL, result.getExtra());
					
					MenuItem item = menu.add(0, CONTEXT_MENU_OPEN, 0, R.string.Main_MenuOpen);
					item.setIntent(i);
	
					item = menu.add(0, CONTEXT_MENU_OPEN_IN_NEW_TAB, 0, R.string.Main_MenuOpenNewTab);					
					item.setIntent(i);
					
					item = menu.add(0, CONTEXT_MENU_COPY, 0, R.string.Main_MenuCopyLinkUrl);					
					item.setIntent(i);
					
					item = menu.add(0, CONTEXT_MENU_DOWNLOAD, 0, R.string.Main_MenuDownload);					
					item.setIntent(i);
					
					item = menu.add(0, CONTEXT_MENU_SHARE, 0, R.string.Main_MenuShareLinkUrl);					
					item.setIntent(i);
				
					menu.setHeaderTitle(result.getExtra());					
				} else if (resultType == HitTestResult.IMAGE_TYPE) {
					Intent i = new Intent();
					i.putExtra(Constants.EXTRA_ID_URL, result.getExtra());
					
					MenuItem item = menu.add(0, CONTEXT_MENU_OPEN, 0, R.string.Main_MenuViewImage);					
					item.setIntent(i);
					
					item = menu.add(0, CONTEXT_MENU_COPY, 0, R.string.Main_MenuCopyImageUrl);					
					item.setIntent(i);
					
					item = menu.add(0, CONTEXT_MENU_DOWNLOAD, 0, R.string.Main_MenuDownloadImage);					
					item.setIntent(i);	
					
					item = menu.add(0, CONTEXT_MENU_SHARE, 0, R.string.Main_MenuShareImageUrl);					
					item.setIntent(i);
					
					menu.setHeaderTitle(result.getExtra());
					
				} else if (resultType == HitTestResult.EMAIL_TYPE) {
					
					Intent sendMail = new Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_MAILTO + result.getExtra()));
					
					MenuItem item = menu.add(0, CONTEXT_MENU_SEND_MAIL, 0, R.string.Main_MenuSendEmail);					
					item.setIntent(sendMail);										
					
					Intent i = new Intent();
					i.putExtra(Constants.EXTRA_ID_URL, result.getExtra());
					
					item = menu.add(0, CONTEXT_MENU_COPY, 0, R.string.Main_MenuCopyEmailUrl);					
					item.setIntent(i);		
					
					item = menu.add(0, CONTEXT_MENU_SHARE, 0, R.string.Main_MenuShareEmailUrl);					
					item.setIntent(i);
					
					menu.setHeaderTitle(result.getExtra());
				}
			}
    		
    	});  	
		
    	mCurrentWebView.setDownloadListener(new DownloadListener() {

			@Override
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				doDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength);
			}
    		
    	});
    	
		final Activity activity = this;
		mCurrentWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				((ZircoWebView) view).setProgress(newProgress);
				
				//activity.setProgress(mCurrentWebView.getProgress() * 100);
				mProgressBar.setProgress(mCurrentWebView.getProgress());
			}
						
			@Override
			public void onReceivedIcon(WebView view, Bitmap icon) {				
				updateFavIcon();
				
				super.onReceivedIcon(view, icon);
			}

			@Override
			public boolean onCreateWindow(WebView view, final boolean dialog, final boolean userGesture, final Message resultMsg) {
				
				WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
				
				addTab(false, mViewFlipper.getDisplayedChild());
				
				transport.setWebView(mCurrentWebView);
				resultMsg.sendToTarget();
				
				return false;
			}
			
			@Override
			public void onReceivedTitle(WebView view, String title) {
				setTitle(String.format(getResources().getString(R.string.ApplicationNameUrl), title)); 
				
				startHistoryUpdaterRunnable(title, mCurrentWebView.getUrl());
				
				super.onReceivedTitle(view, title);
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
				new AlertDialog.Builder(activity)
				.setTitle(R.string.Commons_JavaScriptDialog)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which) {
						result.confirm();
					}
				})
				.setCancelable(false)
				.create()
				.show();

				return true;
			}

			@Override
			public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
				new AlertDialog.Builder(ZircoMain.this)
				.setTitle(R.string.Commons_JavaScriptDialog)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, 
						new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) {
						result.confirm();
					}
				})
				.setNegativeButton(android.R.string.cancel, 
						new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) {
						result.cancel();
					}
				})
				.create()
				.show();

				return true;
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
				
				final LayoutInflater factory = LayoutInflater.from(ZircoMain.this);
                final View v = factory.inflate(R.layout.javascriptpromptdialog, null);
                ((TextView) v.findViewById(R.id.JavaScriptPromptMessage)).setText(message);
                ((EditText) v.findViewById(R.id.JavaScriptPromptInput)).setText(defaultValue);

                new AlertDialog.Builder(ZircoMain.this)
                    .setTitle(R.string.Commons_JavaScriptDialog)
                    .setView(v)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String value = ((EditText) v.findViewById(R.id.JavaScriptPromptInput)).getText()
                                            .toString();
                                    result.confirm(value);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    result.cancel();
                                }
                            })
                    .setOnCancelListener(
                            new DialogInterface.OnCancelListener() {
                                public void onCancel(DialogInterface dialog) {
                                    result.cancel();
                                }
                            })
                    .show();
                
                return true;

			}		
			
		});
    }
    
    /**
     * Select Text in the webview and automatically sends the selected text to the clipboard.
     */
    public void swithToSelectAndCopyTextMode() {
        try {
         KeyEvent shiftPressEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
         shiftPressEvent.dispatch(mCurrentWebView);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Initiate a download. Check the SD card and start the download runnable.
     * @param url The url to download.
     * @param userAgent The user agent.
     * @param contentDisposition The content disposition.
     * @param mimetype The mime type.
     * @param contentLength The content length.
     */
    private void doDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
    	    
        if (ApplicationUtils.checkCardState(this, true)) {
        	DownloadItem item = new DownloadItem(this, url);
        	Controller.getInstance().addToDownload(item);
        	item.startDownload();

        	Toast.makeText(this, getString(R.string.Main_DownloadStartedMsg), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Add a new tab.
     * @param navigateToHome If True, will load the user home page.
     */
    private void addTab(boolean navigateToHome) {
    	addTab(navigateToHome, -1);
    }
    
    /**
     * Add a new tab.
     * @param navigateToHome If True, will load the user home page.
     * @param parentIndex The index of the new tab.
     */
    private void addTab(boolean navigateToHome, int parentIndex) {
    	RelativeLayout view = (RelativeLayout) mInflater.inflate(R.layout.webview, mViewFlipper, false);
    	
    	mCurrentWebView = (ZircoWebView) view.findViewById(R.id.webview);
    	
    	initializeCurrentWebView();    			
		
    	synchronized (mViewFlipper) {
    		if (parentIndex != -1) {
    			mWebViews.add(parentIndex + 1, mCurrentWebView);    		
    			mViewFlipper.addView(view, parentIndex + 1);
    		} else {
    			mWebViews.add(mCurrentWebView);
    			mViewFlipper.addView(view);
    		}
    		mViewFlipper.setDisplayedChild(mViewFlipper.indexOfChild(view));    		
    	}
    	
    	updateUI();
    	
    	mUrlEditText.clearFocus();
    	
    	if (navigateToHome) {
    		navigateToHome();
    	}
    }
    
    /**
     * Remove the current tab.
     */
    private void removeCurrentTab() {
    	
    	int removeIndex = mViewFlipper.getDisplayedChild();
    	
    	mCurrentWebView.doOnPause();
    	
    	synchronized (mViewFlipper) {
    		mViewFlipper.removeViewAt(removeIndex);
    		mViewFlipper.setDisplayedChild(removeIndex - 1);    		
    		mWebViews.remove(removeIndex);    		
    	}
    	
    	mCurrentWebView = mWebViews.get(mViewFlipper.getDisplayedChild());
    	
    	updateUI();
    	
    	mUrlEditText.clearFocus();
    }
    
    /**
     * Change the tool bars visibility.
     * @param visible If True, the tool bars will be shown.
     */
    private void setToolbarsVisibility(boolean visible) {
    	    	
    	if (visible) {
    		
    		if (!mUrlBarVisible) {    			
    			mTopBar.startAnimation(AnimationManager.getInstance().getTopBarShowAnimation());
    			mBottomBar.startAnimation(AnimationManager.getInstance().getBottomBarShowAnimation());
    			
    			mTopBar.setVisibility(View.VISIBLE);
    			mBottomBar.setVisibility(View.VISIBLE);

    			mBubbleRightView.setVisibility(View.GONE);
    			mBubbleLeftView.setVisibility(View.GONE);
    		}
    		
    		startToolbarsHideRunnable();
    		
    		mUrlBarVisible = true;    		    		
    		
    	} else {  	
    		
    		if (mUrlBarVisible) {
    			mTopBar.startAnimation(AnimationManager.getInstance().getTopBarHideAnimation());
    			mBottomBar.startAnimation(AnimationManager.getInstance().getBottomBarHideAnimation());
    			
    			mTopBar.setVisibility(View.GONE);
    			mBottomBar.setVisibility(View.GONE);

    			String bubblePosition = Controller.getInstance().getPreferences().getString(Constants.PREFERENCES_GENERAL_BUBBLE_POSITION, "right");

    			if (bubblePosition.equals("right")) {
    				mBubbleRightView.setVisibility(View.VISIBLE);
    				mBubbleLeftView.setVisibility(View.GONE);
    			} else if (bubblePosition.equals("left")) {
    				mBubbleRightView.setVisibility(View.GONE);
    				mBubbleLeftView.setVisibility(View.VISIBLE);
    			} else if (bubblePosition.equals("both")) {
    				mBubbleRightView.setVisibility(View.VISIBLE);
    				mBubbleLeftView.setVisibility(View.VISIBLE);
    			} else {
    				mBubbleRightView.setVisibility(View.VISIBLE);
    				mBubbleLeftView.setVisibility(View.GONE);
    			}
    		}
			
			mUrlBarVisible = false;
    	}
    }
    
    /**
     * Hide the keyboard.
     * @param delayedHideToolbars If True, will start a runnable to delay tool bars hiding. If False, tool bars are hidden immediatly.
     */
    private void hideKeyboard(boolean delayedHideToolbars) {
    	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(mUrlEditText.getWindowToken(), 0);
    	
    	if (mUrlBarVisible) {
    		if (delayedHideToolbars) {
    			startToolbarsHideRunnable();
    		} else {
    			setToolbarsVisibility(false);
    		}
    	}
    }
    
    /**
     * Start a runnable to hide the tool bars after a user-defined delay.
     */
    private void startToolbarsHideRunnable() {
    	    	    	
    	if (mHideToolbarsRunnable != null) {
    		mHideToolbarsRunnable.setDisabled();
    	}
    	
    	int delay = Integer.parseInt(Controller.getInstance().getPreferences().getString(Constants.PREFERENCES_GENERAL_BARS_DURATION, "3000"));
    	if (delay <= 0) {
    		delay = 3000;
    	}
    	
    	mHideToolbarsRunnable = new HideToolbarsRunnable(this, delay);    	
    	new Thread(mHideToolbarsRunnable).start();
    }
    
    /**
     * Start a runnable to update history.
     * @param title The page title.
     * @param url The page url.
     */
    private void startHistoryUpdaterRunnable(String title, String url) {
    	if ((url != null) &&
    			(url.length() > 0)) {
    		new Thread(new HistoryUpdater(this, title, url)).start();
    	}
    }
    
    /**
     * Navigate to the given url.
     * @param url The url.
     */
    private void navigateToUrl(String url) {
    	// Needed to hide toolbars properly.
    	mUrlEditText.clearFocus();    	
    	
    	if ((url != null) &&
    			(url.length() > 0)) {
    	
    		if (UrlUtils.isUrl(url)) {
    			url = UrlUtils.checkUrl(url);
    		} else {
    			url = UrlUtils.getSearchUrl(this, url);
    		}    		    	
    		
    		hideKeyboard(true);
    		
    		if (url.equals(Constants.URL_ABOUT_START)) {
    			
    			mCurrentWebView.loadDataWithBaseURL("file:///android_asset/startpage/",
    					ApplicationUtils.getStartPage(this), "text/html", "UTF-8", Constants.URL_ABOUT_START);
    			
    		} else {
    			
    			// If the url is not from GWT mobile view, and is in the mobile view url list, then load it with GWT.
    			if ((!url.startsWith(Constants.URL_GOOGLE_MOBILE_VIEW_NO_FORMAT)) &&
    					(UrlUtils.checkInMobileViewUrlList(this, url))) {
    				
    				url = String.format(Constants.URL_GOOGLE_MOBILE_VIEW, url);    				
    			}
    			
    			mCurrentWebView.loadUrl(url);    			
    		}
    	}
    }        
    
    /**
     * Navigate to the url given in the url edit text.
     */
    private void navigateToUrl() {
    	navigateToUrl(mUrlEditText.getText().toString());    	
    }
    
    /**
     * Navigate to the user home page.
     */
    private void navigateToHome() {
    	navigateToUrl(Controller.getInstance().getPreferences().getString(Constants.PREFERENCES_GENERAL_HOME_PAGE,
    			Constants.URL_ABOUT_START));
    }
    
    /**
     * Navigate to the previous page in history.
     */
    private void navigatePrevious() {
    	// Needed to hide toolbars properly.
    	mUrlEditText.clearFocus();
    	
    	hideKeyboard(true);
    	mCurrentWebView.goBack();
    }
    
    /**
     * Navigate to the next page in history. 
     */
    private void navigateNext() {
    	// Needed to hide toolbars properly.
    	mUrlEditText.clearFocus();
    	
    	hideKeyboard(true);
    	mCurrentWebView.goForward();
    }

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			this.moveTaskToBack(true);
			return true;
		default: return super.onKeyLongPress(keyCode, event);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mCurrentWebView.canGoBack()) {
				mCurrentWebView.goBack();				
			} else {
				this.moveTaskToBack(true);
			}
			return true;
		
		default: return super.onKeyUp(keyCode, event);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		String volumeKeysBehaviour = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PREFERENCES_UI_VOLUME_KEYS_BEHAVIOUR, "DEFAULT");
		
		if (!volumeKeysBehaviour.equals("DEFAULT")) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				
				if (volumeKeysBehaviour.equals("SWITCH_TABS")) {
					showPreviousTab();
				} else if (volumeKeysBehaviour.equals("HISTORY")) {
					mCurrentWebView.goForward();
				} else {
					mCurrentWebView.zoomIn();
				}
				
				return true;
			case KeyEvent.KEYCODE_VOLUME_UP:
				
				if (volumeKeysBehaviour.equals("SWITCH_TABS")) {
					showNextTab();
				} else if (volumeKeysBehaviour.equals("HISTORY")) {
					mCurrentWebView.goBack();
				} else {
					mCurrentWebView.zoomOut();
				}
				
				return true;
			default: return super.onKeyDown(keyCode, event);
			}
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * Set the application title to default.
	 */
	private void clearTitle() {
		this.setTitle(getResources().getString(R.string.ApplicationName));
    }
	
	/**
	 * Update the application title.
	 */
	private void updateTitle() {
		String value = mCurrentWebView.getTitle();
    	
    	if ((value != null) &&
    			(value.length() > 0)) {    	
    		this.setTitle(String.format(getResources().getString(R.string.ApplicationNameUrl), value));    		
    	} else {
    		clearTitle();
    	}
	}
	
	/**
	 * Get a Drawable of the current favicon, with its size normalized relative to current screen density.
	 * @return The normalized favicon.
	 */
	private BitmapDrawable getNormalizedFavicon() {
		
		BitmapDrawable favIcon = new BitmapDrawable(getResources(), mCurrentWebView.getFavicon());
		
		if (mCurrentWebView.getFavicon() != null) {
			int favIconSize = ApplicationUtils.getFaviconSize(this);
			
			Bitmap bm = Bitmap.createBitmap(favIconSize, favIconSize, Bitmap.Config.ARGB_4444);
			Canvas canvas = new Canvas(bm);
			
			favIcon.setBounds(0, 0, favIconSize, favIconSize);			
			favIcon.draw(canvas);
			
			favIcon = new BitmapDrawable(getResources(), bm);
		}
		
		return favIcon;
	}
	
	/**
	 * Update the "Go" button image.
	 */
	private void updateGoButton() {		
		if (mCurrentWebView.isLoading()) {
			mGoButton.setImageResource(R.drawable.ic_btn_stop);			
			mUrlEditText.setCompoundDrawablesWithIntrinsicBounds(getNormalizedFavicon(), null, mCircularProgress, null);
			((AnimationDrawable) mCircularProgress).start();
		} else {
			mGoButton.setImageResource(R.drawable.ic_btn_go);			
			mUrlEditText.setCompoundDrawablesWithIntrinsicBounds(getNormalizedFavicon(), null, null, null);			
			((AnimationDrawable) mCircularProgress).stop();
		}
	}
	
	/**
	 * Update the fav icon display.
	 */
	private void updateFavIcon() {		
		mUrlEditText.setCompoundDrawablesWithIntrinsicBounds(getNormalizedFavicon(),
				null,
				mUrlEditText.getCompoundDrawables()[2],
				null);
	}
	
	/**
	 * Update the UI: Url edit text, previous/next button state,...
	 */
	private void updateUI() {
		mUrlEditText.setText(mCurrentWebView.getUrl());
		
		mPreviousButton.setEnabled(mCurrentWebView.canGoBack());
		mNextButton.setEnabled(mCurrentWebView.canGoForward());
		
		mRemoveTabButton.setEnabled(mViewFlipper.getChildCount() > 1);
		
		//setProgress(mCurrentWebView.getProgress() * 100);
		mProgressBar.setProgress(mCurrentWebView.getProgress());
		
		updateGoButton();
		
		updateTitle();
		
		updateFavIcon();
	}
	
	/**
	 * Open the "Add bookmark" dialog.
	 */
	private void openAddBookmarkDialog() {
		Intent i = new Intent(this, EditBookmarkActivity.class);
		
		i.putExtra(Constants.EXTRA_ID_BOOKMARK_ID, (long) -1);
		i.putExtra(Constants.EXTRA_ID_BOOKMARK_TITLE, mCurrentWebView.getTitle());
		i.putExtra(Constants.EXTRA_ID_BOOKMARK_URL, mCurrentWebView.getUrl());
		
		startActivity(i);
	}
	
	/**
	 * Open the bookmark list.
	 */
	private void openBookmarksHistoryActivity() {
    	Intent i = new Intent(this, BookmarksHistoryActivity.class);
    	startActivityForResult(i, OPEN_BOOKMARKS_HISTORY_ACTIVITY);
    }
	
	/**
	 * Open the download list.
	 */
	private void openDownloadsList() {
		Intent i = new Intent(this, DownloadsListActivity.class);
    	startActivityForResult(i, OPEN_DOWNLOADS_ACTIVITY);
	}
	
	/**
	 * Perform the user-defined action when clicking on the quick button.
	 */
	private void onQuickButton() {
		openBookmarksHistoryActivity();
	}
	
	/**
	 * Open preferences.
	 */
	private void openPreferences() {
		Intent preferencesActivity = new Intent(this, PreferencesActivity.class);
  		startActivity(preferencesActivity);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem item;
    	
    	item = menu.add(0, MENU_ADD_BOOKMARK, 0, R.string.Main_MenuAddBookmark);
        item.setIcon(R.drawable.ic_menu_add_bookmark);
        
        item = menu.add(0, MENU_SHOW_BOOKMARKS, 0, R.string.Main_MenuShowBookmarks);
        item.setIcon(R.drawable.ic_menu_bookmarks);
        
        item = menu.add(0, MENU_SHOW_DOWNLOADS, 0, R.string.Main_MenuShowDownloads);
        item.setIcon(R.drawable.ic_menu_downloads);                
        
        item = menu.add(0, MENU_PREFERENCES, 0, R.string.Main_MenuPreferences);
        item.setIcon(R.drawable.ic_menu_preferences);
        
        item = menu.add(0, MENU_EXIT, 0, R.string.Main_MenuExit);
        item.setIcon(R.drawable.ic_menu_exit);
        
        item = menu.add(0, MENU_SELECT_TEXT, 0, R.string.Main_MenuSelectText);
        item.setIcon(R.drawable.ic_menu_select);
        
        item = menu.add(0, MENU_MOBILE_VIEW, 0, R.string.Main_MenuMobileView);
        
        item = menu.add(0, MENU_SHARE_PAGE, 0, R.string.Main_MenuSharePage);
		item.setIcon(android.R.drawable.ic_menu_share);
    	
    	return true;
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch(item.getItemId()) {
    	case MENU_ADD_BOOKMARK:    		
    		openAddBookmarkDialog();
            return true;
    	case MENU_SHOW_BOOKMARKS:    		
    		openBookmarksHistoryActivity();
            return true;
    	case MENU_SHOW_DOWNLOADS:    		
    		openDownloadsList();
            return true;
    	case MENU_PREFERENCES:    		
    		openPreferences();
            return true;
    	case MENU_SELECT_TEXT:
    		swithToSelectAndCopyTextMode();
    		return true;
    	case MENU_EXIT:
    		this.finish();
    		return true;
    	case MENU_MOBILE_VIEW:
    		String currentUrl = mUrlEditText.getText().toString();
    		
    		// Do not reload mobile view if already on it.
    		if (!currentUrl.startsWith(Constants.URL_GOOGLE_MOBILE_VIEW_NO_FORMAT)) {
    			String url = String.format(Constants.URL_GOOGLE_MOBILE_VIEW, mUrlEditText.getText().toString());
    			navigateToUrl(url);
    		}
    		return true;
    	case MENU_SHARE_PAGE:
    		sharePage(mCurrentWebView.getTitle(), mCurrentWebView.getUrl());
    		return true;
        default: return super.onMenuItemSelected(featureId, item);
    	}
    }
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        if (requestCode == OPEN_BOOKMARKS_HISTORY_ACTIVITY) {
        	if (intent != null) {
        		Bundle b = intent.getExtras();
        		if (b != null) {
        			if (b.getBoolean(Constants.EXTRA_ID_NEW_TAB)) {
        				addTab(false);
        			}
        			navigateToUrl(b.getString(Constants.EXTRA_ID_URL));
        		}
        	}
        }
	}
	
	@Override
	protected void onPause() {
		mCurrentWebView.doOnPause();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		mCurrentWebView.doOnResume();
		super.onResume();
	}
	
	/**
	 * Share a page.
	 * @param title The page title.
	 * @param url The page url.
	 */
	private void sharePage(String title, String url) {
    	Intent shareIntent = new Intent(Intent.ACTION_SEND);
    	shareIntent.setType("text/plain");
    	shareIntent.putExtra(Intent.EXTRA_TEXT, url);
    	shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
    	try {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.Main_ShareChooserTitle)));
        } catch(android.content.ActivityNotFoundException ex) {
            // if no app handles it, do nothing
        }
    }

	/**
	 * Show a toast alert on tab switch.
	 */
	private void showToastOnTabSwitch() {
		if (Controller.getInstance().getPreferences().getBoolean(Constants.PREFERENCES_SHOW_TOAST_ON_TAB_SWITCH, true)) {
			String text;
			if (mCurrentWebView.getTitle() != null) {
				text = String.format(getString(R.string.Main_ToastTabSwitchFullMessage), mViewFlipper.getDisplayedChild() + 1, mCurrentWebView.getTitle());
			} else {
				text = String.format(getString(R.string.Main_ToastTabSwitchMessage), mViewFlipper.getDisplayedChild() + 1);
			}
			Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
		}		
	}
	
	/**
	 * Show the previous tab, if any.
	 */
	private void showPreviousTab() {
		
		if (mViewFlipper.getChildCount() > 1) {
			
			mCurrentWebView.doOnPause();
			
			mViewFlipper.setInAnimation(AnimationManager.getInstance().getInFromLeftAnimation());
			mViewFlipper.setOutAnimation(AnimationManager.getInstance().getOutToRightAnimation());

			mViewFlipper.showPrevious();

			mCurrentWebView = mWebViews.get(mViewFlipper.getDisplayedChild());

			mCurrentWebView.doOnResume();
			
			showToastOnTabSwitch();

			updateUI();
		}
	}
	
	/**
	 * Show the next tab, if any.
	 */
	private void showNextTab() {
		
		if (mViewFlipper.getChildCount() > 1) {
			
			mCurrentWebView.doOnPause();
			
			mViewFlipper.setInAnimation(AnimationManager.getInstance().getInFromRightAnimation());
			mViewFlipper.setOutAnimation(AnimationManager.getInstance().getOutToLeftAnimation());

			mViewFlipper.showNext();

			mCurrentWebView = mWebViews.get(mViewFlipper.getDisplayedChild());

			mCurrentWebView.doOnResume();
			
			showToastOnTabSwitch();

			updateUI();
		}
	}
	
	/**
	 * Compute the distance between points of a motion event.
	 * @param event The event.
	 * @return The distance between the two points.
	 */
	private float computeSpacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		hideKeyboard(false);
		
		final int action = event.getAction();
		
		// Get the action that was done on this touch event
		//switch (event.getAction()) {
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			
			mGestureMode = GestureMode.SWIP;
			
			// store the X value when the user's finger was pressed down
			mDownXValue = event.getX();
			mDownDateValue = System.currentTimeMillis();
			
			if (mDownDateValue - mLastDownTimeForDoubleTap < 250) {
				mCurrentWebView.zoomIn();
				mLastDownTimeForDoubleTap = -1;
			} else {
				mLastDownTimeForDoubleTap = mDownDateValue;
			}
			
			break;
		}

		case MotionEvent.ACTION_UP: {
			
			if (mGestureMode == GestureMode.SWIP) {
			
				// Get the X value when the user released his/her finger
				float currentX = event.getX();
				long timeDelta = System.currentTimeMillis() - mDownDateValue;

				if (timeDelta <= FLIP_TIME_THRESHOLD) {
					if (mViewFlipper.getChildCount() > 1) {
						// going backwards: pushing stuff to the right
						if (currentX > (mDownXValue + FLIP_PIXEL_THRESHOLD)) {						

							showPreviousTab();
							return false;
						}

						// going forwards: pushing stuff to the left
						if (currentX < (mDownXValue - FLIP_PIXEL_THRESHOLD)) {					

							showNextTab();
							return false;
						}
					}
				}
			}
			break;
		}
		
		case MotionEvent.ACTION_POINTER_DOWN: {
			
			mOldDistance = computeSpacing(event);
			
			if (mOldDistance > 10f) {
				mGestureMode = GestureMode.ZOOM;
			}
			
			break;
		}				
		
		case MotionEvent.ACTION_MOVE: {
			
			if (mGestureMode == GestureMode.ZOOM) {
			
				float newDist = computeSpacing(event);
				
				if (newDist > 10f) {
					
					float scale = newDist / mOldDistance;
					
					if (scale > 1) {
						
						if (scale > 1.3f) {
						
							mCurrentWebView.zoomIn();							
							mOldDistance = newDist;
						
						}
						
					} else {
						
						if (scale < 0.8f) {
						
							mCurrentWebView.zoomOut();
							mOldDistance = newDist;
							
						}
						
					}					
				}
				
			}		
			break;
		}
		default: break;
		}

        // if you return false, these actions will not be recorded
        return false;

	}
	
	/**
	 * Check if the url is in the AdBlock white list.
	 * @param url The url to check
	 * @return true if the url is in the white list
	 */
	private boolean checkInAdBlockWhiteList(String url) {
		
		if (url != null) {
			boolean inList = false;
			Iterator<String> iter = Controller.getInstance().getAdBlockWhiteList(this).iterator();			
			while ((iter.hasNext()) &&
					(!inList)) {
				if (url.contains(iter.next())) {
					inList = true;
				}
			}
			return inList;
		} else {
			return false;
		}
	}
	
	@Override
	public void onWebEvent(String event, Object data) {
		
		if (event.equals(EventConstants.EVT_WEB_ON_PAGE_FINISHED)) {
			
			updateUI();			
						
			if ((Controller.getInstance().getPreferences().getBoolean(Constants.PREFERENCES_ADBLOCKER_ENABLE, true)) &&
					(!checkInAdBlockWhiteList(mCurrentWebView.getUrl()))) {
				mCurrentWebView.loadAdSweep();
			}
			
			WebIconDatabase.getInstance().retainIconForPageUrl(mCurrentWebView.getUrl());
			
			new Thread(new BookmarkThumbnailUpdater(this, mCurrentWebView)).start();
			
			if (mUrlBarVisible) {
				startToolbarsHideRunnable();
			}
			
		} else if (event.equals(EventConstants.EVT_WEB_ON_PAGE_STARTED)) {
			
			mUrlEditText.setText((CharSequence) data);
			
			mPreviousButton.setEnabled(false);
			mNextButton.setEnabled(false);
			
			updateGoButton();
			
			setToolbarsVisibility(true);
			
		} else if (event.equals(EventConstants.EVT_WEB_ON_URL_LOADING)) {
			
			setToolbarsVisibility(true);
			
		} else if (event.equals(EventConstants.EVT_VND_URL)) {
			
			try {
				
				Intent i  = new Intent(Intent.ACTION_VIEW, Uri.parse((String) data));
				startActivity(i);
				
			} catch (Exception e) {
				
				// Notify user that the vnd url cannot be viewed.
				new AlertDialog.Builder(this)
				.setTitle(R.string.Main_VndErrorTitle)
				.setMessage(String.format(getString(R.string.Main_VndErrorMessage), (String) data))
				.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which) { }
				})
				.setCancelable(true)
				.create()
				.show();
			}
			
		} else if (event.equals(EventConstants.EVT_MAILTO_URL)) {
			Intent sendMail = new Intent(Intent.ACTION_VIEW, Uri.parse((String) data));
			startActivity(sendMail);			
		}
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		Bundle b = item.getIntent().getExtras();
		
		switch(item.getItemId()) {
		case CONTEXT_MENU_OPEN:
			if (b != null) {
				navigateToUrl(b.getString(Constants.EXTRA_ID_URL));
			}			
			return true;
			
		case CONTEXT_MENU_OPEN_IN_NEW_TAB:
			if (b != null) {
				addTab(false, mViewFlipper.getDisplayedChild());
				navigateToUrl(b.getString(Constants.EXTRA_ID_URL));
			}			
			return true;
		
		case CONTEXT_MENU_DOWNLOAD:
			if (b != null) {
				doDownloadStart(b.getString(Constants.EXTRA_ID_URL), null, null, null, 0);
			}
			return true;
		case CONTEXT_MENU_COPY:
			if (b != null) {
				ApplicationUtils.copyTextToClipboard(this, b.getString(Constants.EXTRA_ID_URL), getString(R.string.Commons_UrlCopyToastMessage));
			}
			return true;
		case CONTEXT_MENU_SHARE:
			if (b != null) {
				sharePage("", b.getString(Constants.EXTRA_ID_URL));
			}
			return true;
		default: return super.onContextItemSelected(item);
		}		
	}
	
	/**
	 * Hide the tool bars.
	 */
	public void hideToolbars() {
		if (mUrlBarVisible) {			
			if (!mUrlEditText.hasFocus()) {
				
				if (!mCurrentWebView.isLoading()) {
					setToolbarsVisibility(false);
				}
			}
		}
		mHideToolbarsRunnable = null;
	}

	@Override
	public void onDownloadEvent(String event, Object data) {
		if (event.equals(EventConstants.EVT_DOWNLOAD_ON_FINISHED)) {
			
			DownloadItem item = (DownloadItem) data;
			
			if (item.getErrorMessage() == null) {
				Toast.makeText(this, getString(R.string.Main_DownloadFinishedMsg), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, getString(R.string.Main_DownloadErrorMsg, item.getErrorMessage()), Toast.LENGTH_SHORT).show();
			}
		}			
	}
	
}
