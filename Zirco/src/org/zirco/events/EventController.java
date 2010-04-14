package org.zirco.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zirco.ui.IDownloadEventsListener;

public class EventController {
	
	private List<IWebEventListener> mWebListeners;
	
	private List<IDownloadEventsListener> mDownloadListeners;
	
	/**
	 * Holder for singleton implementation.
	 */
	private static class EventControllerHolder {
		private static final EventController INSTANCE = new EventController();
	}
	
	/**
	 * Get the unique instance of the Controller.
	 * @return The instance of the Controller
	 */
	public static EventController getInstance() {
		return EventControllerHolder.INSTANCE;
	}
	
	private EventController() {
		mWebListeners = new ArrayList<IWebEventListener>();
		mDownloadListeners = new ArrayList<IDownloadEventsListener>();
	}
	
	public void addWebListener(IWebEventListener listener) {
		if (!mWebListeners.contains(listener)) {
			mWebListeners.add(listener);
		}
	}
	
	public void removeWebListener(IWebEventListener listener) {
		mWebListeners.remove(listener);
	}
	
	public void fireWebEvent(String event, Object data) {
		Iterator<IWebEventListener> iter = mWebListeners.iterator();
		while (iter.hasNext()) {
			iter.next().onWebEvent(event, data);
		}
	}
	
	public void addDownloadListener(IDownloadEventsListener listener) {
		if (!mDownloadListeners.contains(listener)) {
			mDownloadListeners.add(listener);
		}
	}
	
	public void removeDownloadListener(IDownloadEventsListener listener) {
		mDownloadListeners.remove(listener);
	}
	
	public void fireDownloadEvent(String event, Object data) {
		Iterator<IDownloadEventsListener> iter = mDownloadListeners.iterator();
		while (iter.hasNext()) {
			iter.next().onDownloadbEvent(event, data);
		}
	}

}
