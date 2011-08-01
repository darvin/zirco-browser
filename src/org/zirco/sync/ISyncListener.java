package org.zirco.sync;

public interface ISyncListener {
	
	void onSyncProgress(int done, int total);
	
	void onSyncEnd(Throwable result);
	
	void onSyncCancelled();

}
