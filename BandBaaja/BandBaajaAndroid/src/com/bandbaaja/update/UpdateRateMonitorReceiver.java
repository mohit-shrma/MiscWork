package com.bandbaaja.update;

import com.bandbaaja.util.BandBaajaUtil;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class UpdateRateMonitorReceiver extends BroadcastReceiver {

	private ComponentName mReceiver;
	private PackageManager mPm;
	
	private static final String TAG = "BandBaaja-UpdateRateMonitorReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.d(TAG, "onReceive(): entered");
		mReceiver 	= new ComponentName(context, 
							AlarmReceiver.class);
		mPm 		= context.getPackageManager();
		
		boolean enableAction = true;
		//Log.d(TAG, intent.getAction());
		if (intent.getAction().equalsIgnoreCase("android.intent.action.BATTERY_LOW")) {
			enableAction = false;
		} else if (intent.getAction().equalsIgnoreCase("android.intent.action.BATTERY_OKAY")) {
			enableAction = true;
		} else if (intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")) {
			if (BandBaajaUtil.isconnected(context.getApplicationContext())) {
				enableAction = true;
			} else {
				enableAction = false;
			}
			
		} else if (intent.getAction().equalsIgnoreCase("android.net.conn.BACKGROUND_DATA_SETTING_CHANGED")) {
			ConnectivityManager cm = (ConnectivityManager) context.
										getSystemService(Context.CONNECTIVITY_SERVICE);
			boolean isBackgDataAllowed = ((cm!=null) && cm.getBackgroundDataSetting());
			if (isBackgDataAllowed) {
				enableAction = true;
			} else {
				enableAction = false;
			}
		}
		
		if (enableAction) {
			enableAlarmReceiver(context);
		} else {
			disableAlarmReceiver(context);
		}
		//Log.d(TAG, "onReceive(): exit");
	}
	
	private void disableAlarmReceiver(Context context) {
		//Log.d(TAG, "disableAlarmReceiver");
		mPm.setComponentEnabledSetting(mReceiver,
									   PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
									   PackageManager.DONT_KILL_APP);
	}
	
	private void enableAlarmReceiver(Context context) {
		//Log.d(TAG, "enableAlarmReceiver");
		mPm.setComponentEnabledSetting(mReceiver,
				   PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				   PackageManager.DONT_KILL_APP);
	}
	
	
}