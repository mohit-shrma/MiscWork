package com.bandbaaja.analytics;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.os.Bundle;

public abstract class GoogleAnalyticsActivity extends Activity {
	
	public static final int VISITOR_SCOPE 			= 1;
    public static final int SESSION_SCOPE 			= 2;
    public static final int PAGE_SCOPE 				= 3;
    public static final int DISPATCH_INTERVAL_SECS  = 10;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Need to do this for every activity that uses google analytics
        GoogleAnalyticsSessionManager.getInstance(getApplication()).incrementActivityCount();
        
        GoogleAnalyticsTracker.getInstance().setDebug(true);
	}

	@Override
    protected void onResume() {
        super.onResume();

        // Example of how to track a pageview event
        GoogleAnalyticsTracker.getInstance().trackPageView("/"+getClass().getSimpleName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Purge analytics so they don't hold references to this activity
        //commenting as it could result in snappy exit
        //GoogleAnalyticsTracker.getInstance().dispatch();

        // Need to do this for every activity that uses google analytics
        GoogleAnalyticsSessionManager.getInstance().decrementActivityCount();
    }

}