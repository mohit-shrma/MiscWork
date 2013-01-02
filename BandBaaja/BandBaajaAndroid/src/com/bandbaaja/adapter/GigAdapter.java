package com.bandbaaja.adapter;

import com.bandbaaja.db.GigsDbAdapter;
import com.bandbaaja.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GigAdapter extends CursorAdapter {

	private LayoutInflater mLayoutInflater;
    private Context mContext;
	
	public GigAdapter(Context context, Cursor c) {
		super(context, c);
		mContext = context;
        mLayoutInflater = LayoutInflater.from(context); 
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		String venue = cursor.getString(cursor.getColumnIndex(GigsDbAdapter.KEY_VENUE));
		String artist = cursor.getString(cursor.getColumnIndex(GigsDbAdapter.KEY_ARTIST));
		String dayDate = cursor.getString(cursor.getColumnIndex(GigsDbAdapter.KEY_DAY_DATE));
		String cityLoc = cursor.getString(cursor.getColumnIndex(GigsDbAdapter.KEY_CITY_LOC));
		int bookmarked = cursor.getInt(cursor.getColumnIndex(GigsDbAdapter.KEY_BOOKMARKED));
		
		TextView venueText = (TextView) view.findViewById(R.id.gigVenue);
		if (venueText != null) {
			venueText.setText(venue);
		}
		
		TextView artistText = (TextView) view.findViewById(R.id.gigArtist);
		if (artistText != null) {
			artistText.setText(artist);
		}
		
		TextView dayDateText = (TextView) view.findViewById(R.id.gigDayDate);
		if (dayDateText != null) {
			dayDateText.setText(dayDate);
		}
		
		TextView cityLocText = (TextView) view.findViewById(R.id.gigCityLoc);
		if (cityLocText != null) {
			cityLocText.setText(cityLoc);
		}
		
		ImageView bookmarkImage = (ImageView) view.findViewById(R.id.itemBookmarked);
		
		if (bookmarkImage!=null) {
			bookmarkImage.setVisibility(ImageView.INVISIBLE);
			if (bookmarked == 1) {
				//make the bookmark icon visible
				bookmarkImage.setVisibility(ImageView.VISIBLE);
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = mLayoutInflater.inflate(R.layout.gigs_row, parent, false);
        return v;
	}
	
}

