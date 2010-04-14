package org.zirco.controllers;

import java.util.ArrayList;
import java.util.List;

import org.zirco.model.DownloadItem;

import android.content.SharedPreferences;

public class Controller {
	
	private SharedPreferences mPreferences;

	private List<DownloadItem> mDownloadList;
	
	/**
	 * Holder for singleton implementation.
	 */
	private static class ControllerHolder {
		private static final Controller INSTANCE = new Controller();
	}
	
	/**
	 * Get the unique instance of the Controller.
	 * @return The instance of the Controller
	 */
	public static Controller getInstance() {
		return ControllerHolder.INSTANCE;
	}
	
	private Controller() {
		mDownloadList = new ArrayList<DownloadItem>();
	}

	public SharedPreferences getPreferences() {
		return mPreferences;
	}

	public void setPreferences(SharedPreferences mPreferences) {
		this.mPreferences = mPreferences;
	}
	
	public List<DownloadItem> getDownloadList() {
		return mDownloadList;
	}
	
	public void addToDownload(DownloadItem item) {
		mDownloadList.add(item);
	}
	
}
