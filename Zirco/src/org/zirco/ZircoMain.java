package org.zirco;

import org.zirco.events.EventConstants;
import org.zirco.events.EventController;
import org.zirco.events.IWebListener;
import org.zirco.ui.HideToolbarsRunnable;
import org.zirco.ui.IToolbarsContainer;
import org.zirco.utils.AnimationManager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

public class ZircoMain extends Activity implements IWebListener, IToolbarsContainer, OnTouchListener {
	
	private static final int ANIMATION_DURATION = 100;
	
	private static final int FLIP_THRESHOLD = 75;
	
	private static final int NB_TAB = 2;
	
		
	private float mDownXValue;
	
	private LinearLayout mTopBar;
	private LinearLayout mBottomBar;
	
	private EditText mUrlEditText;
	private ImageButton mGoButton;
	
	private ImageView mBubleView;
	
	private WebView[] mWebViewTab;
	
	private ImageButton mPreviousButton;
	private ImageButton mNextButton;
	
	private boolean mUrlBarVisible;
	
	private ViewFlipper mViewFlipper;
	
	private int mCurrentTabIndex;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setProgressBarVisibility(true);
        
        setContentView(R.layout.main);
        
        buildComponents();
        
        mCurrentTabIndex = 0;
        
        EventController.getInstance().addWebListener(this);
        
