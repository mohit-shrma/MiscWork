package com.bandbaaja.analytics;

import com.bandbaaja.R;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Application;
import android.content.Context;

public class GoogleAnalyticsSessionManager {
    protected static GoogleAnalyticsSessionManager INSTANCE;

    protected int activityCount = 0;
    protected Integer dispatchIntervalSecs;
    protected String apiKey;
    protected Context context;

    /**
     * NOTE: you should use your Application context, not your Activity context, in order to avoid memory leaks.
     */
    protected GoogleAnalyticsSessionManager( String apiKey, Application context ) {
        this.apiKey = apiKey;
        this.context = context;
    }

    /**
     * NOTE: you should use your Application context, not your Activity context, in order to avoid memory leaks.
     */
    protected GoogleAnalyticsSessionManager( String apiKey, int dispatchIntervalSecs, Application context ) {
        this.apiKey = apiKey;
        this.dispatchIntervalSecs = dispatchIntervalSecs;
        this.context = context;
    }

    /**
     * This should be called once in onCreate() for each of your activities that use GoogleAnalytics.
     * These methods are not synchronized and don't generally need to be, so if you want to do anything
     * unusual you should synchronize them yourself.
     */
    public void incrementActivityCount() {
        if( activityCount==0 )
            if( dispatchIntervalSecs==null )
                GoogleAnalyticsTracker.getInstance().start(apiKey,context);
            else
                GoogleAnalyticsTracker.getInstance().start(apiKey,dispatchIntervalSecs,context);

        ++activityCount;
    }


    /**
     * This should be called once in onDestrkg() for each of your activities that use GoogleAnalytics.
     * These methods are not synchronized and don't generally need to be, so if you want to do anything
     * unusual you should synchronize them yourself.
     */
    public void decrementActivityCount() {
        activityCount = Math.max(activityCount-1, 0);

        if( activityCount==0 )
            GoogleAnalyticsTracker.getInstance().stop();
    }


    /**
     * Get or create an instance of GoogleAnalyticsSessionManager
     */
    public static GoogleAnalyticsSessionManager getInstance( Application application ) {
        if( INSTANCE == null ) {
            //INSTANCE = new GoogleAnalyticsSessionManager( application.getString(R.string.analytics_id) , application);
            INSTANCE = new GoogleAnalyticsSessionManager( application.getString(R.string.analytics_id), 
            											  GoogleAnalyticsActivity.DISPATCH_INTERVAL_SECS, 
            											  application);
        }
        return INSTANCE;
    }

    /**
     * Only call this if you're sure an instance has been previously created using #getInstance(Application)
     */
    public static GoogleAnalyticsSessionManager getInstance() {
        return INSTANCE;
    }
}