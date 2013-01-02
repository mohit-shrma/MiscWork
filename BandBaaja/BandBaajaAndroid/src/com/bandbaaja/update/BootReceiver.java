package com.bandbaaja.update;

import com.bandbaaja.util.BandBaajaUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "BandBaaja-BootReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// set the update alarm on reboot
		//Log.d(TAG, "onReceive(): entered");
		BandBaajaUtil.setUpdateAlarm(context.getApplicationContext());
		//Log.d(TAG, "onReceive(): exit");
	}
	
}