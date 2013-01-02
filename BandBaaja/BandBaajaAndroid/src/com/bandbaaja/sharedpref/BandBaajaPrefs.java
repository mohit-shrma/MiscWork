package com.bandbaaja.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;



public final class BandBaajaPrefs {

	private final static String PREF_NAME = "BANDBAAJA_PREFS";
	public final static String FIRST_LAUNCH = "IS_FIRST_LAUNCH";
	public final static String PREF_CITY = "CITY";
	public static final String NOTIFICATIONS = "NOTIFICATIONS";
	
	public final static String CITY_BANGALORE = "Bangalore";
	public final static String CITY_MUMBAI = "Mumbai";
	public final static String CITY_DELHI = "Delhi";
	public final static String CITY_OTHERS = "Others";
	
	
	public static SharedPreferences get(Context context) {
		return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}
}