        mWebViewTab[0].loadUrl("http://fr.m.wikipedia.org");
    }
    
    private void buildComponents() {
    	
    	mUrlBarVisible = true;
    	
    	mBubleView = (ImageView) findViewById(R.id.BubleView);
    	mBubleView.setOnClickListener(new View.OnClickListener() {		
			public void onClick(View v) {
				setToolbarsVisibility(true);				
			}
		});
    	
    	mViewFlipper = (ViewFlipper) findViewById(R.id.ViewFlipper);
    	
    	mTopBar = (LinearLayout) findViewById(R.id.BarLayout);    	
    	mBottomBar = (LinearLayout) findViewById(R.id.BottomBarLayout);
    	    	
    	mUrlEditText = (EditText) findViewById(R.id.UrlText);
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
    	
    	mWebViewTab = new WebView[NB_TAB];
    	mWebViewTab[0] = (WebView) findViewById(R.id.webview0);
    	mWebViewTab[1] = (WebView) findViewById(R.id.webview1);
    	
    	for (int i = 0; i < NB_TAB; i++) {
    		
    		mWebViewTab[i].setWebViewClient(new ZircoWebViewClient());
    		mWebViewTab[i].getSettings().setJavaScriptEnabled(true);
    		mWebViewTab[i].setOnTouchListener((OnTouchListener) this);
    		
    		final Activity activity = this;
    		mWebViewTab[i].setWebChromeClient(new WebChromeClient() {
    			public void onProgressChanged(WebView view, int progress) {
    				// Activities and WebViews measure progress with different scales.
    				// The progress meter will automatically disappear when we reach 100%
    				activity.setProgress(progress * 100);
    			}
    		});
    		    		    		    		
    	}
    	
    }
    
    private void setToolbarsVisibility(boolean visible) {
    	
    	TranslateAnimation animTop = null;
    	TranslateAnimation animBottom = null;
    	
    	if (visible) {
    		
    		mTopBar.setVisibility(View.VISIBLE);
    		mBottomBar.setVisibility(View.VISIBLE);
    		
    		mBubleView.setVisibility(View.GONE);
    		    		    		    		
    		animTop = new TranslateAnimation(
    	            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
    	            Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
    	        );
    		
    		animBottom = new TranslateAnimation(
    	            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
    	            Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f
    	        );


    		animTop.setDuration(ANIMATION_DURATION);
    		animTop.setInterpolator(new AccelerateInterpolator(1.0f));
    		
    		animBottom.setDuration(ANIMATION_DURATION);
    		animBottom.setInterpolator(new AccelerateInterpolator(1.0f));
    		
    		mTopBar.startAnimation(animTop);
    		mBottomBar.startAnimation(animBottom);
    		
    		new Thread(new HideToolbarsRunnable(this)).start();
    		
    		mUrlBarVisible = true;    		    		
    		
    	} else {  	
    		
    		animTop = new TranslateAnimation(
    	            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
    	            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f
    	        );
    		
    		animBottom = new TranslateAnimation(
    	            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
    	            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f
    	        );

    		animTop.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					mTopBar.setVisibility(View.GONE);					
				}

				@Override
				public void onAnimationRepeat(Animation animation) { }

				@Override
				public void onAnimationStart(Animation animation) {	}
    			
    		});
    		
    		animBottom.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					mBottomBar.setVisibility(View.GONE);
					mBubleView.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) { }

				@Override
				public void onAnimationStart(Animation animation) {	}
    			
    		});

    		animTop.setDuration(ANIMATION_DURATION);
    		animTop.setInterpolator(new AccelerateInterpolator(1.0f));
    		
    		animBottom.setDuration(ANIMATION_DURATION);
    		animBottom.setInterpolator(new AccelerateInterpolator(1.0f));
    		
    		mTopBar.startAnimation(animTop);
    		mBottomBar.startAnimation(animBottom);
    		    		
    		mUrlBarVisible = false;
    	}
    }
    
    private void hideKeyboard() {
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(mUrlEditText.getWindowToken(), 0);
    }
    
    private void navigateToUrl() {
    	hideKeyboard();
    	mWebViewTab[mCurrentTabIndex].loadUrl(mUrlEditText.getText().toString());
    }
    
    private void navigatePrevious() {
    	hideKeyboard();
    	mWebViewTab[mCurrentTabIndex].goBack();
    }
    
    private void navigateNext() {
    	hideKeyboard();
    	mWebViewTab[mCurrentTabIndex].goForward();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			setToolbarsVisibility(!mUrlBarVisible);
		}
		
		return super.onKeyDown(keyCode, event);
	}

	public void updateTitle() {
    	
    	String value = mWebViewTab[mCurrentTabIndex].getTitle();
    	
    	if ((value != null) &&
    			(value.length() > 0)) {    	
    		this.setTitle(String.format(getResources().getString(R.string.app_name_url), value));    		
    	} else {
    		this.setTitle(getResources().getString(R.string.app_name));
    	}
    }
	
	public void updateUI() {
		mUrlEditText.setText(mWebViewTab[mCurrentTabIndex].getUrl());
		
		mPreviousButton.setEnabled(mWebViewTab[mCurrentTabIndex].canGoBack());
		mNextButton.setEnabled(mWebViewTab[mCurrentTabIndex].canGoForward());
		
		updateTitle();
	}
	
	@Override
	public void onWebEvent(String event, Object data) {
		
		if (event.equals(EventConstants.EVT_WEB_ON_PAGE_FINISHED)) {
			
			updateUI();
			
			setToolbarsVisibility(false);
			
		} else if (event.equals(EventConstants.EVT_WEB_ON_PAGE_STARTED)) {
			
			mUrlEditText.setText((CharSequence) data);
			
			mPreviousButton.setEnabled(false);
			mNextButton.setEnabled(false);
			
			setToolbarsVisibility(true);
		}
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		hideKeyboard();
		
		// Get the action that was done on this touch event
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN: {
			// store the X value when the user's finger was pressed down
			mDownXValue = event.getX();
			break;
		}

		case MotionEvent.ACTION_UP: {
			// Get the X value when the user released his/her finger
			float currentX = event.getX();            

			// going backwards: pushing stuff to the right
			if ((currentX > (mDownXValue + FLIP_THRESHOLD)) &&
					(mCurrentTabIndex > 0)) {

				mViewFlipper.setInAnimation(AnimationManager.getInstance().getInFromLeftAnimation());
				mViewFlipper.setOutAnimation(AnimationManager.getInstance().getOutToRightAnimation());
				
				mViewFlipper.showPrevious();

				mCurrentTabIndex--;
				
				updateUI();
			}

			// going forwards: pushing stuff to the left
			if ((currentX < (mDownXValue - FLIP_THRESHOLD)) &&
					(mCurrentTabIndex < NB_TAB - 1)) {
				
				mViewFlipper.setInAnimation(AnimationManager.getInstance().getInFromRightAnimation());
				mViewFlipper.setOutAnimation(AnimationManager.getInstance().getOutToLeftAnimation());
				
				mViewFlipper.showNext();

				mCurrentTabIndex++;
				
				updateUI();
			}
			break;
		}
		}

        // if you return false, these actions will not be recorded
        return false;

	}

	@Override
	public void hideToolbars() {
		setToolbarsVisibility(false);		
	}
}