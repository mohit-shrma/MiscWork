package com.bandbaaja.db;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GigsDbAdapter extends AbstractDbAdapter {
	
	public static final String KEY_ROWID 		= 	"_id";
	public static final String KEY_VENUE		=	"venue";
	public static final String KEY_ARTIST		=	"artist";
	public static final String KEY_CITY_LOC		=	"city_loc";
	public static final String KEY_CITY			=	"city";
	public static final String KEY_EVENT_URL	=	"event_url";
	public static final String KEY_GENRE		=	"genre";
	public static final String KEY_DESC			=	"description";
	public static final String KEY_BOOKMARKED	=	"bookmarked";
	public static final String KEY_DATE_TIME	=	"date_time";
	public static final String KEY_DAY_DATE		=	"day_date";
	public static final String KEY_LAT_LONG		=	"latlong";
	
	private static final String TAG				=	"BandBaaja-GigsDbAdapter";
	
	private static final String DAY_DATE_FORMAT		=	"EEE, d MMM yyyy, HH:mm";
	
	private SimpleDateFormat simpleDateFormat;
	
	
    private static final String DATABASE_NAME 	= "bandbaaja_data";
    private static final String DATABASE_TABLE 	= "gigs";
    private static final int DATABASE_VERSION 	= 2;
	
    
	/**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public GigsDbAdapter(Context ctx) {
    	super(ctx);
        this.simpleDateFormat 	= new SimpleDateFormat(DAY_DATE_FORMAT);
    }
	
    
    /**
     * Create a new gig using the parameters provided. If the gig is
     * successfully created return the new rowId for that gig, otherwise return
     * a -1 to indicate failure.
     * 
     * @param 
     * @param 
     * @return rowId or -1 if failed
     */
    public long createGig(String venue, String artist,
    					  String city_loc, String city, String latlong,
    					  String event_url, String genre,
    					  String description, boolean bookmarked,
    					  long date_time) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_VENUE, venue);
        initialValues.put(KEY_ARTIST, artist);
        initialValues.put(KEY_CITY_LOC, city_loc);
        initialValues.put(KEY_CITY, city);
        initialValues.put(KEY_LAT_LONG, latlong);
        initialValues.put(KEY_EVENT_URL, event_url);
        initialValues.put(KEY_GENRE, genre);
        initialValues.put(KEY_DESC, description);
        initialValues.put(KEY_BOOKMARKED, bookmarked?1:0);
        initialValues.put(KEY_DATE_TIME, date_time);
        initialValues.put(KEY_DAY_DATE, 
        					simpleDateFormat.format(new Date(date_time)));
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    /**
     * Delete the gig with the given rowId
     * 
     * @param rowId id of gig to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteGig(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    /**
     * Delete all the gigs
     * 
     * @return true if deleted, false otherwise
     */
    public boolean deleteAllGigs() {
    	return mDb.delete(DATABASE_TABLE, "1", null) > 0;
    }
    
    /**
     * Delete older gigs which are past current time
     * 
     * @return true if deleted, false otherwise
     */
    public boolean deleteOlderGigs() {
    	return mDb.delete(DATABASE_TABLE, 
    						KEY_DATE_TIME + "<" + System.currentTimeMillis(), 
    						null) > 0;
    }
    
    
    /**
     * Return a Cursor over the list of all gigs in the database
     * 
     * @return Cursor over all gigs
     */
    public Cursor fetchAllGigs() {

        return mDb.query(DATABASE_TABLE, null, null, null, null, null, null);
    }
    
    
    /**
     * Return a Cursor over the list of all gigs in the database
     * 
     * @return Cursor over all gigs but in brief
     */
    public Cursor fetchAllGigsBrief() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_VENUE, KEY_ARTIST, 
        		KEY_DAY_DATE, KEY_CITY_LOC, KEY_BOOKMARKED}, null, null, null, 
        		null, KEY_DATE_TIME);
    }
    
    public Cursor fetchAllBookmarkedGigsBrief() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_VENUE, KEY_ARTIST, 
        		KEY_DAY_DATE, KEY_CITY_LOC}, KEY_BOOKMARKED + "=1", null, null, 
        		null, KEY_DATE_TIME);
    }
    
	
    /**
     * Return a Cursor positioned at the gig that matches the given rowId
     * 
     * @param rowId id of gig to retrieve
     * @return Cursor positioned to matching gig, if found
     * @throws SQLException if gig could not be found/retrieved
     */
    public Cursor fetchGig(long rowId){

        Cursor mCursor = null;
        try {
        	 mCursor = 
        		mDb.query(true, DATABASE_TABLE, null, KEY_ROWID + "=" + rowId, null,
             						null, null, null, null);
			 if (mCursor != null) {
			     mCursor.moveToFirst();
			 }
        } catch (SQLException e) {
        	Log.e(TAG, "fetchGig::SQLException:"+e.getMessage()); 
        	throw e;
        }
       
        return mCursor;

    }
    
    
    /**
     * Return a Cursor positioned at the gig that matches the given rowId
     * 
     * @param 
     * @return Cursor positioned to matching gig, if found
     * @throws SQLException if gig could not be found/retrieved
     */
    public Cursor fetchGigBrief(long rowId) {
    	
    	Cursor mCursor = null;
        try {
        	mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_VENUE, KEY_ARTIST, 
                		KEY_DAY_DATE, KEY_CITY_LOC, KEY_BOOKMARKED}, 
                		KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
            if (mCursor != null) {
                mCursor.moveToFirst();
            }
        } catch(SQLException e) {
        	Log.e(TAG, "fetchGigBrief::SQLException:"+e.getMessage());
        }
    	
        return mCursor;

    }
    
    
    
    /**
     * @param word to search in db for all gigs present
     * @return Cursor positioned to matching gigs, if found
     * @throws SQLException if gig could not be found/retrieved
     */
    public Cursor searchGigs(String searchWord) {
    	Cursor mCursor = null;
    	try {
    		mCursor = 
        		mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_VENUE, KEY_ARTIST, 
                		KEY_DAY_DATE, KEY_CITY_LOC, KEY_BOOKMARKED}, 
                		KEY_VENUE + " LIKE " +"\'%"+searchWord+"%\'" + " OR " +
                		KEY_ARTIST + " LIKE " +"\'%"+searchWord+"%\'" + " OR " +
                		KEY_CITY_LOC + " LIKE " +"\'%"+searchWord+"%\'" + " OR " +
                		KEY_GENRE + " LIKE " +"\'%"+searchWord+"%\'" + " OR " +
                		KEY_DAY_DATE + " LIKE " +"\'%"+searchWord+"%\'", null,
                        null, null, KEY_DATE_TIME);
    	} catch (SQLException e) {
    		Log.e(TAG, "searchGigs::SQLException:"+e.getMessage());
    	}
    	
		return mCursor;
    }
    
    
    /**
     * @param word to search in db for all gigs present
     * @return Cursor positioned to matching gigs, if found
     * @throws SQLException if gig could not be found/retrieved
     */
    public Cursor searchBookmarks(String searchWord) {
    	Cursor mCursor = null;
    	try {
    		mCursor = 
        		mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_VENUE, KEY_ARTIST, 
                		KEY_DAY_DATE, KEY_CITY_LOC, KEY_BOOKMARKED}, 
                		KEY_BOOKMARKED + "=1 AND " + "(" +
                		KEY_VENUE + " LIKE " +"\'%"+searchWord+"%\'" + " OR " +
                		KEY_ARTIST + " LIKE " +"\'%"+searchWord+"%\'" + " OR " +
                		KEY_CITY_LOC + " LIKE " +"\'%"+searchWord+"%\'" + " OR " +
                		KEY_GENRE + " LIKE " +"\'%"+searchWord+"%\'" + " OR " +
                		KEY_DAY_DATE + " LIKE " +"\'%"+searchWord+"%\'" + ")", null,
                        null, null, KEY_DATE_TIME);
    	} catch (SQLException e) {
    		Log.e(TAG, "searchBookmarks::SQLException:"+e.getMessage());
    	}
    	
    		
		return mCursor;
    }
    
    
    
    
    
    /**
     * Update the gig using the details provided. The gig to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of gig to update
     * @param 
     * @param 
     * @return true if the gig was successfully updated, false otherwise
     */
    public boolean updateGig(long rowId, String venue, String artist,
							  String city_loc, String city, String latlong,
							  String event_url, String genre,
							  String description, boolean bookmarked,
							  long date_time) {
        
    	ContentValues args = new ContentValues();
        
    	args.put(KEY_VENUE, venue);
    	args.put(KEY_ARTIST, artist);
    	args.put(KEY_CITY_LOC, city_loc);
    	args.put(KEY_CITY, city);
    	args.put(KEY_LAT_LONG, latlong);
    	args.put(KEY_EVENT_URL, event_url);
    	args.put(KEY_GENRE, genre);
    	args.put(KEY_DESC, description);
    	args.put(KEY_BOOKMARKED, bookmarked?1:0);
    	args.put(KEY_DATE_TIME, date_time);
    	args.put(KEY_DAY_DATE, simpleDateFormat.format(new Date(date_time)));
    	
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateGigBookmark(long rowId, boolean newBookmarkFlag) {
    	
    	ContentValues args = new ContentValues();
    	args.put(KEY_BOOKMARKED, newBookmarkFlag?1:0);
    	
    	return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    
	
}