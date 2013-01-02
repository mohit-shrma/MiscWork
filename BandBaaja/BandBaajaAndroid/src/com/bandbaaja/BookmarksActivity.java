package com.bandbaaja;

import com.bandbaaja.adapter.GigAdapter;
import com.bandbaaja.analytics.GoogleAnalyticsSessionManager;
import com.bandbaaja.db.GigsDbAdapter;
import com.bandbaaja.util.BandBaajaUtil;
import com.bandbaaja.R;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class BookmarksActivity extends ListActivity{
	
	private GigsDbAdapter m_gigsDbHelper;
	private Cursor m_gigsCursor;
	private EditText m_searchBox;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bookmarks_list);
		
		// Need to do this for every activity that uses google analytics
        GoogleAnalyticsSessionManager.getInstance(getApplication()).incrementActivityCount();
		
		m_gigsDbHelper = new GigsDbAdapter(this);
		m_gigsDbHelper.open();
		
		//setup UI callbacks
		m_searchBox = (EditText) findViewById(R.id.searchBookmarkBox);
		setClearSearchCallback();
		setTextChangeCallback();
		registerForContextMenu(getListView());
		
		if(m_searchBox != null)
        {
            // Force filtering of gigs is required because Android system is not calling onTextChanged() callback
            // because we delayed it's registration from constructor until gig list is available.
            filterBookmarks(m_searchBox.getText().toString().trim());
        }
		
		
		//fillData();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// Example of how to track a pageview event
        GoogleAnalyticsTracker.getInstance().trackPageView("/"+getClass().getSimpleName());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (m_gigsDbHelper != null) {
			m_gigsDbHelper.close();
		}

		//commenting as it could result in snappy exit
        //GoogleAnalyticsTracker.getInstance().dispatch();

        // Need to do this for every activity that uses google analytics
        GoogleAnalyticsSessionManager.getInstance().decrementActivityCount();

	}
	
	
	/**
     * Sets up the text change callback for the search box
     */
    private void setTextChangeCallback()
    {
        // Setup the callback for the edit box
        if(m_searchBox != null) 
        {
            m_searchBox.addTextChangedListener(new TextWatcher()
            {
                /**
                 * Performs search for matching application
                 */
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                	filterBookmarks(s);
                }

                /**
                 * Does nothing
                 */
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,int after)
                {                
                }

                /**
                 * Does nothing
                 */            
                @Override
                public void afterTextChanged(Editable s)
                {                
                }

            });
        }
    }
    
    
    private void filterBookmarks(CharSequence s) 
    {
        if(s == null || "".equals(s.toString()))
        {
            // Empty string make the clear search image button go away
            findViewById(R.id.btnBookmarkClearSearch).setVisibility(View.GONE);
        }
        else
        {
            // We have a search target in the edit field
            findViewById(R.id.btnBookmarkClearSearch).setVisibility(View.VISIBLE);
        }
        fillData();
    }
    
	
	/**
     * Sets up the callback for the clear search button
     */
    private void setClearSearchCallback()
    {
        ImageButton btnClearSearch = (ImageButton) findViewById(R.id.btnBookmarkClearSearch);
        if(btnClearSearch != null) 
        {
            btnClearSearch.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(m_searchBox != null) 
                    {
                        m_searchBox.setText("");                
                    }
                }
            });
        }
    }
	
	
	private void fillData() {
		//get all rows from db and create list, retieve in order of most recent event
		
		//searchbox string
		String bookmarkMatcher = null;
		if((m_searchBox != null) && 
				!m_searchBox.getText().toString().trim().equals(""))
        {
            // We've got a search target
			bookmarkMatcher = m_searchBox.getText().toString().trim();
        }
		
		
		if(m_gigsCursor != null) {
			m_gigsCursor.close();
		}
		
		
		if (bookmarkMatcher != null) {
			m_gigsCursor = m_gigsDbHelper.searchBookmarks(bookmarkMatcher);
		} else {
			m_gigsCursor = m_gigsDbHelper.fetchAllBookmarkedGigsBrief();
		}
		
		if (m_gigsCursor != null) {
			startManagingCursor(m_gigsCursor);
			
			// Create an array to specify the fields we want to display in the list (only TITLE)
	        String[] from = new String[]{GigsDbAdapter.KEY_VENUE, GigsDbAdapter.KEY_ARTIST,
	        							 GigsDbAdapter.KEY_DAY_DATE, GigsDbAdapter.KEY_CITY_LOC};
	        
	        //and an array of the fields we want to bind those fields to (in this case just text1)
	        int[] to = new int[]{R.id.gigVenue, R.id.gigArtist, R.id.gigDayDate,
	        					 R.id.gigCityLoc};
	        
	        //Now create a simple cursor adapter and set it to display
	        SimpleCursorAdapter gigs = 
	            new SimpleCursorAdapter(this, R.layout.bookmarks_row, m_gigsCursor, from, to);
	        //GigAdapter gigs = new GigAdapter(this, m_gigsCursor);
	        setListAdapter(gigs);
		}
		
	}
	
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        
		AdapterView.AdapterContextMenuInfo contextInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.giglist_contextmenu, menu);
        
        m_gigsCursor.moveToPosition(contextInfo.position);
        
      	menu.removeItem(R.id.item_addBookmarkGig);
      	
      	menu.setHeaderTitle( m_gigsCursor.getString(
        		m_gigsCursor.getColumnIndex(GigsDbAdapter.KEY_ARTIST)));
    }
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		openGig(id);
	}
	
	@Override
    public boolean onContextItemSelected(MenuItem item) {
    
		AdapterView.AdapterContextMenuInfo contextInfo = 
						(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		
		switch(item.getItemId()) {
			
			case R.id.item_openGig:
				openGig(contextInfo.id);
				break;
				
			case R.id.item_delBookmarkGig:
				m_gigsDbHelper.updateGigBookmark(contextInfo.id, false);
				fillData();
				
				//track this ui interaction
				GoogleAnalyticsTracker.getInstance()
									  .trackEvent("ui_interaction", 
											  	  "delete_bookmark", 
											  	  this.getLocalClassName(), 
											  	  0);
				break;
			
			case R.id.item_shareGig:
				Intent i = new Intent(this, ShareActivity.class);
    			i.putExtra(GigsDbAdapter.KEY_ROWID, contextInfo.id);
    			startActivity(i);
				break;

        }
		
        return super.onContextItemSelected(item);
    }
	
	public void showToastMsg(String msg) {
    	Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
	
	private void openGig(long rowId) {
		
		Intent i = new Intent(this, GigDetailActivity.class);
		i.putExtra(GigsDbAdapter.KEY_ROWID, rowId);
		startActivity(i);
	}
	
}