package com.bandbaaja.update;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.bandbaaja.BandBaaja;
import com.bandbaaja.BandBaajaTabs;
import com.bandbaaja.R;
import com.bandbaaja.net.UpdateController;
import com.bandbaaja.net.UpdateController.UpdateResult;
import com.bandbaaja.sharedpref.BandBaajaPrefs;
import com.bandbaaja.util.NotificationBuilder.NotificationType;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

public class UpdateService extends WakefulIntentService {

	private GoogleAnalyticsTracker mTracker;
	private final String TAG = "BandBaaja-UpdateService";
	
	public UpdateService() {
		super("UpdateService");
		
		
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		//do update work
		//Log.i(TAG, "UpdateService called");
		Set retVal = EnumSet.of(UpdateResult.ALREADY_RUNNING);
		
		if (isAppInForeground()) {
			//dont update as cud be a problem, if only an outdated gig is already opened
			//Log.i(TAG, "Update deferred as app in forground");
		} else {
			//update
			retVal = UpdateController.getInstance().refreshData(getApplicationContext());
			
			//check if notification need to be added
			if (retVal.contains(UpdateResult.GIGS_STORED)) {
				
				//check if settings is placed to notify user
				SharedPreferences pref = BandBaajaPrefs.get(getApplicationContext());
				if (pref.getBoolean(BandBaajaPrefs.NOTIFICATIONS, true)) {
					//show user notification that new items are there for him to see
					//Log.d(TAG, "Notification of new event: " + retVal);
					notifyUser();
				} else {
					//Log.d(TAG, "Notification off by user");
				}
			}

			//Log.i(TAG, "UpdateService:retVal:: " + retVal);
		}
		
		//initialize tracker
		mTracker = GoogleAnalyticsTracker.getInstance();
		
		Context ctx = this.getApplicationContext(); 
		String analyticsId = getString(R.string.analytics_id);//getResources().getString(R.string.analytics_id);

		mTracker.start(analyticsId, ctx);
		
		//track this wakeful work of update
		mTracker.trackEvent("operation", 
						  	  "refresh", 
						  	  this.getClass().getSimpleName(), 
						  	  retVal.contains(UpdateResult.REFRESH_SUCCESS)?1:0);
		
		mTracker.dispatch();
		mTracker.stop();
		
	}
	
	private boolean isAppInForeground() {
		
		String packageName = this.getPackageName();
		ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE); 
		List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo pInfo : runningProcesses) {
			if (pInfo.processName.compareToIgnoreCase(packageName) == 0 
					&& pInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}
		return false;
	}
	
	
	private void notifyUser() {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(ns);
		
		int icon 				= R.drawable.ic_stat_notification;
		CharSequence tickerText = "BandBaaja";              // ticker-text
		long when 				= System.currentTimeMillis();         // notification time
		Context context 		= getApplicationContext();      // application Context
		CharSequence contentTitle 	= "BandBaaja Update";  // expanded message title
		CharSequence contentText 	= "New gigs found.";      // expanded message text

		Intent notificationIntent = new Intent(this, BandBaaja.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		// the next two lines initialize the Notification, using the configurations above
		Notification notification = new Notification(icon, tickerText, when);
		
		//add sound if current hr >08 <22
		Calendar cal = Calendar.getInstance();
		int currentHr = cal.get(Calendar.HOUR_OF_DAY);
		
		if (currentHr >= 8 && currentHr <= 22) {
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags	  |= Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		mNotificationManager.notify(NotificationType.GIGS_STORED.getId(), notification);
	}
}
