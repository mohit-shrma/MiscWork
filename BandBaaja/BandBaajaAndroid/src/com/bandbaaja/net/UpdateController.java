package com.bandbaaja.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bandbaaja.R;
import com.bandbaaja.db.GigsDbAdapter;
import com.bandbaaja.db.PrefDbAdapter;
import com.bandbaaja.sharedpref.BandBaajaPrefs;
import com.bandbaaja.util.BandBaajaUtil;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/*
 * singleton controller for updating db
 */
public final class UpdateController {
	
	private static UpdateController updateController = new UpdateController();
	
	private GigsDbAdapter mGigsDbHelper;
	private PrefDbAdapter mPrefDbHelper;
	private Cursor mPrefCursor; 
	
	
	private Context lifetimeAppContext;
	
	private boolean isRunning;
	private boolean isCancelled;
	private int updateCount;
	
	
	public enum UpdateResult {
		STOPPED_BY_CALLER,
		INVALID_JSON_RESPONSE,
		ALREADY_RUNNING,
		CONNECTIVITY_ERROR,
		REFRESH_SUCCESS,
		REFRESH_FAILED,
		GIGS_STORED
	}
	
	
	private static final String TAG = "BandBaaja-UpdateController";
	
	//TODO: make this server url to be able to pick from database
	private String SERVER_URL;
	
	
	
	
	private UpdateController() {
		isRunning = false;
		isCancelled = false;
	}
	
	public static UpdateController getInstance() {
		return updateController;
	}
	
	/*
	 * 
	 * sample json request
	    {
	      "city":"bangalore",
	      "recentGig":"id_of_last_recent_gig_sent_by_server" 
	    }
	 */
	
	private String buildJSONRequest() {
		//TODO: build a valid json request
		String simpleJsonReq = "";
		if (mPrefDbHelper != null && mPrefCursor != null) {
			mPrefCursor.moveToFirst();
			
			String prefCity = mPrefCursor.getString(
					mPrefCursor.getColumnIndexOrThrow(PrefDbAdapter.KEY_CITY));
			String lastRefreshGigId = mPrefCursor.getString(
					mPrefCursor.getColumnIndexOrThrow(PrefDbAdapter.KEY_LAST_REFRESH_GIG_ID));
			simpleJsonReq = "{\"city\":\"" + prefCity +"\",\"recentGig\":\""
			   				 + ((lastRefreshGigId != null && 
			   					lastRefreshGigId.length() > 0)?lastRefreshGigId:"")
			   			     + "\"}";
		}
		//Log.i(TAG, "buildJsonRequest:"+simpleJsonReq);
		return simpleJsonReq;
	}
	
	private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

	
	
	//get gigs data by sending POST request to url
	private String getGigsData() {
		
		//TODO: pick up url from DB 
		String url = SERVER_URL;
		
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			HttpResponse response;
			
			
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("request", buildJSONRequest()));
			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
			
			//execute the request
			response = httpClient.execute(httpPost);
			
			int statusCode = response.getStatusLine().getStatusCode();
			
			if (statusCode != HttpStatus.SC_OK) {
				Log.e(TAG, "getGigsData::response not OK:"+statusCode);
				return null;
			}
			
			HttpEntity httpEntity = response.getEntity();
			
