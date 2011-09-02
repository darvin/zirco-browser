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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zirco.R;
import org.zirco.model.DbAdapter;
import org.zirco.model.adapters.BookmarksCursorAdapter;
import org.zirco.model.items.BookmarkItem;
import org.zirco.providers.BookmarksProviderWrapper;
import org.zirco.utils.ApplicationUtils;
import org.zirco.utils.BookmarksUtils;
import org.zirco.utils.Constants;
import org.zirco.utils.DateUtils;
import org.zirco.utils.IOUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.provider.Browser.BookmarkColumns;
import android.util.Log;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Bookmarks list activity.
 */
public class BookmarksListActivity extends Activity {
			
	private static final int MENU_ADD_BOOKMARK = Menu.FIRST;
	private static final int MENU_SORT_MODE = Menu.FIRST + 1;	
	private static final int MENU_IMPORT_BOOKMARKS = Menu.FIRST + 2;
	private static final int MENU_EXPORT_BOOKMARKS = Menu.FIRST + 3;
	private static final int MENU_CLEAR_BOOKMARKS = Menu.FIRST + 4;
	
	private static final int MENU_OPEN_IN_TAB = Menu.FIRST + 10;    
    private static final int MENU_COPY_URL = Menu.FIRST + 11;
    private static final int MENU_SHARE = Menu.FIRST + 12;
    private static final int MENU_EDIT_BOOKMARK = Menu.FIRST + 13;
    private static final int MENU_DELETE_BOOKMARK = Menu.FIRST + 14;
    
    private static final int ACTIVITY_ADD_BOOKMARK = 0;
    private static final int ACTIVITY_EDIT_BOOKMARK = 1;    
	
	//private DbAdapter mDbAdapter;
	
	private Cursor mCursor;
	private BookmarksCursorAdapter mCursorAdapter;
	
	private ListView mList;
	
