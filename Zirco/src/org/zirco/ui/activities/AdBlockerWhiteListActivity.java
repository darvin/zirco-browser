package org.zirco.ui.activities;

import org.zirco.R;
import org.zirco.controllers.Controller;
import org.zirco.utils.ApplicationUtils;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AdBlockerWhiteListActivity extends ListActivity {
	
	private static final int MENU_ADD = Menu.FIRST;
	private static final int MENU_CLEAR = Menu.FIRST + 1;
	
	private static final int MENU_DELETE = Menu.FIRST + 10;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adblockerwhitelistactivity);
        
        setTitle(R.string.AdBlockerWhiteListActivity_Title);
        
        registerForContextMenu(getListView());
        
        fillData();
	}
	
	private void fillData() {
				
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.adblockerwhitelistrow, R.id.AdBlockerWhiteListRow_Title, Controller.getInstance().getAdBlockWhiteList());
		setListAdapter(adapter);
		
		setAnimation();
	}
	
	private void setAnimation() {
    	AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(100);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(100);
        set.addAnimation(animation);

        LayoutAnimationController controller =
                new LayoutAnimationController(set, 0.5f);
        ListView listView = getListView();        
        listView.setLayoutAnimation(controller);
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		int position = ((AdapterContextMenuInfo) menuInfo).position;
		if (position != -1) {
			menu.setHeaderTitle(Controller.getInstance().getAdBlockWhiteList().get(position));
		}
		
		menu.add(0, MENU_DELETE, 0, R.string.AdBlockerWhiteListActivity_DeleteMenu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	
    	switch (item.getItemId()) {
    	case MENU_DELETE:
    		Controller.getInstance().getAdBlockWhiteList().remove(info.position);
    		Controller.getInstance().saveAdBlockWhiteList();
    		fillData();
    		return true;
    	}
    	
    	return super.onContextItemSelected(item);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem item;
    	item = menu.add(0, MENU_ADD, 0, R.string.AdBlockerWhiteListActivity_Add);
        item.setIcon(R.drawable.add32);
    	
    	item = menu.add(0, MENU_CLEAR, 0, R.string.AdBlockerWhiteListActivity_Clear);
        item.setIcon(R.drawable.clear32);
        
        return true;
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	
    	switch(item.getItemId()) {
    	case MENU_ADD:
    		addToWhiteList();
    		return true;
    		
    	case MENU_CLEAR:    		
    		clearWhiteList();
            return true;           
    	}
    	
    	return super.onMenuItemSelected(featureId, item);
    }
	
	private void doAddToWhiteList(String value) {
		Controller.getInstance().getAdBlockWhiteList().add(value);
		Controller.getInstance().saveAdBlockWhiteList();
		fillData();
	}
	
	private void addToWhiteList() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);    
    	builder.setCancelable(true);
    	builder.setIcon(android.R.drawable.ic_input_add);
    	builder.setTitle(getResources().getString(R.string.AdBlockerWhiteListActivity_AddMessage));
    	
    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	builder.setView(input);
    	
    	builder.setInverseBackgroundForced(true);
    	builder.setPositiveButton(getResources().getString(R.string.Commons_Ok), new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			dialog.dismiss();
    			doAddToWhiteList(input.getText().toString());
    		}
    	});
    	builder.setNegativeButton(getResources().getString(R.string.Commons_Cancel), new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			dialog.dismiss();
    		}
    	});
    	AlertDialog alert = builder.create();
    	alert.show();

	}
	
	private void doClearWhiteList() {
		Controller.getInstance().getAdBlockWhiteList().clear();
		Controller.getInstance().saveAdBlockWhiteList();
		fillData();
	}
	
	private void clearWhiteList() {
		ApplicationUtils.showYesNoDialog(this,
				android.R.drawable.ic_dialog_alert,
				R.string.AdBlockerWhiteListActivity_ClearMessage,
				R.string.Commons_NoUndoMessage,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						doClearWhiteList();
					}			
		});      
    }

}
