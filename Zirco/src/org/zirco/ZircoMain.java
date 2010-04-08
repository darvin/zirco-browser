package org.zirco;

import org.zirco.events.EventConstants;
import org.zirco.events.EventController;
import org.zirco.events.IWebListener;
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
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

public class ZircoMain extends Activity implements IWebListener, OnTouchListener {
	
	private static final int ANIMATION_DURATION = 100;
	
	private static final int FLIP_THRESHOLD = 75;
	
	private static final int NB_TAB = 2;
	
		
	private float mDownXValue;
	
	private LinearLayout[] mTopBarTab;
	private LinearLayout[] mBottomBarTab;
	
	private EditText[] mUrlEditTextTab;
	private ImageButton[] mGoButtonTab;
	private WebView[] mWebViewTab;
	
	private ImageButton[] mPreviousButtonTab;
	private ImageButton[] mNextButtonTab;
	
	private boolean mUrlBarVisibleTab[];
	
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
    	
    	mUrlBarVisibleTab = new boolean[NB_TAB];
    	
    	mViewFlipper = (ViewFlipper) findViewById(R.id.ViewFlipper);
    	
    	mTopBarTab = new LinearLayout[NB_TAB];
    	mTopBarTab[0] = (LinearLayout) findViewById(R.id.BarLayout0);
    	mTopBarTab[1] = (LinearLayout) findViewById(R.id.BarLayout1);
    	
    	mBottomBarTab = new LinearLayout[NB_TAB];
    	mBottomBarTab[0] = (LinearLayout) findViewById(R.id.BottomBarLayout0);
    	mBottomBarTab[1] = (LinearLayout) findViewById(R.id.BottomBarLayout1);
    	
    	mUrlEditTextTab = new EditText[NB_TAB];    	
    	mUrlEditTextTab[0] = (EditText) findViewById(R.id.UrlText0);
    	mUrlEditTextTab[1] = (EditText) findViewById(R.id.UrlText1);
    	
    	mGoButtonTab = new ImageButton[NB_TAB];
    	mGoButtonTab[0] = (ImageButton) findViewById(R.id.GoBtn0);
    	mGoButtonTab[1] = (ImageButton) findViewById(R.id.GoBtn1);
    	
    	mPreviousButtonTab = new ImageButton[NB_TAB];
    	mPreviousButtonTab[0] = (ImageButton) findViewById(R.id.PreviousBtn0);
    	mPreviousButtonTab[1] = (ImageButton) findViewById(R.id.PreviousBtn1);
    	
    	mNextButtonTab = new ImageButton[NB_TAB];
    	mNextButtonTab[0] = (ImageButton) findViewById(R.id.NextBtn0);
    	mNextButtonTab[1] = (ImageButton) findViewById(R.id.NextBtn1);
    	
    	mWebViewTab = new WebView[NB_TAB];
    	mWebViewTab[0] = (WebView) findViewById(R.id.webview0);
    	mWebViewTab[1] = (WebView) findViewById(R.id.webview1);
    	