	private ProgressDialog mProgressDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmarkslistactivity);
        
        setTitle(R.string.BookmarksListActivity_Title);
        
        View emptyView = findViewById(R.id.BookmarksListActivity_EmptyTextView);
        mList = (ListView) findViewById(R.id.BookmarksListActivity_List);
        
        mList.setEmptyView(emptyView);
        
        mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				Intent result = new Intent();
		        result.putExtra(Constants.EXTRA_ID_NEW_TAB, false);
		        
		        BookmarkItem item = BookmarksProviderWrapper.getStockBookmarkById(getContentResolver(), id);
		        if (item != null) {
		        	result.putExtra(Constants.EXTRA_ID_URL,  item.getUrl());
		        } else {
		        	result.putExtra(Constants.EXTRA_ID_URL,
		        			PreferenceManager.getDefaultSharedPreferences(BookmarksListActivity.this).getString(Constants.PREFERENCES_GENERAL_HOME_PAGE, Constants.URL_ABOUT_START));
		        }
		        //result.putExtra(Constants.EXTRA_ID_URL,  mDbAdapter.getBookmarkById(id)[1]);
		        
		        if (getParent() != null) {
		        	getParent().setResult(RESULT_OK, result);
		        } else {
		        	setResult(RESULT_OK, result);
		        }
		        
		        finish();
			}
		});
        
        //mDbAdapter = new DbAdapter(this);
        //mDbAdapter.open();

        registerForContextMenu(mList);
        
        fillData();
    }
    
    @Override
	protected void onDestroy() {
		//mDbAdapter.close();
		mCursor.close();
		super.onDestroy();
	}

    /**
     * Fill the bookmark to the list UI. 
     */
	private void fillData() {
    	//mCursor = mDbAdapter.fetchBookmarks();
		mCursor = BookmarksProviderWrapper.getStockBookmarks(getContentResolver(),
				PreferenceManager.getDefaultSharedPreferences(this).getInt(Constants.PREFERENCES_BOOKMARKS_SORT_MODE, 0));
    	startManagingCursor(mCursor);
    	
    	//String[] from = new String[] {DbAdapter.BOOKMARKS_TITLE, DbAdapter.BOOKMARKS_URL};
    	String[] from = new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL};
    	int[] to = new int[] {R.id.BookmarkRow_Title, R.id.BookmarkRow_Url};
    	
    	mCursorAdapter = new BookmarksCursorAdapter(this, R.layout.bookmarkrow, mCursor, from, to);
        mList.setAdapter(mCursorAdapter);
        
        setAnimation();
    }
    
	/**
	 * Set the list loading animation.
	 */
    private void setAnimation() {
    	AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(100);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(100);
        set.addAnimation(animation);

        LayoutAnimationController controller =
                new LayoutAnimationController(set, 0.5f);

        mList.setLayoutAnimation(controller);
    }
    
    /**
     * Display the add bookmark dialog.
     */
    private void openAddBookmarkDialog() {
		Intent i = new Intent(this, EditBookmarkActivity.class);
		
		i.putExtra(Constants.EXTRA_ID_BOOKMARK_ID, (long) -1);
		i.putExtra(Constants.EXTRA_ID_BOOKMARK_TITLE, "");
		i.putExtra(Constants.EXTRA_ID_BOOKMARK_URL, "");
		
		startActivityForResult(i, ACTIVITY_ADD_BOOKMARK);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem item;
    	item = menu.add(0, MENU_ADD_BOOKMARK, 0, R.string.BookmarksListActivity_MenuAddBookmark);
        item.setIcon(R.drawable.ic_menu_add_bookmark);
    	
    	item = menu.add(0, MENU_SORT_MODE, 0, R.string.BookmarksListActivity_MenuSortMode);
        item.setIcon(R.drawable.ic_menu_sort);    	    	
        
        item = menu.add(0, MENU_IMPORT_BOOKMARKS, 0, R.string.BookmarksListActivity_ImportBookmarks);
        item.setIcon(R.drawable.ic_menu_import);
        
        item = menu.add(0, MENU_EXPORT_BOOKMARKS, 0, R.string.BookmarksListActivity_ExportBookmarks);
        item.setIcon(R.drawable.ic_menu_export);
        
        item = menu.add(0, MENU_CLEAR_BOOKMARKS, 0, R.string.BookmarksListActivity_ClearBookmarks);
        item.setIcon(R.drawable.ic_menu_delete);
    	
    	return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	
    	switch(item.getItemId()) {
    	case MENU_SORT_MODE:
    		changeSortMode();
    		return true;
    		
    	case MENU_ADD_BOOKMARK:    		
    		openAddBookmarkDialog();
            return true;
            
        case MENU_IMPORT_BOOKMARKS:
        	importBookmarks();
            return true;
            
        case MENU_EXPORT_BOOKMARKS:
        	exportBookmarks();
        	return true;
            
        case MENU_CLEAR_BOOKMARKS:
        	clearBookmarks();
        	return true;
        default: return super.onMenuItemSelected(featureId, item);
    	}
    }
    
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		long id = ((AdapterContextMenuInfo) menuInfo).id;
		if (id != -1) {
			BookmarkItem item = BookmarksProviderWrapper.getStockBookmarkById(getContentResolver(), id);
			if (item != null) {
				menu.setHeaderTitle(item.getTitle());
			}
			//menu.setHeaderTitle(mDbAdapter.getBookmarkById(id)[0]);
		}
		
		menu.add(0, MENU_OPEN_IN_TAB, 0, R.string.BookmarksListActivity_MenuOpenInTab);        
        menu.add(0, MENU_COPY_URL, 0, R.string.BookmarksHistoryActivity_MenuCopyLinkUrl);
        menu.add(0, MENU_SHARE, 0, R.string.Main_MenuShareLinkUrl);
        menu.add(0, MENU_EDIT_BOOKMARK, 0, R.string.BookmarksListActivity_MenuEditBookmark);
        menu.add(0, MENU_DELETE_BOOKMARK, 0, R.string.BookmarksListActivity_MenuDeleteBookmark);
    }
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	    	
    	Intent i;
    	BookmarkItem bookmarkItem = BookmarksProviderWrapper.getStockBookmarkById(getContentResolver(), info.id);
    	
    	switch (item.getItemId()) {
    	case MENU_OPEN_IN_TAB:    	
            i = new Intent();
            i.putExtra(Constants.EXTRA_ID_NEW_TAB, true);
            //i.putExtra(Constants.EXTRA_ID_URL, mDbAdapter.getBookmarkById(info.id)[1]);
            
	        if (bookmarkItem != null) {
	        	i.putExtra(Constants.EXTRA_ID_URL,  bookmarkItem.getUrl());
	        } else {
	        	i.putExtra(Constants.EXTRA_ID_URL,
	        			PreferenceManager.getDefaultSharedPreferences(BookmarksListActivity.this).getString(Constants.PREFERENCES_GENERAL_HOME_PAGE, Constants.URL_ABOUT_START));
	        }
            
            if (getParent() != null) {
            	getParent().setResult(RESULT_OK, i);
            } else {
            	setResult(RESULT_OK, i);            
            }
            
            finish();
            return true;
            
    	case MENU_EDIT_BOOKMARK:    		
    		if (bookmarkItem != null) {
    			i = new Intent(this, EditBookmarkActivity.class);
    			i.putExtra(Constants.EXTRA_ID_BOOKMARK_ID, info.id);
    			//i.putExtra(Constants.EXTRA_ID_BOOKMARK_TITLE, mDbAdapter.getBookmarkById(info.id)[0]);
    			//i.putExtra(Constants.EXTRA_ID_BOOKMARK_URL, mDbAdapter.getBookmarkById(info.id)[1]);
    			i.putExtra(Constants.EXTRA_ID_BOOKMARK_TITLE, bookmarkItem.getTitle());
    			i.putExtra(Constants.EXTRA_ID_BOOKMARK_URL, bookmarkItem.getUrl());

    			startActivityForResult(i, ACTIVITY_EDIT_BOOKMARK);
    		}
            return true;
            
    	case MENU_COPY_URL:
    		if (bookmarkItem != null) {
    			ApplicationUtils.copyTextToClipboard(this,  bookmarkItem.getUrl(), getString(R.string.Commons_UrlCopyToastMessage));
    		}
    		return true;
    		
    	case MENU_SHARE:
    		if (bookmarkItem != null) {
    			ApplicationUtils.sharePage(this, bookmarkItem.getTitle(), bookmarkItem.getUrl());
    		}
    		return true;
    		
    	case MENU_DELETE_BOOKMARK:
    		//mDbAdapter.deleteBookmark(info.id);
    		BookmarksProviderWrapper.deleteStockBookmark(getContentResolver(), info.id);
    		fillData();
    		return true;
    	default: return super.onContextItemSelected(item);
    	}
    }
    
    /**
     * Change list sort mode. Update list.
     * @param sortMode The new sort mode.
     */
    private void doChangeSortMode(int sortMode) {
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
    	editor.putInt(Constants.PREFERENCES_BOOKMARKS_SORT_MODE, sortMode);
    	editor.commit();
    	
    	fillData();
    }
    
    /**
     * Show a dialog for choosing the sort mode.
     * Perform the change if required.
     */
    private void changeSortMode() {
    	
    	int currentSort = PreferenceManager.getDefaultSharedPreferences(this).getInt(Constants.PREFERENCES_BOOKMARKS_SORT_MODE, 0);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setInverseBackgroundForced(true);
    	builder.setIcon(android.R.drawable.ic_dialog_info);
    	builder.setTitle(getResources().getString(R.string.BookmarksListActivity_MenuSortMode));
    	builder.setSingleChoiceItems(new String[] {getResources().getString(R.string.BookmarksListActivity_MostUsedSortMode),
    			getResources().getString(R.string.BookmarksListActivity_AlphaSortMode),
    			getResources().getString(R.string.BookmarksListActivity_RecentSortMode) },
    			currentSort,
    			new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				doChangeSortMode(which);
				dialog.dismiss();				
			}    		
    	}); 
    	builder.setCancelable(true);
    	builder.setNegativeButton(R.string.Commons_Cancel, null);
    	
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    /**
     * Perform the bookmarks import.
     * @param file The file to import. If null, will import Android bookmarks.
     */
    private void doImportBookmarks(String file) {
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.BookmarksListActivity_ImportingBookmarks));
    	
    	if (file == null) {
    		new AndroidBookmarksImporter(this);
    	} else {
    		new XmlBookmarksImporter(this, file);
    	}
    	
    }
    
    /**
     * Perform the bookmarks import.
     */
    private void importBookmarks() {
    	
    	List<String> exportedFiles = IOUtils.getExportedBookmarksFileList();
    	
    	Collections.sort(exportedFiles);
    	
    	final String[] choices = new String[exportedFiles.size() + 1];
    	
    	choices[0] = this.getResources().getString(R.string.BookmarksListActivity_AndroidImportSource);
    	
    	int i = 1;
    	for (String fileName : exportedFiles) {
    		choices[i] = fileName;
    		i++;
    	}
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setInverseBackgroundForced(true);
    	builder.setIcon(android.R.drawable.ic_dialog_info);
    	builder.setTitle(getResources().getString(R.string.BookmarksListActivity_ImportSource));
    	builder.setSingleChoiceItems(choices,
    			0,
    			new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				if (which == 0) {
					doImportBookmarks(null);
				} else {
					doImportBookmarks(choices[which]);
				}
				
				dialog.dismiss();				
			}    		
    	});    	
    	
    	builder.setCancelable(true);
    	builder.setNegativeButton(R.string.Commons_Cancel, null);
    	
    	AlertDialog alert = builder.create();
    	alert.show();
    	
    }
    
    /**
     * Perform the bookmarks export.
     * @param fileName The export file name.
     */
    private void doExportBookmarks(String fileName) {
    	
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.BookmarksListActivity_ExportingBookmarks));

    	if (fileName != null) {
    		new XmlBookmarksExporter(this, fileName);
    	} else {
    		new AndroidBookmarksExporter(this);
    	}
    }
    
    /**
     * Display a confirmation dialog and perform the bookmarks export.
     */
    private void exportBookmarks() {
    	
    	final String fileName = DateUtils.getNowForFileName() + ".xml";
    	
    	final String[] exportTargets;
    	final String[] exportTargetsDisplay;
    	
    	if (ApplicationUtils.checkCardState(this, false)) {
    		exportTargets = new String[2];
    		exportTargets[0] = null;
    		exportTargets[1] = fileName;
    		
    		exportTargetsDisplay = new String[2];
    		exportTargetsDisplay[0] = getResources().getString(R.string.BookmarksListActivity_AndroidExportTarget);
    		exportTargetsDisplay[1] = getResources().getString(R.string.BookmarksListActivity_SDCardExportTarget);
    	} else {
    		exportTargets = new String[1];
    		exportTargets[0] = null;
    		
    		exportTargetsDisplay = new String[1];
    		exportTargetsDisplay[0] = getResources().getString(R.string.BookmarksListActivity_AndroidExportTarget);
    	}
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setInverseBackgroundForced(true);
    	builder.setIcon(android.R.drawable.ic_dialog_info);
    	builder.setTitle(getResources().getString(R.string.BookmarksListActivity_ExportDialogTitle));
    	
    	builder.setSingleChoiceItems(exportTargetsDisplay,
    			0,
    			new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				if (which == 0) {
					doExportBookmarks(null);
				} else {
					doExportBookmarks(exportTargets[which]);
				}
				
				dialog.dismiss();				
			}    		
    	});    	
    	
    	builder.setCancelable(true);
    	builder.setNegativeButton(R.string.Commons_Cancel, null);
    	
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    /**
     * Clear all the bookmarks.
     */
    private void doClearBookmarks() {
    	mProgressDialog = ProgressDialog.show(this,
    			this.getResources().getString(R.string.Commons_PleaseWait),
    			this.getResources().getString(R.string.BookmarksListActivity_ClearingBookmarks));
    	
    	new BookmarksCleaner();
    }
    
    /**
     * Show a confirmation dialog for bookmarks clearing.
     * Perform the clear if required.
     */
    private void clearBookmarks() {
    	ApplicationUtils.showYesNoDialog(this,
				android.R.drawable.ic_dialog_alert,
				R.string.BookmarksListActivity_ClearBookmarks,
				R.string.Commons_NoUndoMessage,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						doClearBookmarks();
					}			
		}); 
    }
    
    /**
     * Runnable for import of Android bookmarks.
     */
    private class AndroidBookmarksImporter implements Runnable {
    	
    	private Context mContext;

    	/**
    	 * Constructor.
    	 * @param context The current context.
    	 */
    	public AndroidBookmarksImporter(Context context) {
    		mContext = context;
    		
    		new Thread(this).start();
    	}
    	
		@Override
		public void run() {
			Cursor cursor = BookmarksUtils.getAllAndroidBookmarks(mContext);
	    	startManagingCursor(cursor);
	    	
	    	if (cursor != null) {
	    		if (cursor.moveToFirst()) {
	    			
	    			String title;
	    			String url;
	    			
	    			do {
	    				
	    				title = cursor.getString(cursor.getColumnIndex(BookmarkColumns.TITLE));
	    				url = cursor.getString(cursor.getColumnIndex(BookmarkColumns.URL));
	    				
	    				// TODO: implement this
	    				//mDbAdapter.addBookmark(title, url);
	    				
	    			} while (cursor.moveToNext());	    				    			
	    		}
	    	}
	    	
	    	cursor.close();
	    	
	    	handler.sendEmptyMessage(0);
		}
		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				mProgressDialog.dismiss();
				fillData();
			}
		};
    }
    
    /**
     * Runnable for bookmarks import from xml.
     */
    private class XmlBookmarksImporter implements Runnable {

    	private Context mContext;
    	private String mFileName;
    	
    	/**
    	 * Constructor.
    	 * @param context The current context.
    	 * @param fileName The file to import.
    	 */
    	public XmlBookmarksImporter(Context context, String fileName) {
    		mContext = context;
    		mFileName = fileName;
    		
    		new Thread(this).start();
    	}
    	
    	/**
    	 * Get the content of a node, why Android does not include Node.getTextContent() ?
    	 * @param node The node.
    	 * @return The node content.
    	 */
    	private String getNodeContent(Node node) {
    		StringBuffer buffer = new StringBuffer();
    		NodeList childList = node.getChildNodes();
    		for (int i = 0; i < childList.getLength(); i++) {
    		    Node child = childList.item(i);
    		    if (child.getNodeType() != Node.TEXT_NODE) {
    		        continue; // skip non-text nodes
    		    }
    		    buffer.append(child.getNodeValue());
    		}

    		return buffer.toString(); 
    	}
    	
		@Override
		public void run() {
			
			File file = new File(IOUtils.getBookmarksExportFolder(), mFileName);
			
			if ((file != null) &&
					(file.exists()) &&
					(file.canRead())) {
				
				try {
					
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();				
					DocumentBuilder builder = factory.newDocumentBuilder();
					
					Document document = builder.parse(file);
					
					Element root = document.getDocumentElement();
					
					if ((root != null) &&
							(root.getNodeName().equals("bookmarkslist"))) {
						
						NodeList bookmarksList = root.getElementsByTagName("bookmark");
						
						Node bookmark;
						NodeList bookmarkItems;
						String title;
						String url;
						String creationDate;
						int count;
						Node item;
						
						for (int i = 0; i < bookmarksList.getLength(); i++) {
							
							bookmark = bookmarksList.item(i);
							
							if (bookmark != null) {
								
								title = null;
								url = null;
								creationDate = null;
								count = 0;
								
								bookmarkItems = bookmark.getChildNodes();
								
								for (int j = 0; j < bookmarkItems.getLength(); j++) {
									
									item = bookmarkItems.item(j);
									
									if ((item != null) &&
											(item.getNodeName() != null)) {
										if (item.getNodeName().equals("title")) {
											title = getNodeContent(item);										
										} else if (item.getNodeName().equals("url")) {
											url = URLDecoder.decode(getNodeContent(item));
										} else if (item.getNodeName().equals("creationdate")) {
											creationDate = getNodeContent(item);
										} else if (item.getNodeName().equals("count")) {
											try {
												count = Integer.parseInt(getNodeContent(item));
											} catch (Exception e) {
												count = 0;
											}
										}
									}
									
								}
								
								if ((creationDate == null) ||
										(creationDate.length() == 0)) {
									creationDate = DateUtils.getNow(mContext);
								}
								
								// TODO: implement this.
								//mDbAdapter.addBookmark(title, url, creationDate, count);								
							}
							
						}
						
					} else {
						Log.i("Bookmark import", "Empty or invalid file.");
					}
					
				} catch (ParserConfigurationException e) {
					Log.w("Bookmark import failed", e.getMessage());
				} catch (SAXException e) {
					Log.w("Bookmark import failed", e.getMessage());
				} catch (IOException e) {
					Log.w("Bookmark import failed", e.getMessage());
				}
				
			}
			
			handler.sendEmptyMessage(0);			
		}
		
		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				mProgressDialog.dismiss();
				fillData();								
			}
		};
    	
    }
    
    /**
     * Runnable to export bookmarks to Android bookmarks.
     */
    private class AndroidBookmarksExporter implements Runnable {

    	private Context mContext;
    	
    	/**
    	 * Constructor.
    	 * @param context The current context.
    	 */
    	public AndroidBookmarksExporter(Context context) {
    		mContext = context;
    		new Thread(this).start();
    	}
    	
		@Override
		public void run() {
			
			if (mCursor.moveToFirst()) {
				
				String title;
				String url;
				
				do {
					
					title = mCursor.getString(mCursor.getColumnIndex(DbAdapter.BOOKMARKS_TITLE));
					url = mCursor.getString(mCursor.getColumnIndex(DbAdapter.BOOKMARKS_URL));
					
					BookmarksUtils.saveAndroidBookmark(mContext, title, url);
					
				} while (mCursor.moveToNext());			
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
     * Runnable for bookmarks export to xml.
     */
    private class XmlBookmarksExporter implements Runnable {
    	    	
    	private Context mContext;
    	private String mFileName;
    	private File mFile;
    	private String mErrorMessage = null;
    	
    	/**
    	 * Constructor.
    	 * @param context The current context.
    	 * @param fileName The export file name.
    	 */
    	public XmlBookmarksExporter(Context context, String fileName) {
    		mContext = context;
    		mFileName = fileName;
    		new Thread(this).start();
    	}
    	
		@Override
		public void run() {
				
			try {

				mFile = new File(IOUtils.getBookmarksExportFolder(), mFileName);				
				FileWriter writer = new FileWriter(mFile);

				writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				writer.write("<bookmarkslist>\n");

				if (mCursor.moveToFirst()) {

					do {

						writer.write("<bookmark>\n");

						writer.write(String.format("<title>%s</title>\n", mCursor.getString(mCursor.getColumnIndex(DbAdapter.BOOKMARKS_TITLE))));
						writer.write(String.format("<url>%s</url>\n", URLEncoder.encode(mCursor.getString(mCursor.getColumnIndex(DbAdapter.BOOKMARKS_URL)))));
						writer.write(String.format("<creationdate>%s</creationdate>\n", mCursor.getString(mCursor.getColumnIndex(DbAdapter.BOOKMARKS_CREATION_DATE))));
						writer.write(String.format("<count>%s</count>\n", mCursor.getInt(mCursor.getColumnIndex(DbAdapter.BOOKMARKS_COUNT))));

						writer.write("</bookmark>\n");

					} while (mCursor.moveToNext());

				}

				writer.write("</bookmarkslist>\n");

				writer.flush();
				writer.close();

			} catch (IOException e1) {
				Log.w("Bookmark export failed", e1.toString());				
				mErrorMessage = e1.toString();
			}							
			
			handler.sendEmptyMessage(0);
		}
		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				mProgressDialog.dismiss();
				
				if (mErrorMessage == null) {
					ApplicationUtils.showOkDialog(mContext,
							android.R.drawable.ic_dialog_info,
							mContext.getResources().getString(R.string.BookmarksListActivity_BookmarksExportSDCardDoneTitle),
							String.format(mContext.getResources().getString(R.string.BookmarksListActivity_BookmarksExportSDCardDoneMessage), mFile.getAbsolutePath()));
				} else {
					ApplicationUtils.showOkDialog(mContext,
							android.R.drawable.ic_dialog_alert,
							mContext.getResources().getString(R.string.BookmarksListActivity_BookmarksExportSDCardFailedTitle),
							String.format(mContext.getResources().getString(R.string.BookmarksListActivity_BookmarksExportSDCardFailedMessage), mErrorMessage));
				}
			}
		};
    	
    }
    
    /**
     * Runnable for bookmark clearing.
     */
    private class BookmarksCleaner implements Runnable {
    	
    	/**
    	 * Constructor.
    	 */
    	public BookmarksCleaner() {	
    		new Thread(this).start();
    	}
    	
		@Override
		public void run() {
			// TODO: check this
			//mDbAdapter.clearBookmarks();
	    	handler.sendEmptyMessage(0);
		}
		
		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				mProgressDialog.dismiss();
				fillData();
			}
		};
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        switch (requestCode) {
		case ACTIVITY_EDIT_BOOKMARK:
			if (resultCode == RESULT_OK) {
				fillData();
			}
			break;
		case ACTIVITY_ADD_BOOKMARK:
			if (resultCode == RESULT_OK) {
				fillData();
			}
			break;
			
		default:
			break;
		}
    }

}
