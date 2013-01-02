package com.bandbaaja.util;

import com.bandbaaja.R;
import com.bandbaaja.SceneMap;
import com.bandbaaja.sharedpref.BandBaajaPrefs;
import com.bandbaaja.update.AlarmReceiver;
import com.google.ads.AdRequest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

public final class BandBaajaUtil {
	
	private static final String TAG = "BandBaaja-BandBaajaUtil";
	
	private BandBaajaUtil() {};
	
	public static boolean isconnected(Context applicationContext) {
		
		ConnectivityManager cm = (ConnectivityManager) applicationContext.
									getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNW = cm.getActiveNetworkInfo();
		boolean isConnected = false;
		if (activeNW != null) {
			 isConnected = activeNW.isConnectedOrConnecting();
		}
		return isConnected;
	}
	
	public static void launchMap(Context activityContext, String latlong, String label) {
		Intent intent = new Intent(activityContext, SceneMap.class);
		intent.putExtra("latlong", latlong);
		activityContext.startActivity(intent);
	}
	
	public static void launchMap(Context activityContext, String query) {
		//Log.d(TAG, "launchMap::"+query);
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
				Uri.parse("geo:0,0?q="+query));
		activityContext.startActivity(intent);
	}
	
	public static void launchWeb(Context activityContext,
									String url) {
		//Log.d(TAG, "launchWeb::"+url);
		Intent in = new Intent();
		in.setAction(Intent.ACTION_VIEW);
		in.addCategory(Intent.CATEGORY_BROWSABLE);
		in.setData(Uri.parse(url));
		activityContext.startActivity(in);
	}
	
	public static void setUpdateAlarm(Context applicationContext) {
		//Log.d(TAG, "setUpdateAlarm");
		int wake 	= AlarmManager.ELAPSED_REALTIME_WAKEUP;
		long bestInt = AlarmManager.INTERVAL_HALF_DAY/5;
		//long bestInt = (AlarmManager.INTERVAL_FIFTEEN_MINUTES/15) * 2;
		long trigger = SystemClock.elapsedRealtime() + bestInt;
		Intent i 			= new Intent(applicationContext, AlarmReceiver.class);
		PendingIntent pi 	= PendingIntent.getBroadcast(applicationContext, 0, i, 0);
		AlarmManager am = (AlarmManager)applicationContext.getSystemService(Context.ALARM_SERVICE);
		am.setInexactRepeating(wake, trigger, bestInt, pi);
	}
	
	public static AdRequest getAdRequest(Context context) {
		
		String cityLat, cityLong;
		
		AdRequest adRequest = new AdRequest();
		//adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
		//adRequest.addTestDevice("71F645459A61F67B621F4CDBACD793B4");
		
		//get city from shared pref
		SharedPreferences pref = BandBaajaPrefs.get(context);
		
		String city = pref.getString(BandBaajaPrefs.PREF_CITY, "");
		if (city.length() > 0) {
			
			if (city.equalsIgnoreCase(BandBaajaPrefs.CITY_BANGALORE)) {
				cityLat = context.getString(R.string.bangalore_latitude);
				cityLong = context.getString(R.string.bangalore_longitude);
			} else if (city.equalsIgnoreCase(BandBaajaPrefs.CITY_MUMBAI)) {
				cityLat = context.getString(R.string.mumbai_latitude);
				cityLong = context.getString(R.string.mumbai_longitude);
			} else if (city.equalsIgnoreCase(BandBaajaPrefs.CITY_DELHI)) {
				cityLat = context.getString(R.string.delhi_latitude);
				cityLong = context.getString(R.string.delhi_longitude);
			} else {
				cityLat = context.getString(R.string.others_latitude);
				cityLong = context.getString(R.string.others_longitude);
			}
			
			Location tempLoc = new Location("APP");
			tempLoc.setLatitude(Double.parseDouble(cityLat));
			tempLoc.setLongitude(Double.parseDouble(cityLong));
			adRequest.setLocation(tempLoc);
		}
		
		return adRequest;
	}
	
}