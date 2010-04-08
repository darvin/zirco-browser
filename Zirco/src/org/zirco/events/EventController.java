package org.zirco.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventController {
	
	private List<IWebListener> mWebListeners;
	
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
		mWebListeners = new ArrayList<IWebListener>();
	}
	
	public void addWebListener(IWebListener listener) {
		if (!mWebListeners.contains(listener)) {
			mWebListeners.add(listener);
		}
	}
	
	public void removeWebListener(IWebListener listener) {
		mWebListeners.remove(listener);
	}
	
	public void fireWebEvent(String event, Object data) {
		Iterator<IWebListener> iter = mWebListeners.iterator();
		while (iter.hasNext()) {
			iter.next().onWebEvent(event, data);
		}
	}

}