    	for (int i = 0; i < NB_TAB; i++) {
    		
    		mUrlBarVisibleTab[i] = true;
    		
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

    		
    		mGoButtonTab[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                	navigateToUrl();
                }          
            });
    		
    		mPreviousButtonTab[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                	navigatePrevious();
                }          
            });
    		
    		mNextButtonTab[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                	navigateNext();
                }          
            });
    	}
    	
    }
    
    private void setUrlBarVisibility(boolean visible) {
    	
    	TranslateAnimation animTop = null;
    	TranslateAnimation animBottom = null;
    	
    	if (visible) {
    		/*
    		mUrlEditTextTab[mCurrentTabIndex].setVisibility(View.VISIBLE);
    		mGoButtonTab[mCurrentTabIndex].setVisibility(View.VISIBLE);
    		
    		mPreviousButtonTab[mCurrentTabIndex].setVisibility(View.VISIBLE);
    		mNextButtonTab[mCurrentTabIndex].setVisibility(View.VISIBLE);
    		*/
    		
    		mTopBarTab[mCurrentTabIndex].setVisibility(View.VISIBLE);
    		mBottomBarTab[mCurrentTabIndex].setVisibility(View.VISIBLE);
    		    		    		    		
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
    		
    		mTopBarTab[mCurrentTabIndex].startAnimation(animTop);
    		mBottomBarTab[mCurrentTabIndex].startAnimation(animBottom);
    		
    		mUrlBarVisibleTab[mCurrentTabIndex] = true;
    		
    	} else {
    		/*
    		mUrlEditTextTab[mCurrentTabIndex].setVisibility(View.GONE);
    		mGoButtonTab[mCurrentTabIndex].setVisibility(View.GONE);
    		
    		mPreviousButtonTab[mCurrentTabIndex].setVisibility(View.GONE);
    		mNextButtonTab[mCurrentTabIndex].setVisibility(View.GONE);
    		*/
    		
    		//mTopBarTab[mCurrentTabIndex].setVisibility(View.GONE);
    		//mBottomBarTab[mCurrentTabIndex].setVisibility(View.GONE);
    		
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
					mTopBarTab[mCurrentTabIndex].setVisibility(View.GONE);					
				}

				@Override
				public void onAnimationRepeat(Animation animation) { }

				@Override
				public void onAnimationStart(Animation animation) {	}
    			
    		});
    		
    		animBottom.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					mBottomBarTab[mCurrentTabIndex].setVisibility(View.GONE);					
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
    		
    		mTopBarTab[mCurrentTabIndex].startAnimation(animTop);
    		mBottomBarTab[mCurrentTabIndex].startAnimation(animBottom);
    		    		
    		mUrlBarVisibleTab[mCurrentTabIndex] = false;
    	}
    }
    
    private void hideKeyboard() {
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(mUrlEditTextTab[mCurrentTabIndex].getWindowToken(), 0);
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
    
    private void navigateToUrl() {
    	hideKeyboard();
    	mWebViewTab[mCurrentTabIndex].loadUrl(mUrlEditTextTab[mCurrentTabIndex].getText().toString());
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
			setUrlBarVisibility(!mUrlBarVisibleTab[mCurrentTabIndex]);
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onWebEvent(String event, Object data) {
		
		if (event.equals(EventConstants.EVT_WEB_ON_PAGE_FINISHED)) {
			
			mUrlEditTextTab[mCurrentTabIndex].setText((CharSequence) data);
						
			mPreviousButtonTab[mCurrentTabIndex].setEnabled(mWebViewTab[mCurrentTabIndex].canGoBack());
			mNextButtonTab[mCurrentTabIndex].setEnabled(mWebViewTab[mCurrentTabIndex].canGoForward());
			
			updateTitle();
			
			setUrlBarVisibility(false);
		} else if (event.equals(EventConstants.EVT_WEB_ON_PAGE_STARTED)) {
			
			mUrlEditTextTab[mCurrentTabIndex].setText((CharSequence) data);
			
			mPreviousButtonTab[mCurrentTabIndex].setEnabled(false);
			mNextButtonTab[mCurrentTabIndex].setEnabled(false);
			
			setUrlBarVisibility(true);
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
				
				updateTitle();
			}

			// going forwards: pushing stuff to the left
			if ((currentX < (mDownXValue - FLIP_THRESHOLD)) &&
					(mCurrentTabIndex < NB_TAB - 1)) {
				
				mViewFlipper.setInAnimation(AnimationManager.getInstance().getInFromRightAnimation());
				mViewFlipper.setOutAnimation(AnimationManager.getInstance().getOutToLeftAnimation());
				
				mViewFlipper.showNext();

				mCurrentTabIndex++;
				
				updateTitle();
			}
			break;
		}
		}

        // if you return false, these actions will not be recorded
        return false;

	}
}