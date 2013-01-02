package com.bandbaaja;

import com.bandbaaja.R;
import com.bandbaaja.util.BandBaajaUtil;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TabHost;

import com.google.ads.*;


public class BandBaajaTabs extends TabActivity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bandbaajatabs);
		
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		
		//intent to launch activity for the tab
		intent = new Intent().setClass(this, GigsListActivity.class);
		
		//initialize TabSpec for each tab and add it to TabHost
		spec = tabHost.newTabSpec("gigs").setIndicator("Gigs",
							res.getDrawable(R.drawable.ic_tab_gigs))
							.setContent(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		tabHost.addTab(spec);
		
		
		//do the same for other tabs
		
		
		intent = new Intent().setClass(this, BookmarksActivity.class);
		spec = tabHost.newTabSpec("bookmarks").setIndicator("Bookmarks",
				res.getDrawable(R.drawable.ic_tab_bookmarks))
				.setContent(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, SettingsActivity.class);
		spec = tabHost.newTabSpec("settings").setIndicator("Settings",
				res.getDrawable(R.drawable.ic_tab_settings))
				.setContent(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
		
		
		//integrate admob
		// Create the adView
		if (BandBaajaUtil.isconnected(getApplicationContext())) {
			AdView adView = (AdView)this.findViewById(R.id.bandBaajaTabAdView);
		    adView.loadAd(BandBaajaUtil.getAdRequest(this.getApplicationContext()));
		}
	}
	
}