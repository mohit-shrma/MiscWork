package com.bandbaaja.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
 *This will receive the update alarm if fired 
 */


public class AlarmReceiver extends BroadcastReceiver {

	private static final String TAG = "BandBaaja-AlarmReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.d(TAG, "onReceive(): entered");
		WakefulIntentService.sendWakefulWork(context, UpdateService.class);
		//Log.d(TAG, "onReceive(): exit");
	}
	
}