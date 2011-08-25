package org.zirco.sync;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.Date;
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
import org.zirco.utils.Constants;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class WeaveSyncTask extends AsyncTask<WeaveAccountInfo, Integer, Throwable> {

	private static final String WEAVE_PATH = "/storage/bookmarks";
	
	private static final String WEAVE_HEADER_TYPE = "type";
	
	private static final String WEAVE_VALUE_BOOKMARK = "bookmark";
	private static final String WEAVE_VALUE_FOLDER = "folder";
	private static final String WEAVE_VALUE_ITEM = "item";
	private static final String WEAVE_VALUE_ID = "id";
	private static final String WEAVE_VALUE_PARENT_ID = "parentid";
	private static final String WEAVE_VALUE_TITLE = "title";
	private static final String WEAVE_VALUE_URI = "bmkUri";
	private static final String WEAVE_VALUE_DELETED = "deleted";
	
	private static WeaveFactory mWeaveFactory = null;
	
	private static WeaveFactory getWeaveFactory() {
		if (mWeaveFactory == null) {
			mWeaveFactory = new WeaveFactory(true);
		}
		
		return mWeaveFactory;
	}
	
	private Context mContext;
	private ISyncListener mListener;
	private DbAdapter mDbAdapter;
	
	public WeaveSyncTask(Context context, ISyncListener listener) {
		mContext = context;
		mListener = listener;
		mDbAdapter = new DbAdapter(context);
		mDbAdapter.open();
	}
	
	@Override
	protected Throwable doInBackground(WeaveAccountInfo... arg0) {
		Throwable result = null;
		
		SQLiteDatabase db = mDbAdapter.getDatabase();		
		
		try {
			db.beginTransaction();			
			
			WeaveAccountInfo accountInfo = arg0[0];
			UserWeave userWeave = getWeaveFactory().createUserWeave(accountInfo.getServer(), accountInfo.getUsername(), accountInfo.getPassword());						
			
			long lastModifiedDate = getLastModified(userWeave).getTime();			
			long lastSyncDate = PreferenceManager.getDefaultSharedPreferences(mContext).getLong(Constants.PREFERENCE_WEAVE_LAST_SYNC_DATE, -1);
			
			if (lastModifiedDate > lastSyncDate) {		
				
				boolean syncByDelta = lastSyncDate > 0;								
				
				QueryResult<List<WeaveBasicObject>> queryResult;
				
				QueryParams parms = null;
				if (syncByDelta) {
					parms = new QueryParams();
					parms.setFull(false);
					parms.setNewer(new Date(lastSyncDate));
				} else {
					mDbAdapter.clearWeaveBookmarks();
				}
				
				queryResult = getCollection(userWeave, WEAVE_PATH, parms);
				List<WeaveBasicObject> wboList = queryResult.getValue();

				if (syncByDelta) {
					doSyncByDelta(accountInfo, userWeave, wboList, db);
				} else {
					doSync(accountInfo, userWeave, wboList, db);
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
	
	private void doSync(WeaveAccountInfo accountInfo, UserWeave userWeave, List<WeaveBasicObject> wboList, SQLiteDatabase db)
		throws WeaveException, JSONException, IOException, GeneralSecurityException {
		
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
	}
	
	private void doSyncByDelta(WeaveAccountInfo accountInfo, UserWeave userWeave, List<WeaveBasicObject> wboList, SQLiteDatabase db)
		throws WeaveException, JSONException, IOException, GeneralSecurityException {
		
		int i = 0;
		int count = wboList.size();
		
		for (WeaveBasicObject wbo : wboList) {
			JSONObject decryptedPayload = wbo.getEncryptedPayload(userWeave, accountInfo.getSecret());
			
			i++;						
			
			if (decryptedPayload.has(WEAVE_HEADER_TYPE)) {
				
				if (decryptedPayload.getString(WEAVE_HEADER_TYPE).equals(WEAVE_VALUE_ITEM) &&
						decryptedPayload.has(WEAVE_VALUE_DELETED) &&
						decryptedPayload.getBoolean(WEAVE_VALUE_DELETED)) {

					String weaveId = decryptedPayload.has(WEAVE_VALUE_ID) ? decryptedPayload.getString(WEAVE_VALUE_ID) : null;
					if ((weaveId != null) &&
							(weaveId.length() > 0)) {
						mDbAdapter.deleteWeaveBookmarkByWeaveId(weaveId);
					}
				} else if (decryptedPayload.getString(WEAVE_HEADER_TYPE).equals(WEAVE_VALUE_BOOKMARK) ||
						decryptedPayload.getString(WEAVE_HEADER_TYPE).equals(WEAVE_VALUE_FOLDER)) {
					
					String weaveId = decryptedPayload.has(WEAVE_VALUE_ID) ? decryptedPayload.getString(WEAVE_VALUE_ID) : null;
					if ((weaveId != null) &&
							(weaveId.length() > 0)) {
						
						boolean isFolder = decryptedPayload.getString(WEAVE_HEADER_TYPE).equals(WEAVE_VALUE_FOLDER);
						
						String title = decryptedPayload.getString(WEAVE_VALUE_TITLE);
						String parentId = decryptedPayload.has(WEAVE_VALUE_PARENT_ID) ? decryptedPayload.getString(WEAVE_VALUE_PARENT_ID) : null;
						
						ContentValues values = new ContentValues();
						values.put(DbAdapter.WEAVE_BOOKMARKS_WEAVE_ID, weaveId);
						values.put(DbAdapter.WEAVE_BOOKMARKS_WEAVE_PARENT_ID, parentId);
						values.put(DbAdapter.WEAVE_BOOKMARKS_TITLE, title);						
						
						if (isFolder) {
							values.put(DbAdapter.WEAVE_BOOKMARKS_FOLDER, true);
						} else {
							String url = decryptedPayload.getString(WEAVE_VALUE_URI);
							
							values.put(DbAdapter.WEAVE_BOOKMARKS_FOLDER, false);
							values.put(DbAdapter.WEAVE_BOOKMARKS_URL, url);
						}
						
						long id = mDbAdapter.getIdByWeaveId(weaveId);
						if (id == -1) {
							// Insert.
							db.insert(DbAdapter.WEAVE_BOOKMARKS_TABLE, null, values);
						} else {
							// Update.
							db.update(DbAdapter.WEAVE_BOOKMARKS_TABLE, values, DbAdapter.WEAVE_BOOKMARKS_ID + " = " + id, null);
						}						
						
					}
				}
			}
			
			//Log.d("Decrypted:", decryptedPayload.toString());
			
			publishProgress(i, count);

			if (isCancelled()) {
				break;
			}
		}
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
	
	private Date getLastModified(UserWeave userWeave) throws WeaveException {
		try {
			JSONObject infoCol = userWeave.getNode(UserWeave.HashNode.INFO_COLLECTIONS).getValue();

			if (infoCol.has("bookmarks")) {
				long modLong = infoCol.getLong("bookmarks");
				return new Date(modLong * 1000);
			}

			return null;
		} catch (JSONException e) {
			throw new WeaveException(e);
		}
	}

}
