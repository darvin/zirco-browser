package org.zirco.sync;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.List;

import org.emergent.android.weave.client.QueryParams;
import org.emergent.android.weave.client.QueryResult;
import org.emergent.android.weave.client.UserWeave;
import org.emergent.android.weave.client.WeaveAccountInfo;
import org.emergent.android.weave.client.WeaveBasicObject;
import org.emergent.android.weave.client.WeaveException;
import org.emergent.android.weave.client.WeaveFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.zirco.model.DbAdapter;

import android.content.Context;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class WeaveSyncTask extends AsyncTask<WeaveAccountInfo, Integer, Throwable> {

	private static final String WEAVE_PATH = "/storage/bookmarks";
	
	private static final String WEAVE_HEADER_TYPE = "type";
	
	private static final String WEAVE_VALUE_BOOKMARK = "bookmark";
	private static final String WEAVE_VALUE_FOLDER = "folder";
	private static final String WEAVE_VALUE_ID = "id";
	private static final String WEAVE_VALUE_PARENT_ID = "parentid";
	private static final String WEAVE_VALUE_TITLE = "title";
	private static final String WEAVE_VALUE_URI = "bmkUri";
	
	private static WeaveFactory mWeaveFactory = null;
	
	private static WeaveFactory getWeaveFactory() {
		if (mWeaveFactory == null) {
			mWeaveFactory = new WeaveFactory(true);
		}
		
		return mWeaveFactory;
	}
	
	private ISyncListener mListener;
	private DbAdapter mDbAdapter;
	
	public WeaveSyncTask(Context context, ISyncListener listener) {
		mListener = listener;
		mDbAdapter = new DbAdapter(context);
		mDbAdapter.open();
	}
	
	@Override
	protected Throwable doInBackground(WeaveAccountInfo... arg0) {
		Throwable result = null;
		
		SQLiteDatabase db = mDbAdapter.getDatabase();
		
		mDbAdapter.clearWeaveBookmarks();
		
		try {
			db.beginTransaction();			
			
			WeaveAccountInfo accountInfo = arg0[0];
			UserWeave userWeave = getWeaveFactory().createUserWeave(accountInfo.getServer(), accountInfo.getUsername(), accountInfo.getPassword());
			
			QueryResult<List<WeaveBasicObject>> queryResult;
			queryResult = getCollection(userWeave, WEAVE_PATH, null);
			List<WeaveBasicObject> wboList = queryResult.getValue();
			
			int i = 0;
			int count = wboList.size();						
			
			InsertHelper ih = new InsertHelper(db, DbAdapter.WEAVE_BOOKMARKS_TABLE);
	    	
	    	int titleColumn = ih.getColumnIndex(DbAdapter.WEAVE_BOOKMARKS_TITLE);
	    	int urlColumn = ih.getColumnIndex(DbAdapter.WEAVE_BOOKMARKS_URL);
	    	int isFolderColumn = ih.getColumnIndex(DbAdapter.WEAVE_BOOKMARKS_FOLDER);
	    	int weaveIdColumn = ih.getColumnIndex(DbAdapter.WEAVE_BOOKMARKS_WEAVE_ID);
	    	int parentWeaveIdColumn = ih.getColumnIndex(DbAdapter.WEAVE_BOOKMARKS_WEAVE_PARENT_ID);
			
			for (WeaveBasicObject wbo : wboList) {
    			JSONObject decryptedPayload = wbo.getEncryptedPayload(userWeave, accountInfo.getSecret());
    			
    			i++;
    			
    			//Log.d("Decrypted:", decryptedPayload.toString());
    			
    			if (decryptedPayload.has(WEAVE_HEADER_TYPE) &&
    					((decryptedPayload.getString(WEAVE_HEADER_TYPE).equals(WEAVE_VALUE_BOOKMARK)) ||
    							(decryptedPayload.getString(WEAVE_HEADER_TYPE).equals(WEAVE_VALUE_FOLDER)))) {
    				
    				if (decryptedPayload.has(WEAVE_VALUE_TITLE)) {
    					
    					boolean isFolder = decryptedPayload.getString(WEAVE_HEADER_TYPE).equals(WEAVE_VALUE_FOLDER);
    					
    					String title = decryptedPayload.getString(WEAVE_VALUE_TITLE);    					
    					String weaveId = decryptedPayload.has(WEAVE_VALUE_ID) ? decryptedPayload.getString(WEAVE_VALUE_ID) : null;
    					String parentId = decryptedPayload.has(WEAVE_VALUE_PARENT_ID) ? decryptedPayload.getString(WEAVE_VALUE_PARENT_ID) : null;
    					
    					if ((title != null) && (title.length() > 0)) {
    						
    						ih.prepareForInsert();
    						
    						ih.bind(titleColumn, title);
    						ih.bind(weaveIdColumn, weaveId);
    						ih.bind(parentWeaveIdColumn, parentId);
    						
    						if (isFolder) {
    							ih.bind(isFolderColumn, true);
    						} else {
    							String url = decryptedPayload.getString(WEAVE_VALUE_URI);
    							
    							ih.bind(isFolderColumn, false);
    							ih.bind(urlColumn, url);
    						}
    						
    						ih.execute();    						
    					}
    				}
    			}
    			
    			publishProgress(i, count);

				if (isCancelled()) {
					break;
				}
    		}
			
			db.setTransactionSuccessful();
			
		} catch (WeaveException e) {
			e.printStackTrace();
			result = e;
		} catch (JSONException e) {
			e.printStackTrace();
			result = e;
		} catch (IOException e) {
			e.printStackTrace();
			result = e;
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			result = e;
		} finally {
			db.endTransaction();
		}
		
		return result;
	}
	
	@Override
	protected void onCancelled() {
		mDbAdapter.close();
		mListener.onSyncCancelled();
		super.onCancelled();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		mListener.onSyncProgress(values[0], values[1]);
	}

	@Override
	protected void onPostExecute(Throwable result) {
		mDbAdapter.close();
		mListener.onSyncEnd(result);
	}

	private QueryResult<List<WeaveBasicObject>> getCollection(UserWeave weave, String name, QueryParams params) throws WeaveException {
    	if (params == null)
    		params = new QueryParams();
    	URI uri = weave.buildSyncUriFromSubpath(name + params.toQueryString());
    	return weave.getWboCollection(uri);
    }

}
