package com.bandbaaja;

import java.util.Set;

import com.bandbaaja.analytics.GoogleAnalyticsActivity;
import com.bandbaaja.db.PrefDbAdapter;
import com.bandbaaja.net.UpdateController;
import com.bandbaaja.net.UpdateController.UpdateResult;
import com.bandbaaja.sharedpref.BandBaajaPrefs;
import com.bandbaaja.update.AlarmReceiver;
import com.bandbaaja.update.UpdateAsyncTask;
import com.bandbaaja.update.UpdateListener;
import com.bandbaaja.util.BandBaajaUtil;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class WelcomePrefActivity extends GoogleAnalyticsActivity implements UpdateListener {
	
	private PrefDbAdapter mPrefDbAdapter;
	private Cursor mPrefCursor;
	private long mPrefRowId;
	private Spinner mSpinner;
	
	private static final String TAG = "BandBaaja-WelcomePrefActivity";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welocome_pref);
		
		mPrefDbAdapter = new PrefDbAdapter(this);
	    mPrefDbAdapter.open();
		
	    if (mPrefCursor != null) {
	    	mPrefCursor.close();
	    }
	    
	    mPrefCursor = mPrefDbAdapter.fetchPref();
	    
	    if (mPrefCursor != null) {
	    	startManagingCursor(mPrefCursor);
	    }
	    
	    if (mPrefCursor == null || mPrefCursor.getCount() == 0) {
	    	//no preference created, create a default one and update the cursor
			stopManagingCursor(mPrefCursor);
			mPrefCursor.close();
			long res = mPrefDbAdapter.createAndInitPref();
			mPrefCursor = mPrefDbAdapter.fetchPref();
			startManagingCursor(mPrefCursor);
	    }
	    
	    mPrefCursor.moveToFirst();
		mPrefRowId = mPrefCursor.getInt(
						mPrefCursor.getColumnIndexOrThrow(PrefDbAdapter.KEY_ROWID));
	    
	    
		
		String[] cityArray = getResources().getStringArray(R.array.city_array);
		
		mSpinner = (Spinner) findViewById(R.id.welcomeSpinner);
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.city_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mSpinner.setAdapter(adapter);
	    mSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
		
	    Button buttNext = (Button) findViewById(R.id.welcomeNext);
	    buttNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*
				 * when clicked next, initiate update controller to fetch request
				 * from server and update in db, use asynctask for this op.
				 */
				
				//get spinner selected text and save it to db
				String city = mSpinner.getSelectedItem().toString().toLowerCase();
		    	//save to database
		    	mPrefDbAdapter.updateCity(mPrefRowId, city);
		    	
		    	//track city as session var
		    	GoogleAnalyticsTracker.getInstance()
		    						  .setCustomVar(1, 
		    								  		"city", 
		    								  		city, 
		    								  		GoogleAnalyticsActivity.SESSION_SCOPE);
		    	

		    	
		    	//set an alarm to receive pending updates
		    	//note this alarm is to be setup for first time only, all other
		    	//alarms will be setup on boot
		    	//TODO: what if device reboot after installation will we get double alarms
		    	
		    	SharedPreferences pref = BandBaajaPrefs.get(WelcomePrefActivity.this.getApplicationContext());
		    	SharedPreferences.Editor prefEditor = pref.edit();
		    	
		    	//check first launch
		    	if ( ! pref.contains(BandBaajaPrefs.FIRST_LAUNCH)) {
		    		//Log.i(TAG, "first launch detected");
		    		BandBaajaUtil.setUpdateAlarm(getApplicationContext());
		    		prefEditor.putBoolean(BandBaajaPrefs.FIRST_LAUNCH, false);
		    	}
		    	
		    	//store city in shared pref
		    	prefEditor.putString(BandBaajaPrefs.PREF_CITY, city);
		    	prefEditor.commit();
		    	
				new UpdateAsyncTask(WelcomePrefActivity.this, 
						WelcomePrefActivity.this.getString(
								R.string.welcomepref_progress_msg)).execute();
			}
		});
	    
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mPrefDbAdapter != null) {
			mPrefDbAdapter.close();
		}
	}
	
	public class MyOnItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	    	
	    	//save to database
	    	//mPrefDbAdapter.updateCity(mPrefRowId, 
			//				parent.getItemAtPosition(pos).toString().toLowerCase());
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}


	//TODO: wat if user press back here, note if cancel event detected
	@Override
	public void onUpdateComplete(Set resultCode) {
		String toastMsg;
		
		if (resultCode.contains(UpdateResult.REFRESH_SUCCESS)) {
			toastMsg = getResources().getString(R.string.gigs_refresh_success_msg);
		} else if (resultCode.contains(UpdateResult.CONNECTIVITY_ERROR)) {
			toastMsg = getResources().getString(R.string.network_coverage_insufficient);
		} else {
			toastMsg = getResources().getString(R.string.gigs_refresh_fail_msg);
		}
		
		Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
		
		//store this event result
		GoogleAnalyticsTracker.getInstance()
							  .trackEvent("operation", 
									  	  "refresh", 
									  	  this.getLocalClassName(), 
									  	  resultCode.contains(UpdateResult.REFRESH_SUCCESS)?1:0);
		
		//also invoke gig tabs
		Intent i = new Intent(WelcomePrefActivity.this, BandBaajaTabs.class);
		startActivity(i);
		finish();
	}
	
}