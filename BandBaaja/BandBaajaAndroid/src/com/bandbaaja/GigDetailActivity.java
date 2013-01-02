package com.bandbaaja;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.bandbaaja.analytics.GoogleAnalyticsActivity;
import com.bandbaaja.db.GigsDbAdapter;
import com.bandbaaja.util.BandBaajaUtil;
import com.bandbaaja.util.DialogBuilder;
import com.bandbaaja.util.DialogBuilder.DialogType;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.graphics.Path.FillType;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class GigDetailActivity extends GoogleAnalyticsActivity {
	
	private GigsDbAdapter mGigDbHelper;
	private Long mRowId;
	private TextView mGigArtist;
	private TextView mGigGenre;
	private TextView mGigDayDate;
	private TextView mGigFullLocation;
	private TextView mGigDescription;
	private ImageView mBookmarkImage;
	private Cursor mGigCursor;
	
	private static final String TAG = "BandBaaja-GigDetailActivity";
	
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gig_detail);
		
		mGigDbHelper = new GigsDbAdapter(this);
		mGigDbHelper.open();
		
		mGigArtist 			= (TextView) findViewById(R.id.gigDetailArtist);
		mGigGenre 			= (TextView) findViewById(R.id.gigDetailGenre);
		mGigDayDate 		= (TextView) findViewById(R.id.gigDetailDayDate);
		mGigFullLocation	= (TextView) findViewById(R.id.gigDetailFullLocation);
		mGigDescription		= (TextView) findViewById(R.id.gigDetailDescription);
		mBookmarkImage		= (ImageView) findViewById(R.id.gigDetailBookmarked);
		
		mRowId		=	(savedInstanceState == null) ? null :
							(Long) savedInstanceState.getSerializable(GigsDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras	=	getIntent().getExtras();
			mRowId			=	extras != null ? extras.getLong(GigsDbAdapter.KEY_ROWID)
											   : null;
		}

		populateFields();
		
		
		// Create the adView
		if (BandBaajaUtil.isconnected(getApplicationContext())) {
		    AdView adView = (AdView)this.findViewById(R.id.gigDetailAdView);
		    adView.loadAd(BandBaajaUtil.getAdRequest(this.getApplicationContext()));
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGigDbHelper != null) {
			mGigDbHelper.close();
		}
	}
	
	private void populateFields() {
		
		try {
			
			if (mRowId != null) {
				
				if (mGigCursor != null)
					mGigCursor.close();
				
				mGigCursor		=	mGigDbHelper.fetchGig(mRowId);
				
				if (mGigCursor !=  null) {
				
					startManagingCursor(mGigCursor);
					String city = mGigCursor.getString(mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_CITY));
					city = city.substring(0, 1).toUpperCase()+city.substring(1);
					city = (city.equalsIgnoreCase("others"))?"":city;
					String temp 	= mGigCursor.getString(mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_VENUE)) +", "+
										mGigCursor.getString(mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_CITY_LOC))+ 
										((city.length() > 0)?", ":"") + city;
							
					mGigFullLocation.setText(temp);
					temp = mGigCursor.getString(
							mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_ARTIST));
					mGigArtist.setText(temp);
					temp = mGigCursor.getString(
							mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_GENRE));
					mGigGenre.setText(temp);
					mGigDayDate.setText(mGigCursor.getString(
							mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_DAY_DATE)));
					mGigDescription.setText(mGigCursor.getString(
							mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_DESC)));
					int isBookmarked = mGigCursor.getInt(mGigCursor.getColumnIndex(GigsDbAdapter.KEY_BOOKMARKED));
					if (isBookmarked == 1) {
						//if bookmarked then make the bookmark image visible
						mBookmarkImage.setVisibility(ImageView.VISIBLE);
					} else {
						//make boookmark image invisible
						mBookmarkImage.setVisibility(ImageView.INVISIBLE);
					}
				} else {
					Log.e(TAG, "populateFields:mGigCursor is null");
					finish();
				}
			} else {
				Log.e(TAG, "oncreate:mRowId is null");
				finish();
			}
			
		} catch (SQLException e) {
			Log.e(TAG, "populateFields:SQLException could be row id no longer valid");
			finish();
		} catch (CursorIndexOutOfBoundsException e) {
			Log.e(TAG, "populateFields:CursorOOBException could be row id no longer valid");
			finish();
		}
		
	}

	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(GigsDbAdapter.KEY_ROWID, mRowId);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.gigdetail_optionmenu, menu);
	    if (mGigCursor != null) {
	    	int isBookmarked = mGigCursor.getInt(mGigCursor.getColumnIndex(GigsDbAdapter.KEY_BOOKMARKED)); 
	        if (isBookmarked == 1) {
	        	//remove addBookmark option
	        	menu.removeItem(R.id.item_addBookmarkGig);
	        } else {
	        	//remove delBookmark option
	        	menu.removeItem(R.id.item_delBookmarkGig);
	        }
	    }
	    return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.gigdetail_optionmenu, menu);
	    if (mGigCursor != null) {
	    	int isBookmarked = mGigCursor.getInt(mGigCursor.getColumnIndex(GigsDbAdapter.KEY_BOOKMARKED)); 
	        if (isBookmarked == 1) {
	        	//remove addBookmark option
	        	menu.removeItem(R.id.item_addBookmarkGig);
	        } else {
	        	//remove delBookmark option
	        	menu.removeItem(R.id.item_delBookmarkGig);
	        }
	    }
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {

		Dialog dialog;
		
		if (id == DialogType.NETWORK_COVERAGE_INSUFF.getId()) {
			AlertDialog.Builder builder = DialogBuilder.getDialogBuilder(this, 
											DialogType.NETWORK_COVERAGE_INSUFF);
			dialog = builder.create();
		} else {
			dialog = null;
		}

		return dialog;
	}
	
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        
    	switch(item.getItemId()) {
        
    		case R.id.item_addBookmarkGig:
    			mGigDbHelper.updateGigBookmark(mRowId, true);
    			populateFields();
    			
    			//track this ui interaction
				GoogleAnalyticsTracker.getInstance()
									  .trackEvent("ui_interaction", 
											  	  "add_bookmark", 
											  	  this.getLocalClassName(), 
											  	  0);
    			
                return true;
            
    		case R.id.item_delBookmarkGig:
    			mGigDbHelper.updateGigBookmark(mRowId, false);
    			populateFields();
    			
    			//track this ui interaction
				GoogleAnalyticsTracker.getInstance()
									  .trackEvent("ui_interaction", 
											  	  "delete_bookmark", 
											  	  this.getLocalClassName(), 
											  	  0);
    			
    			return true;
    			
    		case R.id.item_showMap:
    			
    			if (mGigCursor != null && mGigCursor.getCount() > 0) {
    				String query = mGigCursor.getString((mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_LAT_LONG)));
    				String venue  = mGigCursor.getString((mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_VENUE)));
    				if (query.length() > 0) {
    					//use the latlong
    					BandBaajaUtil.launchMap(this, query, venue);
    				} else {
    					//perform map search
    					
    					String city = mGigCursor.getString(mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_CITY));
    					city = city.substring(0, 1).toUpperCase()+city.substring(1);
    					city = (city.equalsIgnoreCase("others"))?"":city;
    					
    					query 	= venue +", "+
							mGigCursor.getString(mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_CITY_LOC)) +
							((city.length() > 0)?", ":"") + city;
    					try {
    						query = URLEncoder.encode(query, "utf-8");
    						BandBaajaUtil.launchMap(this, query);
    					} catch (UnsupportedEncodingException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    				}
    			}
    			
    			//track this ui interaction
				GoogleAnalyticsTracker.getInstance()
									  .trackEvent("ui_interaction", 
											  	  "map", 
											  	  this.getLocalClassName(), 
											  	  0);
    			
    			
                return true;
                
    		case R.id.item_shareGig:
    				Intent i = new Intent(this, ShareActivity.class);
        			i.putExtra(GigsDbAdapter.KEY_ROWID, mRowId);
        			startActivity(i);				
                return true;
                
    		case R.id.item_moreInfo:
    			
    			if (BandBaajaUtil.isconnected(this.getApplicationContext())) {
    				if (mGigCursor != null) {
    					String url = mGigCursor.getString(mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_EVENT_URL));
    					if (url.length() > 0) {
    						BandBaajaUtil.launchWeb(this, url);
    					}
    				}        		
    			} else {
					//no connectivity
					showDialog(DialogType.NETWORK_COVERAGE_INSUFF.getId());
				}
    			
    			//track this ui interaction
				GoogleAnalyticsTracker.getInstance()
									  .trackEvent("ui_interaction", 
											  	  "more", 
											  	  this.getLocalClassName(), 
											  	  0);
    			
    			return true;
    	}

    	return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
	protected void onResume() {
    	super.onResume();
    	populateFields();
		
	}

	public void showToastMsg(String msg) {
    	Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
	
}