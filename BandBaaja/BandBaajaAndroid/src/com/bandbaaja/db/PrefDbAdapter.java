package com.bandbaaja.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PrefDbAdapter extends AbstractDbAdapter {
	
	public static final String KEY_ROWID 				= 	"_id";
	public static final String KEY_CITY					=	"city";
	public static final String KEY_LAST_REFRESH_GIG_ID	=	"last_refresh_gig_id";
	public static final String KEY_TWITT_ACCESS_TOKEN_0 =   "twitt_access_token_0";
	public static final String KEY_TWITT_ACCESS_TOKEN_1 =   "twitt_access_token_1";
	
	private static final String TAG						=	"PrefDbAdapter";
	
	
    private static final String DATABASE_NAME 			= 	"bandbaaja_data";
    private static final String DATABASE_TABLE 			= 	"pref";
    private static final int DATABASE_VERSION 			= 	2;
	
    public static final int VALUE_CITY_BANGALORE		=	0; 
    public static final int VALUE_CITY_MUMBAI			=	1;
    public static final int VALUE_CITY_DELHI			=	2;
    public static final int VALUE_CITY_OTHERS			=	3;
	
	/**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public PrefDbAdapter(Context ctx) {
        super(ctx);
    }    
    
    /**
     * Create a new pref using the parameters provided. If the pref is
     * successfully created return the new rowId for that pref, otherwise return
     * a -1 to indicate failure.
     * 
     * @param 
     * @param 
     * @return rowId or -1 if failed
     */
    public long createPref(String city, String recentGigId, String twittAccess0, 
    						String twittAccess1) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CITY, city);
        initialValues.put(KEY_LAST_REFRESH_GIG_ID, recentGigId);
        initialValues.put(KEY_TWITT_ACCESS_TOKEN_0, twittAccess0);
        initialValues.put(KEY_TWITT_ACCESS_TOKEN_1, twittAccess1);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    /**
     * Create a new pref with null params. If the pref is
     * successfully created return the new rowId for that pref, otherwise return
     * a -1 to indicate failure.
     * 
     * @return rowId or -1 if failed
     */
    public long createAndInitPref() {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CITY, (String)null);
        initialValues.put(KEY_LAST_REFRESH_GIG_ID, (String)null);
        initialValues.put(KEY_TWITT_ACCESS_TOKEN_0, (String)null);
        initialValues.put(KEY_TWITT_ACCESS_TOKEN_1, (String)null);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    
    /**
     * Delete the pref with the given rowId
     * 
     * @param rowId id of pref to delete
     * @return true if deleted, false otherwise
     */
    public boolean deletePref(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    /**
     * Return a Cursor over the list of all pref in the database
     * 
     * @return Cursor over all pref
     */
    public Cursor fetchPref() {

        return mDb.query(DATABASE_TABLE, null, null, null, null, null, null);
    }
    
    /**
     * Update the pref using the details provided. The pref to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of pref to update
     * @param 
     * @param 
     * @return true if the pref was successfully updated, false otherwise
     */
    public boolean updatePref(long rowId, String city,
    							String recentGigId, String twittAccess0, 
							  String twittAccess1) {
    	ContentValues args = new ContentValues();
    	args.put(KEY_CITY, city);
    	args.put(KEY_LAST_REFRESH_GIG_ID, recentGigId);
    	args.put(KEY_TWITT_ACCESS_TOKEN_0, twittAccess0);
    	args.put(KEY_TWITT_ACCESS_TOKEN_1, twittAccess1);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updatePrefTokens(long rowId, String twittAccess0, 
    								String twittAccess1) {
    	ContentValues args = new ContentValues();
    	args.put(KEY_TWITT_ACCESS_TOKEN_0, twittAccess0);
    	args.put(KEY_TWITT_ACCESS_TOKEN_1, twittAccess1);
    	return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateCity(long rowId, String city) {
    	ContentValues args = new ContentValues();
    	args.put(KEY_CITY, city.toLowerCase());
    	return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateLastRefreshGigId(long rowId, String recentGigId) {
    	ContentValues args = new ContentValues();
    	args.put(KEY_LAST_REFRESH_GIG_ID, recentGigId);
    	return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
	public boolean deleteAll() {
		return mDb.delete(DATABASE_TABLE, "1", null) > 0;
	}
    
	
}