			if (httpEntity != null) {
				//simple string response read
				InputStream istream = httpEntity.getContent();
				String result = convertStreamToString(istream);
				istream.close();
				//Log.i(TAG, "getGigsData::response:"+result);
				return result;
			}
			
			
		} catch (ClientProtocolException e) {
			Log.e(TAG, "getGigsData::ClientProtocolException:"
									+e.getMessage());
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "getGigsData::UnsupportedEncodingException:"
									+e.getMessage());
			
		} catch (IOException e) {
			Log.e(TAG, "getGigsData::IOException:"
									+e.getMessage());
		}
		
		return null;
	}
	
	//put into db the received json data
	/*
	 * 
	 * sample json format
	 * {	"city": "bangalore", 
	 * 		"recentGigId": 41, 
	 * 		"gigs": [
	 * 			      {"event_url": "", "description": "", "date_time": "2011-05-13 01:15",  "bookmarked": "False", "genre": "rock", "city": "bangalore", "artist": "iron amma2", "venue": "palace ground", "city_loc": "palace"}, 
	 * 				  {"event_url": "", "description": "", "date_time": "2011-05-23 23:44",  "bookmarked": "False", "genre": "rock", "city": "bangalore", "artist": "newart", "venue": "bomnm", "city_loc": "bangalore"}, 
	 * 				  {"event_url": "", "description": "", "date_time": "2011-05-26 11:08",  "bookmarked": "False", "genre": "sdkodm", "city": "bangalore", "artist": "ssd", "venue": "sdfsd", "city_loc": "odxks"},  
	 * 				  {"event_url": "", "description": "", "date_time": "2011-05-24 11:08",  "bookmarked": "False", "genre": "sdad",  "city": "bangalore", "artist": "asd", "venue": "dsada", "city_loc": "23"}
	 * 				]
	 * }
	 * 
	 * 
	 */
	private Set<UpdateResult> putGigsData(JSONObject jsonObj) {
		
		if (mGigsDbHelper != null && mPrefDbHelper != null 
			&& mPrefCursor != null) {
		
			mPrefCursor.moveToFirst();
			
			try {
				
				int rowId = mPrefCursor.getInt(
								mPrefCursor.getColumnIndexOrThrow(PrefDbAdapter.KEY_ROWID));
				
				
				//check city if same as requested
				String prefCity = mPrefCursor.getString(
						mPrefCursor.getColumnIndexOrThrow(PrefDbAdapter.KEY_CITY));
				
				String jsonCity = jsonObj.getString("city");
				
				if (!(prefCity.compareToIgnoreCase(jsonCity) == 0)) {
					Log.e(TAG, "json request city dont match");
					return EnumSet.of(UpdateResult.REFRESH_FAILED);
				}
				
				//put recentGigId under application pref if recentgigid len > 0
				String recentGigId = jsonObj.getString("recentGigId");
				if (recentGigId != null && recentGigId.length() > 0) {
					mPrefDbHelper.updateLastRefreshGigId(rowId, recentGigId);
				}
				
				
				
				// store the sent gigs(jsonformat) in db
				JSONArray gigs = jsonObj.getJSONArray("gigs");
				JSONObject gigObj;
				
				//Log.i(TAG, "gigs count to store: "+gigs.length());
				
				for (int i = gigs.length()-1; i >= 0; i--) {
					gigObj = gigs.getJSONObject(i);
					
					//insert this gigobject in db
					insertGigData(gigObj);
				}
				
				//remove old gigs
				mGigsDbHelper.deleteOlderGigs();
				
				return gigs.length()>0 ? 
						EnumSet.of(UpdateResult.REFRESH_SUCCESS, UpdateResult.GIGS_STORED) :
						EnumSet.of(UpdateResult.REFRESH_SUCCESS);
			
			} catch (JSONException e) {
				Log.e(TAG, "putGigsData:invalid json request");
			}
		}
		
		return EnumSet.of(UpdateResult.REFRESH_FAILED);
	}
	
	private void removeOlderGigs() {
		//remove older gigs in nite
		//add sound if current hr >08 <22
		Calendar cal = Calendar.getInstance();
		int currentHr = cal.get(Calendar.HOUR_OF_DAY);
		
		if ((currentHr >= 0 && currentHr <= 7)) {
			//Log.d(TAG, "deleting older gigs");
			mGigsDbHelper.deleteOlderGigs();
		}
		
	}
	
	private void insertGigData(JSONObject jsonGig) {
		try {
			String venue		=	jsonGig.getString("venue")
										.replace("&#39;", "'")
										.replace("&amp;", "&");
			String artist		=	jsonGig.getString("artist")
										.replace("&#39;", "'")
										.replace("&amp;", "&");
			String cityLoc		=	jsonGig.getString("city_loc")
										.replace("&#39;", "'")
										.replace("&amp;", "&");
			String city			=	jsonGig.getString("city")
										.replace("&#39;", "'")
										.replace("&amp;", "&");
			String eventUrl		=	jsonGig.getString("event_url");
			String genre		=	jsonGig.getString("genre")
										.replace("&#39;", "'")
										.replace("&amp;", "&");
			String description	=	jsonGig.getString("description")
										.replace("&#39;", "'")
										.replace("&amp;", "&");
			String latlong  	= 	jsonGig.getString("latlong")
										.replace("&#39;", "'")
										.replace("&amp;", "&");

			boolean bookmarked	=	jsonGig.getBoolean("bookmarked");
			
			//need to convert date_time into long(ms)
			String date_time	=	jsonGig.getString("date_time");
			long dateTime		=	getDateinMS(date_time);
			
			mGigsDbHelper.createGig(venue, artist, cityLoc, city, latlong, eventUrl, 
					genre, description, bookmarked, dateTime);
			updateCount++;
			
		} catch (JSONException e) {
			Log.e(TAG, "insertGigsData: "+e.getMessage());
		}
	}
	
	private long getDateinMS(String dateTime) {
		SimpleDateFormat simplDtFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date tempDt;
		try {
			tempDt = simplDtFmt.parse(dateTime);
			return tempDt.getTime();
		} catch (ParseException e) {
			Log.e(TAG, "getDateinMS: "+e.getMessage());
		}
		return -1;
	}
	
	
	public boolean isActive() {
		return isRunning;
	}
	
	public void cancel() {
		isCancelled = true;
	}
	
	public int getUpdateCount() {
		return updateCount;
	}
	
	private boolean resetStorage() {
		
		if (mGigsDbHelper != null && mPrefDbHelper != null 
				&& mPrefCursor != null) {
			mPrefCursor.moveToFirst();
			
			int rowId = mPrefCursor.getInt(
					mPrefCursor.getColumnIndexOrThrow(PrefDbAdapter.KEY_ROWID));
			mGigsDbHelper.deleteAllGigs();
			mPrefDbHelper.updateLastRefreshGigId(rowId, "");
			return true;
		}
		
		return false;
	}
	
	private boolean handleDeletionResponse(String jsonGigs) {
		
		try {
			JSONObject jsonObj = new JSONObject(jsonGigs);
			boolean jsonIsDelete = jsonObj.getBoolean("isdelete");
			if (jsonIsDelete) {
				if (!resetStorage()) {
					//TODO: custom app exception
					Log.e(TAG, "checkDeletionAndRefresh:storage cant be reset");
					return false;
				} else {
					//Log.i(TAG, "checkDeletionAndRefresh:storage was reset");
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "handledeletionresponse:jsonexception"+e.getMessage());
		}
		//Log.i(TAG, "handledeletionresponse:success");
		return true;
	}
	
	//refresh = get + put
	//return Errorcodes
	public synchronized Set refreshData(Context context) {
		
		String jsonGigs;
		Set<UpdateResult> isPutSuccess;
		Set<UpdateResult> retVal = EnumSet.of(UpdateResult.REFRESH_FAILED);
		SERVER_URL = context.getString(R.string.server_url) + "/get/gigs";
		if (!isRunning) {
			
			try {
				isRunning = true;
				if (BandBaajaUtil.isconnected(context)) {
					//TODO: review this dangerous block playing with context cud cause leaks
					lifetimeAppContext = context;
					updateCount = 0;
					mGigsDbHelper = new GigsDbAdapter(lifetimeAppContext);
					mGigsDbHelper.open();
					
					mPrefDbHelper = new PrefDbAdapter(lifetimeAppContext);
					mPrefDbHelper.open();
					
					mPrefCursor = mPrefDbHelper.fetchPref();
					
					//TODO: what if mprefcursor is null or no settings is stored
					if (isCancelled) {
						//Log.d(TAG, "refreshData::stopped by caller");
						return EnumSet.of(UpdateResult.STOPPED_BY_CALLER);
					}
					
					jsonGigs = getGigsData();
					
					if (jsonGigs == null || jsonGigs.length() == 0) {
						//invalid json
						Log.e(TAG, "refreshData::invalid json response: "+jsonGigs);
						return EnumSet.of(UpdateResult.INVALID_JSON_RESPONSE);
					}
					
					//commenting as let it update db, if already n/w request has occured
					/*
					if (isCancelled) {
						//Log.d(TAG, "refreshData::stopped by caller");
						return EnumSet.of(UpdateResult.STOPPED_BY_CALLER);
					}*/
					
					//check if deletion response present and handled successfully
					if (handleDeletionResponse(jsonGigs)) {
						isPutSuccess = putGigsData(new JSONObject(jsonGigs));
						if (isPutSuccess.contains(UpdateResult.REFRESH_SUCCESS)) {
							retVal = isPutSuccess;
						}
					}
					
				} else {
					retVal = EnumSet.of(UpdateResult.CONNECTIVITY_ERROR);
					//Log.i(TAG, "CONNECTIVITY_ERROR");
				}
			} catch (JSONException e) {
				Log.e(TAG, "refreshData::JSONException:" 
										+ e.getMessage());
			} catch (Exception e) {
				Log.e(TAG, "refreshData::Exception:" 
										+ e.getMessage());
			} finally {
				
				isRunning = false;
				isCancelled = false;
				
				if (mGigsDbHelper != null) {
					mGigsDbHelper.close();
				}
				
				if (mPrefDbHelper != null) {
					mPrefDbHelper.close();
				}
				
				lifetimeAppContext = null;
				
				if (mPrefCursor != null) {
					mPrefCursor.deactivate();
					mPrefCursor.close();
				}
			}
		} else {
			retVal = EnumSet.of(UpdateResult.ALREADY_RUNNING);
		}
		//Log.i(TAG, "retval: "+retVal);
		return retVal;
	}
	
} 