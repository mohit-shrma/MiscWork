package com.bandbaaja;

import java.util.Set;

import com.bandbaaja.analytics.GoogleAnalyticsActivity;
import com.bandbaaja.db.GigsDbAdapter;
import com.bandbaaja.db.PrefDbAdapter;
import com.bandbaaja.net.UpdateController;
import com.bandbaaja.net.UpdateController.UpdateResult;
import com.bandbaaja.sharedpref.BandBaajaPrefs;
import com.bandbaaja.update.UpdateAsyncTask;
import com.bandbaaja.update.UpdateListener;
import com.bandbaaja.util.DialogBuilder;
import com.bandbaaja.util.DialogBuilder.DialogType;
import com.bandbaaja.R;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;


public class SettingsActivity extends GoogleAnalyticsActivity implements UpdateListener {
	
	
	private PrefDbAdapter mPrefDbAdapter;
	private GigsDbAdapter mGigsDbHelper;
	
	private Cursor mPrefCursor;
	private long mPrefRowId;
	private String mCity;
	private Spinner mSpinner;
	private CheckBox mNotifyCheckbox;
	private SharedPreferences mPref;
	
	private static final String TAG = "SettingsActivity";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.settings);
	    
	    mPref = BandBaajaPrefs.get(getApplicationContext());
	    
	    mGigsDbHelper = new GigsDbAdapter(this);
	    mGigsDbHelper.open();
	    
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
			mPrefDbAdapter.createAndInitPref();
			mPrefCursor = mPrefDbAdapter.fetchPref();
			startManagingCursor(mPrefCursor);
	    }
	    
	    mPrefCursor.moveToFirst();
		mPrefRowId = mPrefCursor.getInt(
						mPrefCursor.getColumnIndexOrThrow(PrefDbAdapter.KEY_ROWID));
		
		mCity = mPrefCursor.getString(
				mPrefCursor.getColumnIndexOrThrow(PrefDbAdapter.KEY_CITY));
		
		String[] cityArray = getResources().getStringArray(R.array.city_array);
		
	    mSpinner = (Spinner) findViewById(R.id.spinner);
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.city_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mSpinner.setAdapter(adapter);
	    mSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
	    //set the default selected index or city from db
	    
	    if (mCity != null && mCity.length() >0) {
	    	int i;
	    	for (i = cityArray.length-1; i >= 0; i--) {
	    		if (cityArray[i].compareToIgnoreCase(mCity) == 0) {
	    			break;
	    		}
	    	}
	    	mSpinner.setSelection(i);
	    }
	    
	    mNotifyCheckbox = (CheckBox) findViewById(R.id.checkNotification);
	    mNotifyCheckbox.setOnCheckedChangeListener(new MyCheckStateListener());
	    mNotifyCheckbox.setChecked(mPref.getBoolean(BandBaajaPrefs.NOTIFICATIONS, 
	    							true));
	    
	    Button buttFeedback = (Button) findViewById(R.id.feedback);
	    buttFeedback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// open send feedback form
				openFeedbackForm();
			}
		});
	    
	}
	
	private void openFeedbackForm() {
		Intent i = new Intent(this, FeedbackActivity.class);
		startActivity(i);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mPrefDbAdapter !=  null) {
			mPrefDbAdapter.close();
		}
		if (mGigsDbHelper != null) {
			mGigsDbHelper.close();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {

		Dialog dialog;
		
		if (id == DialogType.NETWORK_COVERAGE_INSUFF.getId()) {
			AlertDialog.Builder builder = DialogBuilder.getDialogBuilder(this, 
											DialogType.NETWORK_COVERAGE_INSUFF);
			dialog = builder.create();
		} else if (id == DialogType.REFRESH_FAIL.getId()) {
			AlertDialog.Builder builder = DialogBuilder.getDialogBuilder(this, 
					DialogType.REFRESH_FAIL);
			dialog = builder.create();
		} else {
			dialog = null;
		}

		return dialog;
	}
	
	
	private class MyCheckStateListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			//store notifications preference in shared preference
			SharedPreferences.Editor prefEditor = mPref.edit();
			prefEditor.putBoolean(BandBaajaPrefs.NOTIFICATIONS, isChecked);
			prefEditor.commit();
		}
		
	}
	
	private class MyOnItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {

	    	String selectedCity = mSpinner.getSelectedItem().toString().toLowerCase();
			if (mCity != null && mCity.compareToIgnoreCase(selectedCity) != 0) {
				//remove previous gig data and previous recentgigId, save tis new city
				if (mGigsDbHelper != null) {
					mGigsDbHelper.deleteAllGigs();
				}
				if (mPrefDbAdapter != null) {
					mPrefDbAdapter.updateCity(mPrefRowId, selectedCity);
					mPrefDbAdapter.updateLastRefreshGigId(mPrefRowId, "");
					mCity = selectedCity;
				}
				
				//track city as session var
		    	GoogleAnalyticsTracker.getInstance()
		    						  .setCustomVar(1, 
		    								  		"city", 
		    								  		mCity, 
		    								  		GoogleAnalyticsActivity.SESSION_SCOPE);
		    	
		    	//store city in shared pref
		    	SharedPreferences.Editor prefEditor = mPref.edit();
		    	prefEditor.putString(BandBaajaPrefs.PREF_CITY, mCity);
		    	prefEditor.commit();

				new UpdateAsyncTask(SettingsActivity.this, 
									 SettingsActivity.this.getString(
											 	R.string.settings_progress_msg)
									).execute();
			} else {
				//dont need to do anything
				Log.e(TAG, 
					   "settingsSave: mCity null or mCity same as previous city");
			}

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
			Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
		} else if (resultCode.contains(UpdateResult.CONNECTIVITY_ERROR)) {
			showDialog(DialogType.NETWORK_COVERAGE_INSUFF.getId());
		} else {
			showDialog(DialogType.REFRESH_FAIL.getId());
		}
		
		//store this event result
		GoogleAnalyticsTracker.getInstance()
							  .trackEvent("operation", 
									  	  "refresh", 
									  	  this.getLocalClassName(), 
									  	  resultCode.contains(UpdateResult.REFRESH_SUCCESS)?1:0);

	}
	
}