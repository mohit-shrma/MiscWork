package com.bandbaaja.util;

import com.bandbaaja.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public final class DialogBuilder {
	
	public enum DialogType {
		
		NETWORK_COVERAGE_INSUFF(1),
		REFRESH_FAIL(2),
		TWITTER_SETUP_FAIL(3),
		TWEET_FAIL(4);
		
		private final int id;
		
		DialogType(int id) {
			this.id = id;
		}
		
		public int getId() {
			return this.id;
		}
		
	}
	
	private DialogBuilder() {};
	
	public static AlertDialog.Builder getDialogBuilder(Context activityContext,
												DialogType dialogType) {
		AlertDialog.Builder builder = null;
		
		switch (dialogType) {
			case NETWORK_COVERAGE_INSUFF:
				builder = getBuilderNetErr(activityContext, builder);
				break;
			case REFRESH_FAIL:
				builder = getBuilderRefreshErr(activityContext, builder);
				break;
			case TWITTER_SETUP_FAIL:
				builder = getBuilderTwitterSetupErr(activityContext, builder);
				break;
			case TWEET_FAIL:
				builder = getBuilderTweetFailErr(activityContext, builder);
				break;
		}
		
		return builder;
	}
	
	
	private static AlertDialog.Builder getBuilderErr(Context activityContext, 
														AlertDialog.Builder builder, 
														String msg,
														String title) {

		builder = new AlertDialog.Builder(activityContext);
		
		builder.setMessage(msg)
				.setTitle(title)
				.setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
				});
		
		return builder;
	}
	
	
	
	private static AlertDialog.Builder getBuilderRefreshErr(Context activityContext, 
												AlertDialog.Builder builder) {
		return getBuilderErr(activityContext, 
								builder, 
								activityContext.getString(R.string.gigs_refresh_fail_msg), 
								activityContext.getString(R.string.refresh_err_title));
	}
	
	private static AlertDialog.Builder getBuilderNetErr(Context activityContext, 
												AlertDialog.Builder builder) {
		return getBuilderErr(activityContext, 
								builder, 
								activityContext.getString(R.string.network_coverage_insufficient), 
								activityContext.getString(R.string.network_err_title));
	}
	
	private static AlertDialog.Builder getBuilderTwitterSetupErr(Context activityContext, 
												AlertDialog.Builder builder) {
		return getBuilderErr(activityContext, 
								builder, 
								activityContext.getString(R.string.tweet_setup_failed), 
								activityContext.getString(R.string.tweet_setup_failed_title));
	}
	
	private static AlertDialog.Builder getBuilderTweetFailErr(Context activityContext, 
												AlertDialog.Builder builder) {
		return getBuilderErr(activityContext, 
								builder, 
								activityContext.getString(R.string.tweet_failed), 
								activityContext.getString(R.string.share_twitter));
	}
	
}