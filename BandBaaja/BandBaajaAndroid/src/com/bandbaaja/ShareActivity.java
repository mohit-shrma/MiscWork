package com.bandbaaja;

import java.net.URI;

import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;

import com.bandbaaja.analytics.GoogleAnalyticsActivity;
import com.bandbaaja.db.GigsDbAdapter;
import com.bandbaaja.db.PrefDbAdapter;
import com.bandbaaja.share.FBSdkProxy;
import com.bandbaaja.util.BandBaajaUtil;
import com.bandbaaja.util.DialogBuilder;
import com.bandbaaja.util.DialogBuilder.DialogType;
import com.bandbaaja.R;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ShareActivity extends GoogleAnalyticsActivity {

	private Button mButtFacebook;
	private Button mButtTwitter;
	private Button mButtEmail;
	private Button mButtSms;
	
	private Long mRowId;
	private GigsDbAdapter mGigDbHelper;
	private Cursor mGigCursor;
	
	private String mArtist;
	private String mMesg;
	private String mEventUrl;
	private String mLocation;//venue, city
	private String mDayDate;
	private String mDescription;
	private String mGenre;
	
	private final static String CONSUMER_SECRET = "XXX";
	private final static String CONSUMER_KEY 	= "XXX";
	//on retrieval save it to App specific DB
	private String mAccessToken;
	private String mAuthorizationCode;
	private OAuthSignpostClient mOauthClient;
	private PrefDbAdapter mPrefDbAdapter;
	private long mPrefRowId;
	private Cursor mPrefCursor;
	
	private static final String TAG 				= "BandBaaja-ShareActivity";
	private static final int TWITTER_STATUS_LENGTH  = 140;
	
	private static final int DIALOG_TWITTER_PIN = 0;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share);
		
		mGigDbHelper = new GigsDbAdapter(this);
		mGigDbHelper.open();
		
		mPrefDbAdapter = new PrefDbAdapter(this);
		mPrefDbAdapter.open();
		
		mRowId		=	(savedInstanceState == null) ? null :
			(Long) savedInstanceState.getSerializable(GigsDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras	=	getIntent().getExtras();
			mRowId			=	extras != null ? extras.getLong(GigsDbAdapter.KEY_ROWID)
									   : null;
		}
		
		
		try {
			if (mRowId != null) {
				if (mGigCursor != null) {
					mGigCursor.close();
				}

				mGigCursor		=	mGigDbHelper.fetchGig(mRowId);
				
				if (mGigCursor != null) {
					startManagingCursor(mGigCursor);
					
					String city = mGigCursor.getString(mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_CITY));
					city = city.substring(0, 1).toUpperCase()+city.substring(1);
					city = (city.equalsIgnoreCase("others"))?"":city;
					
					mLocation		= mGigCursor.getString(mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_VENUE)) +", "+
										mGigCursor.getString(mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_CITY_LOC)) +
										((city.length() > 0)?", ":"") + city;
					mArtist 		= mGigCursor.getString(
										mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_ARTIST));
					mMesg			=	"Hey, check this out!";
					mEventUrl   	= mGigCursor.getString(
										mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_EVENT_URL));;
					mDayDate		= mGigCursor.getString(
								  		mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_DAY_DATE));
					mDescription	= mGigCursor.getString(
										mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_DESC));
					mGenre			= mGigCursor.getString(
										mGigCursor.getColumnIndexOrThrow(GigsDbAdapter.KEY_GENRE));	
				} else {
					Log.e(TAG, "oncreate:mGigCursor is null");
					finish();
				}
			} else {
				Log.e(TAG, "oncreate:mRowId is null");
				finish();
			}
		} catch (SQLException e) {
			Log.e(TAG, "onCreate:SQLException could be row id no longer valid");
			finish();
		}  catch (CursorIndexOutOfBoundsException e) {
			Log.e(TAG, "onCreate:SQLException could be row id no longer valid");
			finish();
		}
		
		mPrefRowId = 0;
		if (mPrefCursor != null) {
			mPrefCursor.close();
		}
		
		mPrefCursor = mPrefDbAdapter.fetchPref();
		
		if (mPrefCursor != null)
			startManagingCursor(mPrefCursor);
		
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
		
		
		
		/*@Override
	    protected void onActivityResult(int requestCode, int resultCode,
	                                    Intent data) {
	        mFacebook.authorizeCallback(requestCode, resultCode, data);
	    }*/
		
		mButtFacebook = (Button) findViewById(R.id.shareFacebook);
		mButtTwitter  = (Button) findViewById(R.id.shareTwitter);
		mButtEmail	  = (Button) findViewById(R.id.shareMail);
		mButtSms	  = (Button) findViewById(R.id.shareMessage);
		
		mButtFacebook.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (!BandBaajaUtil.isconnected(getApplicationContext())) {
					showDialog(DialogType.NETWORK_COVERAGE_INSUFF.getId());
				} else {
					shareFacebookAction();
				}
				
				//track this ui interaction
				GoogleAnalyticsTracker.getInstance()
									  .trackEvent("share", 
											  	  "facebook", 
											  	  "ShareActivity", 
											  	  0);
			}
		});
		
		mButtTwitter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (!BandBaajaUtil.isconnected(getApplicationContext())) {
					showDialog(DialogType.NETWORK_COVERAGE_INSUFF.getId());
				} else {
					shareTwitterAction();
				}
				
				//track this ui interaction
				GoogleAnalyticsTracker.getInstance()
									  .trackEvent("share", 
											  	  "twitter", 
											  	  "ShareActivity", 
											  	  0);
			}
		});
		
		mButtEmail.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (!BandBaajaUtil.isconnected(getApplicationContext())) {
					showDialog(DialogType.NETWORK_COVERAGE_INSUFF.getId());
				} else {
					shareEmailAction();
				}
				
				//track this ui interaction
				GoogleAnalyticsTracker.getInstance()
									  .trackEvent("share", 
											  	  "email", 
											  	  "ShareActivity", 
											  	  0);
			}
		});

		mButtSms.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				shareSMSAction();
				
				//track this ui interaction
				GoogleAnalyticsTracker.getInstance()
									  .trackEvent("share", 
											  	  "SMS", 
											  	  "ShareActivity", 
											  	  0);
			}
		});
		
		// Create the adView
		if (BandBaajaUtil.isconnected(getApplicationContext())) {
			AdView adView = (AdView)this.findViewById(R.id.shareAdView);
		    adView.loadAd(BandBaajaUtil.getAdRequest(this.getApplicationContext()));
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGigDbHelper != null) {
			mGigDbHelper.close();
		}
		if (mPrefDbAdapter != null) {
			mPrefDbAdapter.close();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(GigsDbAdapter.KEY_ROWID, mRowId);
	}
	
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		
		if (id == DIALOG_TWITTER_PIN) {
			//create an alert dialog asking user fot its PIN
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle(R.string.share_twitter);
			alert.setMessage(R.string.share_ask_pin);

			// Set an EditText view to get user input 
			final EditText input = new EditText(this);
			alert.setView(input);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = input.getText().toString().trim();
				  	// Do something with value!
					if (value.length() > 0) {
						//retrieve tokens and save
						ShareActivity.this.saveTokenAndTwitt(value);
					} else {
						
					}
				  }
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
					dialog.cancel();
				  }
			});
			
			dialog = alert.create();

		} else if (id == DialogType.NETWORK_COVERAGE_INSUFF.getId()) {
			AlertDialog.Builder builder = DialogBuilder.getDialogBuilder(this, 
											DialogType.NETWORK_COVERAGE_INSUFF);
			dialog = builder.create();
		} else if (id == DialogType.TWITTER_SETUP_FAIL.getId()) {
			AlertDialog.Builder builder = DialogBuilder.getDialogBuilder(this, 
											DialogType.TWITTER_SETUP_FAIL);
			dialog = builder.create();
		} else if (id == DialogType.TWEET_FAIL.getId()) {
			AlertDialog.Builder builder = DialogBuilder.getDialogBuilder(this, 
											DialogType.TWEET_FAIL);
			dialog = builder.create();
		}  else {
			dialog = null;
		}
		
		return dialog;
	}
	
	
	private void shareTwitterAction() {
		

		
		//check for twitter accesstoken in db
		if (!mPrefCursor.isFirst()) {
			mPrefCursor.moveToFirst();
		}
		
		String prefAccessToken0 = mPrefCursor.getString(
						mPrefCursor.getColumnIndexOrThrow(PrefDbAdapter.KEY_TWITT_ACCESS_TOKEN_0));
		String prefAccessToken1 = mPrefCursor.getString(
						mPrefCursor.getColumnIndexOrThrow(PrefDbAdapter.KEY_TWITT_ACCESS_TOKEN_1));
		
		
		if (prefAccessToken0 == null ||  prefAccessToken0.length() == 0
				|| prefAccessToken1 == null || prefAccessToken1.length() ==0) {
			
			new TweetSetupTask().execute((Void)null);
		
		} else {
			
			try {
				//use the tokens and tweet the event
				mOauthClient =  new OAuthSignpostClient(CONSUMER_KEY, CONSUMER_SECRET, 
														prefAccessToken0, prefAccessToken1);
				//tweet
				new TweetTask().execute(mOauthClient);
			} catch(TwitterException e) {
				Log.e(TAG, "OAuthSignpostClient:"+e.getMessage());
				showDialog(DialogType.TWEET_FAIL.getId());
			}

		}
	}
	
	
	private void saveTokenAndTwitt(String pin) {
		if (mOauthClient != null) {
			mOauthClient.setAuthorizationCode(pin);
			String[] accesstoken = mOauthClient.getAccessToken();
			//save accesstoken
			if (mPrefRowId != 0) {
				mPrefDbAdapter.updatePrefTokens(mPrefRowId, accesstoken[0], 
												accesstoken[1]);
			}
			//tweet
			//Log.d(TAG, "saveTokenAndTwitt");
			new TweetTask().execute(mOauthClient);
		}
	}
	
	
	private void shareFacebookAction() {
		
		Bundle postParams = new Bundle();
		postParams.putString("message", mMesg);
		
		if (mEventUrl.length() > 0) {
			postParams.putString("link", mEventUrl);
		}
		
		postParams.putString("name", mArtist);
		postParams.putString("caption", mGenre+", "+mDayDate+", "+mLocation);
		
		if (mDescription.length() > 0) {
			postParams.putString("description", mDescription);
		}
		
		//postParams.putString("privacy", "{\"value\":\"ALL_FRIENDS\"}");
		FBSdkProxy fbSdkProxy = new FBSdkProxy(this);
		fbSdkProxy.doLoginAndPostEvent(postParams);
	}
	
	private String sharingMessage() {
		String info = mArtist+", "
						+mDayDate+", "
						+mLocation
						+(((mEventUrl != null) && (mEventUrl.length() > 0))?", "+mEventUrl:"") 
						+".";
		String msg  = "Hey! check this out.\n" + info;
		return msg;
	}
	
	private String sharingTwitterMessage() {
		String info = mArtist+", "
						+mDayDate+", "
						+mLocation 
						+".";
		String msg  = info;
		return msg;
	}
	
	private void shareEmailAction() {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, 
								"check this out");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, sharingMessage());
		//this.startActivity(emailIntent);
		this.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}
	
	private void shareSMSAction() {
		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent.setData(Uri.parse("sms:"));
		smsIntent.putExtra("sms_body", sharingMessage());
		startActivity(smsIntent);
	}
	
	
	private void showToastMsg(String msg) {
    	Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
	
	
	private class TweetSetupTask extends AsyncTask<Void, Void, URI> {
		
		private final ProgressDialog dialog = new ProgressDialog(ShareActivity.this);
		
		
		protected void onPreExecute() {
			dialog.setMessage("Please wait...");
			dialog.setIndeterminate(true);
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
			});
			dialog.show();
		}
		
		@Override
		protected URI doInBackground(Void... params) {
			URI url = null;
			try {
				
				//Log.d(TAG, "twitter setup in backg");
				
				if (isCancelled()) 
					return null;
				
				//following is a n/w request which cud take time
				// Make an oauth client (you'll want to change this bit)
				mOauthClient = new OAuthSignpostClient(CONSUMER_KEY, 
														CONSUMER_SECRET, "oob");
				if (isCancelled()) 
					return null;
				
				url = mOauthClient.authorizeUrl();
				//Log.d(TAG, "shareTwitterAction:authorize url: "+url);
			
			} catch (TwitterException e) {
				Log.e(TAG, "TweetSetupTask:TwitterException while init of oauthclient");
			}
			
			return url;
		}
		
		protected void onPostExecute(URI url) {
		
			dialog.dismiss();
			if (url != null) {
		
				//Log.d(TAG, "shareTwitterAction::pin input + browser");
				
				//show the pin input dialog
				showDialog(DIALOG_TWITTER_PIN);
				
				// Open the authorisation page in the user's browser
				Intent myIntent = new Intent(Intent.ACTION_VIEW);
				myIntent.setData(Uri.parse(url.toString()));
				startActivity(myIntent);
		
			} else {
				showDialog(DialogType.TWITTER_SETUP_FAIL.getId());
			}
		}
		
	}
	
	
	
	private class TweetTask extends AsyncTask<OAuthSignpostClient, String, Boolean> {
		
		private final ProgressDialog dialog = new ProgressDialog(ShareActivity.this);
		private String error 				= null;
		
		protected void onPreExecute() {
			dialog.setMessage(getString(R.string.tweet_progress_msg));
			dialog.setIndeterminate(true);
			dialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
			});
			dialog.show();
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			dialog.setMessage(values[0]);
		}
		
		@Override
		protected Boolean doInBackground(OAuthSignpostClient... params) {
			// TODO Auto-generated method stub
			//params[0] is passed oauthclient
			try {
				Twitter twitter = new Twitter(null, params[0]);
				
				String status = sharingTwitterMessage();
				
				if (status.length() > TWITTER_STATUS_LENGTH) {
					status = status.substring(0, TWITTER_STATUS_LENGTH);
				}
				
				Twitter.Status twStatus= twitter.setStatus(status);
				String tweetedStatus = twStatus.toString();
				
				if (tweetedStatus!= null && 
						tweetedStatus.length() > 0) {
					//success
					return true;
				} else {
					//fail
					error = ShareActivity.this.getString(R.string.tweet_failed);
					cancel(true);
				}
				
			} catch (TwitterException e) {
				//TODO: check for tweeted already
				error = e.getMessage();
				if (error.indexOf("unauthoriz") >= 0 
						|| error.indexOf("Unauthoriz") >= 0) {
					//revoke tokens and reacquire them
					Log.e(TAG, "tweetasynctask: unauthorized access, remove token");
					mPrefDbAdapter.updatePrefTokens(mPrefRowId, "", "");
				} else if (error.indexOf("already") >= 0 ||
						error.indexOf("Already") >= 0) {
					Log.e(TAG, "tweetasynctask: already tweeted");
					error = null;
				}
				cancel(true);
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "tweetasynctask: status could be more than 160 chars: "
							+e.getMessage());
				cancel(true);
			}
			
			return false;
		}
		
		protected void onPostExecute(Boolean resp) {
			dialog.dismiss();
			if (error != null) {
				Log.e(TAG, "tweetasynctask:"+ error);
				showDialog(DialogType.TWEET_FAIL.getId());
			} else {
				showToastMsg(getString(R.string.tweet_successful));
			}
		}
		
		
	}
	
	
}