package com.bandbaaja.share;

import com.bandbaaja.facebook.android.AsyncFacebookRunner;
import com.bandbaaja.facebook.android.BaseDialogListener;
import com.bandbaaja.facebook.android.BaseRequestListener;
import com.bandbaaja.facebook.android.DialogError;
import com.bandbaaja.facebook.android.Facebook;
import com.bandbaaja.facebook.android.FacebookError;
import com.bandbaaja.facebook.android.SessionEvents;
import com.bandbaaja.facebook.android.SessionStore;
import com.bandbaaja.facebook.android.Facebook.DialogListener;
import com.bandbaaja.facebook.android.SessionEvents.AuthListener;
import com.bandbaaja.facebook.android.SessionEvents.LogoutListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

//class helps in login and posting event on user's wall post

public class FBSdkProxy {
	
	//Facebook application id
	public static final String APP_ID = "xxxx";
	
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;
	private Activity mActivity;
	private String[] mPermissions;
	private SessionListener mSessionListener = new SessionListener();
	//TODO: handler not to be used in background thread
	private Handler mHandler;
	private Bundle  mEventParams;
	
	public FBSdkProxy(final Activity activity) {
		
		mFacebook 		= new Facebook(APP_ID);
		mAsyncRunner 	= new AsyncFacebookRunner(mFacebook);
		mActivity		= activity;
		mPermissions	= new String[] {"read_stream, publish_stream"};
		
		//TODO: investigate
		mHandler		= new Handler();
		
		SessionStore.restore(mFacebook, activity);
       
        SessionEvents.addAuthListener(mSessionListener);
		SessionEvents.addLogoutListener(mSessionListener);
	}
	
	public void doLogin() {
		if (!mFacebook.isSessionValid()) {
			//session not valid then login
			mFacebook.authorize(mActivity, mPermissions, 
								new LoginDialogListener());
		}
	}
	
	public void doLoginAndPostEvent(Bundle params) {
		mEventParams = params;
		mFacebook.dialog(mActivity, "feed", params, new SampleDialogListener());
		//TODO: Single sign on
		/*if (!mFacebook.isSessionValid()) {
			//session not valid then login
			mFacebook.authorize(mActivity, mPermissions, 
								new LoginDialogListener());
		}*/
	}
	
	public class SampleDialogListener extends BaseDialogListener {

        public void onComplete(Bundle values) {
            final String postId = values.getString("post_id");
            if (postId != null) {
                //Log.d("BandBaaja-FBSdkProxy", "Dialog Success! post_id=" + postId);
            } else {
                //Log.d("BandBaaja-FBSdkProxy", "No wall post made");
            }
        }
    }
	
	private void postSampleFacebookEvent(Bundle params) {
		String graphPath  = "me/feed";
    	String httpMethod = "POST";
    	
	}
	
	private final class LoginDialogListener implements DialogListener {
        public void onComplete(Bundle values) {
            SessionEvents.onLoginSuccess();
        }

        public void onFacebookError(FacebookError error) {
            SessionEvents.onLoginError(error.getMessage());
        }
        
        public void onError(DialogError error) {
            SessionEvents.onLoginError(error.getMessage());
        }

        public void onCancel() {
            SessionEvents.onLoginError("Action Canceled");
        }
    }

	private class LogoutRequestListener extends BaseRequestListener {
        public void onComplete(String response, final Object state) {
            // callback should be run in the original thread, 
            // not the background thread
            mHandler.post(new Runnable() {
                public void run() {
                    SessionEvents.onLogoutFinish();
                }
            });
        }
    }
	
	private class SessionListener implements AuthListener, LogoutListener {
        
        public void onAuthSucceed() {
        	//action to take on successful login
            SessionStore.save(mFacebook, mActivity);
            
            //try to post event to user wall
            if (mEventParams != null) {
            	//run a please wait progress dialog in source activity
            	//should be cancelable too
            }
        }

        public void onAuthFail(String error) {
        	//action to take when login failed
        }
        
        public void onLogoutBegin() {           
        }
        
        public void onLogoutFinish() {
            SessionStore.clear(mActivity);
        }
    }
	
	

	
}