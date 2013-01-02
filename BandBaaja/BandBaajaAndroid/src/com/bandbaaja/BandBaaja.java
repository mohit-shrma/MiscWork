package com.bandbaaja;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.bandbaaja.analytics.GoogleAnalyticsActivity;
import com.bandbaaja.db.GigsDbAdapter;
import com.bandbaaja.db.PrefDbAdapter;
import com.bandbaaja.net.UpdateController;
import com.bandbaaja.sharedpref.BandBaajaPrefs;
import com.bandbaaja.update.AlarmReceiver;
import com.bandbaaja.update.UpdateAsyncTask;
import com.bandbaaja.update.UpdateListener;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class BandBaaja extends GoogleAnalyticsActivity {
	
	private static final String TAG = "BandBaaja-BandBaaja";
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		String city = "";
		Intent i;

		SharedPreferences pref = BandBaajaPrefs.get(this.getApplicationContext());
		city = pref.getString(BandBaajaPrefs.PREF_CITY, "");
		
		if (city != null && city.length() > 0) {
			
			//track city as session var
	    	GoogleAnalyticsTracker.getInstance()
	    						  .setCustomVar(1, 
	    								  		"city", 
	    								  		city, 
	    								  		GoogleAnalyticsActivity.SESSION_SCOPE);
	    	
			
			//already exists preference, can directly launch gigs tab
			showBandBaajaTabs();
		} else {
			//no setting stored, most probably first launch, launch welcome preferences
			//Log.i(TAG, "BandBaaja:onCreate:: first launch probably");
			i = new Intent(BandBaaja.this, WelcomePrefActivity.class);
			startActivity(i);
		}
		
		finish();
	}
	
	private void showBandBaajaTabs() {
		Intent i = new Intent(this, BandBaajaTabs.class);
		startActivity(i);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	
}