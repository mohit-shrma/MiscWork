package com.bandbaaja.update;

import java.util.Set;

import com.bandbaaja.net.UpdateController;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;

public class UpdateAsyncTask extends AsyncTask<Void, Void, Set> {
	
	private UpdateListener mUpdateListener;
	private String mDialogMsg;
	private ProgressDialog mProgressDialog;
	
	public UpdateAsyncTask(UpdateListener updateListener,
							String dialogMsg) {
		this.mUpdateListener = updateListener;
		this.mDialogMsg	= dialogMsg;
		//TODO:check if following casting to context works
		this.mProgressDialog = new ProgressDialog((Context) this.mUpdateListener);
		this.mProgressDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				//cancel the current task
				cancel(false);
			}
		});
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
		UpdateController.getInstance().cancel();
	}

	protected void onPreExecute() {
		mProgressDialog.setMessage(mDialogMsg);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();
	}
	
	@Override
	protected Set doInBackground(Void... params) {
		Set ret = UpdateController.getInstance()
				   .refreshData(((Context)this.mUpdateListener).getApplicationContext());
		return ret;
	}
	
	protected void onPostExecute(Set ret) {
		mProgressDialog.dismiss();
		mUpdateListener.onUpdateComplete(ret);
	}
	
}