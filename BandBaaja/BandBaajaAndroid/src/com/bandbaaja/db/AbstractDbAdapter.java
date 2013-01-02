package com.bandbaaja.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class AbstractDbAdapter {

	protected static final String TAG = "BandBaajaDbAdapter";
	protected DatabaseHelper mDbHelper;
	protected SQLiteDatabase mDb;
	
	protected static final String CREATE_TABLE_GIGS   = 
					"create table gigs (_id integer primary key autoincrement," 
						+"venue TEXT not null, "
				    	+"artist TEXT not null, "
				    	+"city_loc TEXT not null, "
				    	+"city TEXT not null, "
				    	+"latlong TEXT not null, "
				    	+"event_url TEXT not null, "
				    	+"genre TEXT not null, "
				    	+"description TEXT, "
				    	+"bookmarked INTEGER DEFAULT 0, "
				        +"date_time INTEGER not null," 
				        +"day_date TEXT not null);";

	protected static final String CREATE_TABLE_PREFS  = 
					"create table pref (_id integer primary key autoincrement," 
						+"city TEXT, "
				        +"last_refresh_gig_id TEXT," 
				        +"twitt_access_token_0 TEXT,"
				        +"twitt_access_token_1 TEXT"
				        +");";
	
	protected static final String DATABASE_NAME = "bandbaaja_data";
	protected static final int DATABASE_VERSION = 2;
	protected final Context mCtx;
	
	protected static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE_GIGS);
			db.execSQL(CREATE_TABLE_PREFS);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS gigs");
            db.execSQL("DROP TABLE IF EXISTS pref");
            onCreate(db);
		}
		
	}
	
	
	public AbstractDbAdapter(Context ctx) {
		this.mCtx	=	ctx;
	}
	
    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */

	
	public AbstractDbAdapter open() throws SQLException{
		mDbHelper	=	new DatabaseHelper(mCtx);
		mDb			=	mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		mDbHelper.close();
	}
	
	
}