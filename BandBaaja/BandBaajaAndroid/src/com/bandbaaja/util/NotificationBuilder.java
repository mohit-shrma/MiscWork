package com.bandbaaja.util;

import android.app.Notification;

public final class NotificationBuilder {
	
	public enum NotificationType {
		
		GIGS_STORED(1);
		
		private final int id;
		
		NotificationType(int id) {
			this.id = id;
		}
		
		public int getId() {
			return this.id;
		}
	}
	
	private NotificationBuilder() {}
	
}