package com.bandbaaja;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.bandbaaja.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class SceneMap extends MapActivity {

	private MapView mMapView;
	private MapController mMapController;
	private GeoPoint mGeoPoint;
	
	
	private class CustomItemizedOverlay  extends ItemizedOverlay {

		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
		private Context mContext;
		
		public CustomItemizedOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}
		
		public CustomItemizedOverlay(Drawable defaultMarker, Context context) {
			super(boundCenterBottom(defaultMarker));
			mContext = context;
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mOverlays.get(i);
		}

		@Override
		public int size() {
			return mOverlays.size();
		}
		
		
		
		public void addOverlay(OverlayItem overlay) {
		    mOverlays.add(overlay);
		    populate();
		}
		
	}
	
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.scene_map);
	    
	    mMapView = (MapView) findViewById(R.id.mapview);
	    
	    Bundle extras = getIntent().getExtras();
	    String latLong = extras.getString("latlong");
	    String[] arrLatLong = latLong.split(",");
	    double lat  = Double.parseDouble(arrLatLong[0]);
	    double longt = Double.parseDouble(arrLatLong[1]);
	    mGeoPoint	= new GeoPoint( 
	    					(int)(lat*1E6),
	    					(int)(longt*1E6)
	    				);
	    
	    List<Overlay> mapOverlays = mMapView.getOverlays();
	    Drawable drawable = this.getResources().getDrawable(R.drawable.pushpin);
	    CustomItemizedOverlay itemizedoverlay = new CustomItemizedOverlay(drawable);
	    OverlayItem overlayitem = new OverlayItem(mGeoPoint, "", "");
	    itemizedoverlay.addOverlay(overlayitem);
	    mapOverlays.add(itemizedoverlay);
	    
	    mMapController = mMapView.getController();
	    mMapController.animateTo(mGeoPoint);
	    mMapController.setZoom(17);
	    
	    mMapView.setBuiltInZoomControls(true);
	    mMapView.invalidate();
	}

	
}