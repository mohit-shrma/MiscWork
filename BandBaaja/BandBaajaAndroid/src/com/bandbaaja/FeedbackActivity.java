package com.bandbaaja;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.bandbaaja.analytics.GoogleAnalyticsActivity;
import com.bandbaaja.util.BandBaajaUtil;
import com.bandbaaja.util.DialogBuilder;
import com.bandbaaja.util.DialogBuilder.DialogType;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

public class FeedbackActivity extends GoogleAnalyticsActivity {
	
	private EditText mFeedbackText;
	
	private static final String TAG = "BandBaaja-FeedbackActivity";
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		
		mFeedbackText = (EditText) findViewById(R.id.feedbackText);
		Button buttSubmit = (Button) findViewById(R.id.submitFeedback);
		buttSubmit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				//track this ui interaction
				GoogleAnalyticsTracker.getInstance()
									  .trackEvent("ui_interaction", 
											  	  "submit", 
											  	  "FeedbackActivity", 
											  	  0);
				
				if (!BandBaajaUtil.isconnected(getApplicationContext())) {
					showDialog(DialogType.NETWORK_COVERAGE_INSUFF.getId());
					return;
				}
				
				if (mFeedbackText.getText().toString().trim().length() > 0) {
					new FeedbackTask().execute(mFeedbackText.getText().toString().trim());
				}
				
				//hide keyboard 
				InputMethodManager mgr = (InputMethodManager) getSystemService(
													Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(mFeedbackText.getWindowToken(), 0);
			}
		});
		
		// Create the adView
		if (BandBaajaUtil.isconnected(getApplicationContext())) {
		    AdView adView = (AdView)this.findViewById(R.id.feedbackAdView);
		    adView.loadAd(BandBaajaUtil.getAdRequest(this.getApplicationContext()));
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
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



	private class FeedbackTask extends AsyncTask<String, Void, Integer> {
		
		private ProgressDialog mProgressDialog;
		
		private final static int HTTP_ERROR 		 = 400;
		private final static int SERVER_SIDE_ERROR   = 600;
		private final static int SUCCESS			 = 1;
		
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(FeedbackActivity.this);
			mProgressDialog.setMessage(getString(R.string.feedback_progress_msg));
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
			});
			mProgressDialog.show();
		}
		
		@Override
		protected Integer doInBackground(String... feedbacks) {
			// TODO Auto-generated method stub
			return postFeedback(feedbacks[0]);
		}
		
		protected void onPostExecute(Integer ret) {
			mProgressDialog.dismiss();
			String toastMsg; 
			if (ret == SUCCESS) {
				toastMsg = getResources().getString(R.string.feedback_success_msg);
			} else {
				toastMsg = getResources().getString(R.string.feedback_fail_msg);
			}
			Toast.makeText(FeedbackActivity.this, toastMsg, Toast.LENGTH_LONG).show();
			finish();
		}
		
		private String convertStreamToString(InputStream is) {
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        StringBuilder sb = new StringBuilder();

	        String line = null;
	        try {
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                is.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        return sb.toString();
	    }
		
		private int postFeedback(String feedback) {
			
			//TODO: pick up url from DB 
			String url = getResources().getString(R.string.server_url)
							+"/feedback";
			
			try {
				
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(url);
				HttpResponse response;
				
				String android_id = Secure.getString(getContentResolver(), 
														Secure.ANDROID_ID);
				if (android_id == null) {
					android_id = "emulator";
				}
				
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("unique_id", android_id));
				pairs.add(new BasicNameValuePair("feedback", feedback));
				httpPost.setEntity(new UrlEncodedFormEntity(pairs));
				
				//execute the request
				response = httpClient.execute(httpPost);
				
				int statusCode = response.getStatusLine().getStatusCode();
				
				if (statusCode != HttpStatus.SC_OK) {
					Log.e(TAG, "postFeedback::response not OK:"+statusCode);
					return HTTP_ERROR;
				}
				
				HttpEntity httpEntity = response.getEntity();
				
				if (httpEntity != null) {
					//simple string response read
					InputStream istream = httpEntity.getContent();
					String result = convertStreamToString(istream);
					istream.close();
					//Log.i(TAG, "postFeedback::response:"+result);
					
					if (result == null || result.length() == 0) {
						Log.e(TAG, 
								"postFeedback::invalid json response");
						return HTTP_ERROR;
					}
					//parse result to see if success or failure
					//{"result": "success", "unique_id": "mohit@example.com"}
					JSONObject jsonResp = new JSONObject(result);
					String responseResult = jsonResp.getString("result");
					if ("success".compareToIgnoreCase(responseResult) == 0) {
						//success
						return SUCCESS;
					} else {
						//failure
						return SERVER_SIDE_ERROR;
					}
				} else {
					return SERVER_SIDE_ERROR;
				}
				
			} catch (JSONException e) {
				Log.e(TAG, ""
									+e.getMessage());
			} catch (ClientProtocolException e) {
				Log.e(TAG, ""
									+e.getMessage());
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, ""
									+e.getMessage());
			
			} catch (IOException e) {
				Log.e(TAG, ""
									+e.getMessage());
			}
			
			return SERVER_SIDE_ERROR;
			
		}
		
	}
	
}