package org.zirco.ui.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.zirco.R;
import org.zirco.controllers.Controller;
import org.zirco.events.EventConstants;
import org.zirco.events.EventController;
import org.zirco.events.IDownloadEventsListener;
import org.zirco.events.IWebEventListener;
import org.zirco.model.DownloadItem;
import org.zirco.ui.components.ZircoWebView;
import org.zirco.ui.components.ZircoWebViewClient;
import org.zirco.ui.runnables.HideToolbarsRunnable;
import org.zirco.ui.runnables.HistoryUpdater;
import org.zirco.utils.AnimationManager;
import org.zirco.utils.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ZircoMain extends Activity implements IWebEventListener, IToolbarsContainer, OnTouchListener, IDownloadEventsListener {
	
	private static final int FLIP_PIXEL_THRESHOLD = 200;
	private static final int FLIP_TIME_THRESHOLD = 400;
	
	private static final int MENU_ADD_BOOKMARK = Menu.FIRST;
	private static final int MENU_SHOW_BOOKMARKS = Menu.FIRST + 1;
	private static final int MENU_SHOW_HISTORY = Menu.FIRST + 2;
	private static final int MENU_SHOW_DOWNLOADS = Menu.FIRST + 3;
	private static final int MENU_PREFERENCES = Menu.FIRST + 4;
	private static final int MENU_ABOUT = Menu.FIRST + 5;
	
	private static final int CONTEXT_MENU_OPEN = Menu.FIRST + 10;
	private static final int CONTEXT_MENU_OPEN_IN_NEW_TAB = Menu.FIRST + 11;
	private static final int CONTEXT_MENU_DOWNLOAD = Menu.FIRST + 12;
	
	private static final int OPEN_BOOKMARKS_ACTIVITY = 0;
	private static final int OPEN_HISTORY_ACTIVITY = 1;
	private static final int OPEN_DOWNLOADS_ACTIVITY = 2;
	
	private long mDownDateValue;
	private float mDownXValue;
	
	protected LayoutInflater mInflater = null;
	
	private LinearLayout mTopBar;
	private LinearLayout mBottomBar;
	
	private ImageButton mHomeButton;
	private EditText mUrlEditText;
	private ImageButton mGoButton;
	
	private ImageView mBubleView;
	
	private ZircoWebView mCurrentWebView;
	private List<ZircoWebView> mWebViews;
	
	private ImageButton mPreviousButton;
	private ImageButton mNextButton;
	
	private ImageButton mNewTabButton;
	private ImageButton mRemoveTabButton;
	
	private ImageButton mQuickButton;
	
	private boolean mUrlBarVisible;
	
	private HideToolbarsRunnable mHideToolbarsRunnable;
	
	private ViewFlipper mViewFlipper;
	
	private String mAdSweepString = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);

        setProgressBarVisibility(true);
        
        setContentView(R.layout.main);
        
        Controller.getInstance().setPreferences(PreferenceManager.getDefaultSharedPreferences(this));        
        
        EventController.getInstance().addDownloadListener(this);
        
        mHideToolbarsRunnable = null;
        
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        buildComponents();
        
        EventController.getInstance().addWebListener(this);
        
        mViewFlipper.removeAllViews();
        
        Controller.getInstance().getPreferences().registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				applyPreferences();
			}        	
        });
        
        addTab(true);
        
        startToolbarsHideRunnable();
    }
    
    private void buildComponents() {
    	
    	mUrlBarVisible = true;
    	
    	mWebViews = new ArrayList<ZircoWebView>();
    	Controller.getInstance().setWebViewList(mWebViews);
    	
    	mBubleView = (ImageView) findViewById(R.id.BubleView);
    	mBubleView.setOnClickListener(new View.OnClickListener() {		
			public void onClick(View v) {
				setToolbarsVisibility(true);				
			}
		});
    	
    	mBubleView.setVisibility(View.GONE);
    	
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
    	
    	mHomeButton = (ImageButton) findViewById(R.id.HomeBtn);    	
    	mHomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	navigateToHome();
            }          
        });
    	
    	mUrlEditText = (EditText) findViewById(R.id.UrlText);
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
    	    	
    	mGoButton = (ImageButton) findViewById(R.id.GoBtn);    	
    	mGoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	navigateToUrl();
            }          
        });
    	
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
            	removeTab();
            }          
        });
		
		mQuickButton = (ImageButton) findViewById(R.id.QuickBtn);
		mQuickButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {            	
            	onQuickButton();
            }          
        });
		applyQuickButtonPreferences();
    	
    }
    
    private void applyQuickButtonPreferences() {
    	String buttonPref = Controller.getInstance().getPreferences().getString(Constants.PREFERENCES_GENERAL_QUICK_BUTTON, "bookmarks");
		
		if (buttonPref.equals("bookmarks")) {
			mQuickButton.setImageResource(R.drawable.bookmarks32);
		} else if (buttonPref.equals("history")) {
			mQuickButton.setImageResource(R.drawable.history32);
		} else {
			mQuickButton.setImageResource(R.drawable.bookmarks32);
		}
		mQuickButton.invalidate();
    }
    
    private void applyPreferences() {
    	
    	applyQuickButtonPreferences();
    	
    	Iterator<ZircoWebView> iter = mWebViews.iterator();
    	while (iter.hasNext()) {
    		iter.next().initializeOptions();
    	}
    }
    
    private void initializeCurrentWebView() {
    	
    	mCurrentWebView.setWebViewClient(new ZircoWebViewClient());
    	mCurrentWebView.setOnTouchListener((OnTouchListener) this);
    	
    	mCurrentWebView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				HitTestResult result = ((WebView) v).getHitTestResult();
				
				int resultType = result.getType();
				if ((resultType == HitTestResult.ANCHOR_TYPE) ||
						(resultType == HitTestResult.SRC_ANCHOR_TYPE) ||
						(resultType == HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
					
					Intent i = new Intent();
					i.putExtra(Constants.EXTRA_ID_URL, result.getExtra());
					
					MenuItem item = menu.add(0, CONTEXT_MENU_OPEN, 0, R.string.Main_MenuOpen);
					item.setIntent(i);
	
					item = menu.add(0, CONTEXT_MENU_OPEN_IN_NEW_TAB, 0, R.string.Main_MenuOpenNewTab);					
					item.setIntent(i);
					
					item = menu.add(0, CONTEXT_MENU_DOWNLOAD, 0, R.string.Main_MenuDownload);					
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
				
				activity.setProgress(mCurrentWebView.getProgress() * 100);
			}
			
			@Override
			public boolean onCreateWindow(WebView view, final boolean dialog, final boolean userGesture, final Message resultMsg) {
				
				WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;

				addTab(false);
				
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
					public void onClick(DialogInterface dialog, int which)
					{
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
					public void onClick(DialogInterface dialog, int which) 
					{
						result.confirm();
					}
				})
				.setNegativeButton(android.R.string.cancel, 
						new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						result.cancel();
					}
				})
				.create()
				.show();

				return true;
			}								
		});
    }
    
    private void doDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
    	    			
		// Check to see if we have an SDCard
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            String msg;

            // Check to see if the SDCard is busy, same as the music app
            if (status.equals(Environment.MEDIA_SHARED)) {
                msg = getString(R.string.Main_SDCardErrorSDUnavailable);
            } else {
                msg = getString(R.string.Main_SDCardErrorNoSDMsg);
            }

            new AlertDialog.Builder(this)
                .setTitle(R.string.Main_SDCardErrorTitle)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(msg)
                .setPositiveButton(R.string.Commons_Ok, null)
                .show();
            return;
        }
        DownloadItem item = new DownloadItem(url);
        Controller.getInstance().addToDownload(item);
        item.startDownload();
        
        Toast.makeText(this, getString(R.string.Main_DownloadStartedMsg), Toast.LENGTH_SHORT).show();
    }
    
    private void addTab(boolean navigateToHome) {
    	RelativeLayout view = (RelativeLayout) mInflater.inflate(R.layout.webview, mViewFlipper, false);
    	
    	mCurrentWebView = (ZircoWebView) view.findViewById(R.id.webview);
    	
    	initializeCurrentWebView();    			
		
		mWebViews.add(mCurrentWebView);
		
    	synchronized (mViewFlipper) {
    		mViewFlipper.addView(view);
    		mViewFlipper.setDisplayedChild(mViewFlipper.indexOfChild(view));    		
    	}
    	
    	updateUI();
    	
    	mUrlEditText.clearFocus();
    	
    	if (navigateToHome) {
    		navigateToHome();
    	}
    }
    
    private void removeTab() {
    	
    	int removeIndex = mViewFlipper.getDisplayedChild();
    	
    	synchronized (mViewFlipper) {
    		mViewFlipper.removeViewAt(removeIndex);
    		mWebViews.remove(removeIndex);    		
    	}
    	
    	mCurrentWebView = mWebViews.get(mViewFlipper.getDisplayedChild());
    	
    	updateUI();
    	
    	mUrlEditText.clearFocus();
    }
    
    private void setToolbarsVisibility(boolean visible) {
    	    	
    	if (visible) {
    		
    		mTopBar.setVisibility(View.VISIBLE);
    		mBottomBar.setVisibility(View.VISIBLE);
    		
    		mBubleView.setVisibility(View.GONE);    		    		    		 
    		
    		startToolbarsHideRunnable();
    		
    		mUrlBarVisible = true;    		    		
    		
    	} else {  	
    		
    		mTopBar.setVisibility(View.GONE);
    		mBottomBar.setVisibility(View.GONE);
    		
			mBubleView.setVisibility(View.VISIBLE);
			
			mUrlBarVisible = false;
    	}
    }
    
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
    
    private void startHistoryUpdaterRunnable(String title, String url) {
    	new Thread(new HistoryUpdater(this, title, url)).start();
    }
    
    private void navigateToUrl(String url) {
    	// Needed to hide toolbars properly.
    	mUrlEditText.clearFocus();    	
    	
    	if ((url != null) &&
    			(url.length() > 0)) {
    	
    		if ((!url.startsWith("http://")) &&
    				(!url.startsWith("https://")) &&
    				(!url.startsWith("about:blank"))) {
    			
    			url = "http://" + url;
    			
    		}
    		
    		hideKeyboard(true);
    		mCurrentWebView.loadUrl(url);
    	}
    }
    
    private void navigateToUrl() {
    	navigateToUrl(mUrlEditText.getText().toString());    	
    }
    
    private void navigateToHome() {
    	navigateToUrl(Controller.getInstance().getPreferences().getString(Constants.PREFERENCES_GENERAL_HOME_PAGE,
    			getResources().getString(R.string.PreferencesActivity_HomePagePreferenceDefaultValue)));
    }
    
    private void navigatePrevious() {
    	// Needed to hide toolbars properly.
    	mUrlEditText.clearFocus();
    	
    	hideKeyboard(true);
    	mCurrentWebView.goBack();
    }
    
    private void navigateNext() {
    	// Needed to hide toolbars properly.
    	mUrlEditText.clearFocus();
    	
    	hideKeyboard(true);
    	mCurrentWebView.goForward();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mCurrentWebView.canGoBack()) {
				mCurrentWebView.goBack();				
			}
			return true;
			
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			mCurrentWebView.zoomIn();
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			mCurrentWebView.zoomOut();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	private void clearTitle() {
		this.setTitle(getResources().getString(R.string.ApplicationName));
    }
	
	private void updateTitle() {
		String value = mCurrentWebView.getTitle();
    	
    	if ((value != null) &&
    			(value.length() > 0)) {    	
    		this.setTitle(String.format(getResources().getString(R.string.ApplicationNameUrl), value));    		
    	} else {
    		clearTitle();
    	}
	}
	
	private void updateUI() {
		mUrlEditText.setText(mCurrentWebView.getUrl());
		
		mPreviousButton.setEnabled(mCurrentWebView.canGoBack());
		mNextButton.setEnabled(mCurrentWebView.canGoForward());
		
		mRemoveTabButton.setEnabled(mViewFlipper.getChildCount() > 1);
		
		setProgress(mCurrentWebView.getProgress() * 100);
		
		updateTitle();
	}
	
	private void openAboutDialog() {
		Intent i = new Intent(this, AboutActivity.class);
		startActivity(i);
	}
	
	private void openAddBookmarkDialog() {
		Intent i = new Intent(this, EditBookmarkActivity.class);
		
		i.putExtra(Constants.EXTRA_ID_BOOKMARK_ID, (long) -1);
		i.putExtra(Constants.EXTRA_ID_BOOKMARK_TITLE, mCurrentWebView.getTitle());
		i.putExtra(Constants.EXTRA_ID_BOOKMARK_URL, mCurrentWebView.getUrl());
		
		startActivity(i);
	}
	
	private void openBookmarksList() {
    	Intent i = new Intent(this, BookmarksListActivity.class);
    	startActivityForResult(i, OPEN_BOOKMARKS_ACTIVITY);
    }
	
	private void openHistoryList() {
		Intent i = new Intent(this, HistoryListActivity.class);
    	startActivityForResult(i, OPEN_HISTORY_ACTIVITY);
    }
	
	private void openDownloadsList() {
		Intent i = new Intent(this, DownloadsListActivity.class);
    	startActivityForResult(i, OPEN_DOWNLOADS_ACTIVITY);
	}
	
	private void onQuickButton() {
		String buttonPref = Controller.getInstance().getPreferences().getString(Constants.PREFERENCES_GENERAL_QUICK_BUTTON, "bookmarks");
		
		if (buttonPref.equals("bookmarks")) {
			openBookmarksList();
		} else if (buttonPref.equals("history")) {
			openHistoryList();
		} else {
			openBookmarksList();
		}
	}
	
	private void openPreferences() {
		Intent preferencesActivity = new Intent(this, PreferencesActivity.class);
  		startActivity(preferencesActivity);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem item;
    	
    	item = menu.add(0, MENU_ADD_BOOKMARK, 0, R.string.Main_MenuAddBookmark);
        item.setIcon(R.drawable.addbookmark32);
        
        item = menu.add(0, MENU_SHOW_BOOKMARKS, 0, R.string.Main_MenuShowBookmarks);
        item.setIcon(R.drawable.bookmarks32);
        
        item = menu.add(0, MENU_SHOW_HISTORY, 0, R.string.Main_MenuShowHistory);
        item.setIcon(R.drawable.history32);
        
        item = menu.add(0, MENU_SHOW_DOWNLOADS, 0, R.string.Main_MenuShowDownloads);
        item.setIcon(R.drawable.downloads32);
        
        item = menu.add(0, MENU_PREFERENCES, 0, R.string.Main_MenuPreferences);
        item.setIcon(R.drawable.preferences32);
        
        item = menu.add(0, MENU_ABOUT, 0, R.string.Main_MenuAbout);
        item.setIcon(R.drawable.about32);
    	
    	return true;
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch(item.getItemId()) {
    	case MENU_ADD_BOOKMARK:    		
    		openAddBookmarkDialog();
            return true;
    	case MENU_SHOW_BOOKMARKS:    		
    		openBookmarksList();
            return true;
    	case MENU_SHOW_HISTORY:    		
    		openHistoryList();
            return true;
    	case MENU_SHOW_DOWNLOADS:    		
    		openDownloadsList();
            return true;
    	case MENU_PREFERENCES:    		
    		openPreferences();
            return true;
    	case MENU_ABOUT:
    		openAboutDialog();
            return true;
    	}
    	
    	return super.onMenuItemSelected(featureId, item);
    }
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        if ((requestCode == OPEN_BOOKMARKS_ACTIVITY) ||
        		(requestCode == OPEN_HISTORY_ACTIVITY)) {
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
	public boolean onTouch(View v, MotionEvent event) {
		
		hideKeyboard(false);
		
		// Get the action that was done on this touch event
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN: {
			// store the X value when the user's finger was pressed down
			mDownXValue = event.getX();
			mDownDateValue = new Date().getTime();
			break;
		}

		case MotionEvent.ACTION_UP: {
			// Get the X value when the user released his/her finger
			float currentX = event.getX();
			long timeDelta = new Date().getTime() - mDownDateValue;
			
			if (timeDelta <= FLIP_TIME_THRESHOLD) {
				if (mViewFlipper.getChildCount() > 1) {
					// going backwards: pushing stuff to the right
					if (currentX > (mDownXValue + FLIP_PIXEL_THRESHOLD)) {						

						mViewFlipper.setInAnimation(AnimationManager.getInstance().getInFromLeftAnimation());
						mViewFlipper.setOutAnimation(AnimationManager.getInstance().getOutToRightAnimation());

						mViewFlipper.showPrevious();

						mCurrentWebView = mWebViews.get(mViewFlipper.getDisplayedChild());
						updateUI();
						
						return true;
					}

					// going forwards: pushing stuff to the left
					if (currentX < (mDownXValue - FLIP_PIXEL_THRESHOLD)) {					

						mViewFlipper.setInAnimation(AnimationManager.getInstance().getInFromRightAnimation());
						mViewFlipper.setOutAnimation(AnimationManager.getInstance().getOutToLeftAnimation());

						mViewFlipper.showNext();

						mCurrentWebView = mWebViews.get(mViewFlipper.getDisplayedChild());
						updateUI();
						
						return true;
					}
				}
			}
			break;
		}
		}

        // if you return false, these actions will not be recorded
        return false;

	}
	
	/**
	 * Load the AdSweep script if necessary.
	 * @return The AdSweep script.
	 */
	private String getAdSweepString() {
		if (mAdSweepString == null) {
			InputStream is = getResources().openRawResource(R.raw.adsweep);
			if (is != null) {
				StringBuilder sb = new StringBuilder();
				String line;

				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
					while ((line = reader.readLine()) != null) {
						if ((line.length() > 0) &&
								(!line.startsWith("//"))) {
							sb.append(line).append("\n");
						}
					}
				} catch (IOException e) {
					Log.w("AdSweep", "Unable to load AdSweep: " + e.getMessage());
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						Log.w("AdSweep", "Unable to load AdSweep: " + e.getMessage());
					}
				}
				mAdSweepString = sb.toString();
			} else {        
				mAdSweepString = "";
			}
		}
		return mAdSweepString;
	}
	
	/**
	 * Check if the url is in the AdBlock white list
	 * @param url The url to check
	 * @return true if the url is in the white list
	 */
	private boolean checkInAdBlockWhiteList(String url) {
		boolean inList = false;
		Iterator<String> iter = Controller.getInstance().getAdBlockWhiteList().iterator();
		while ((iter.hasNext()) &&
				(!inList)) {
			if (url.contains(iter.next())) {
				inList = true;
			}
		}
		return inList;
	}
	
	@Override
	public void onWebEvent(String event, Object data) {
		
		if (event.equals(EventConstants.EVT_WEB_ON_PAGE_FINISHED)) {
			
			updateUI();			
						
			if ((Controller.getInstance().getPreferences().getBoolean(Constants.PREFERENCES_ADBLOCKER_ENABLE, true)) &&
					(!checkInAdBlockWhiteList(mCurrentWebView.getUrl()))) {
				mCurrentWebView.loadUrl(getAdSweepString());
			}
			
		} else if (event.equals(EventConstants.EVT_WEB_ON_PAGE_STARTED)) {
			
			mUrlEditText.setText((CharSequence) data);
			
			mPreviousButton.setEnabled(false);
			mNextButton.setEnabled(false);
			
		} else if (event.equals(EventConstants.EVT_WEB_ON_URL_LOADING)) {
			setToolbarsVisibility(true);
		}
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		Bundle b = item.getIntent().getExtras();;
		
		switch(item.getItemId()) {
		case CONTEXT_MENU_OPEN:
			if (b != null) {
				navigateToUrl(b.getString(Constants.EXTRA_ID_URL));
			}			
			return true;
			
		case CONTEXT_MENU_OPEN_IN_NEW_TAB:
			if (b != null) {
				addTab(false);
				navigateToUrl(b.getString(Constants.EXTRA_ID_URL));
			}			
			return true;
		
		case CONTEXT_MENU_DOWNLOAD:
			if (b != null) {
				doDownloadStart(b.getString(Constants.EXTRA_ID_URL), null, null, null, 0);
			}
			return true;
		}
		
		return super.onContextItemSelected(item);		
	}
	
	/**
	 * Hide the tool bars.
	 */
	public void hideToolbars() {
		if (mUrlBarVisible) {			
			if (!mUrlEditText.hasFocus()) {
				setToolbarsVisibility(false);
			}
		}
		mHideToolbarsRunnable = null;
	}

	@Override
	public void onDownloadbEvent(String event, Object data) {
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