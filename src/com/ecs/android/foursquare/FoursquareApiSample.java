package com.ecs.android.foursquare;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ecs.android.foursquare.oauth2.OAuth2ClientCredentials;
import com.ecs.android.foursquare.oauth2.OAuthAccessTokenActivity;
import com.ecs.android.foursquare.oauth2.store.CredentialStore;
import com.ecs.android.foursquare.oauth2.store.SharedPreferencesCredentialStore;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.Checkin;
import fi.foyt.foursquare.api.entities.Setting;
import fi.foyt.foursquare.api.io.DefaultIOHandler;

public class FoursquareApiSample extends MapActivity {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	private FoursquareApi foursquareApi;
	
	private SharedPreferences prefs;
	private TextView apiResponseCode; 
	private TextView checkinText;
	
	private MapView mapView;
	private MapController mc;

	private Drawable drawable;
	
	String venueName = null;
	String venueAddress = null;
	String venueId = null;
	 
	View checkinSection;
	
	private boolean showCheckinSection = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

		this.drawable = this.getResources().getDrawable(R.drawable.pin_red);
		
		this.mapView = (MapView) findViewById(R.id.mapview1);
		this.checkinSection = findViewById(R.id.checkin_section);
		this.checkinText = (TextView) findViewById(R.id.checkin_text);
		this.apiResponseCode = (TextView) findViewById(R.id.response_code);
		
		this.mapView.setBuiltInZoomControls(true);
		this.mapView.setVisibility(View.GONE);
		this.mc = mapView.getController();
		
		this.checkinSection.setVisibility(View.GONE);
		
		HelloItemizedOverlay itemizedoverlay = new HelloItemizedOverlay(drawable);
	    mapView.getOverlays().add(itemizedoverlay);
	    
		if (getIntent().getExtras()!=null) {
	    	Double lat = getIntent().getExtras().getDouble(Constants.PLACE_LAT_FIELD);
	    	Double lng = getIntent().getExtras().getDouble(Constants.PLACE_LNG_FIELD);
	    	venueAddress = getIntent().getExtras().getString(Constants.PLACE_ADDRESS_FIELD);
	    	venueName = getIntent().getExtras().getString(Constants.PLACE_NAME_FIELD);
	    	venueId = getIntent().getExtras().getString(Constants.PLACE_ID_FIELD);
	    	GeoPoint p = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
	    	HelloItemizedOverlay venueOverlay = (HelloItemizedOverlay) mapView.getOverlays().get(0);
	        OverlayItem overlayitem = new OverlayItem(p, venueName, venueAddress);
	        venueOverlay.addOverlay(overlayitem);
	        mc.animateTo(p);
	        mc.setCenter(p);
	        mc.setZoom(16);
	        showCheckinSection = true;
	        this.checkinSection.setVisibility(View.VISIBLE);
	    }
		
		
		/**
		 * Launch the OAuth flow to get an access token required to do authorized API calls.
		 * When the OAuth flow finishes, we redirect to this Activity to perform the API call.
		 */
		Button launchOauth = (Button) findViewById(R.id.btn_launch_oauth);
	    launchOauth.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent().setClass(v.getContext(),OAuthAccessTokenActivity.class));
			}
		});

	    
	    /**
	     * Clearing the credentials and performing an API call to see the unauthorized message.
	     */ 
		Button clearCredentials = (Button) findViewById(R.id.btn_clear_credentials);
	    clearCredentials.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				clearCredentials();
				new PerformApiCallTask().execute();
			}

		});
		
		/**
		 * Performs a Foursquare checkin to the place the user selected in the previous screen.
		 * We retrieve our access token from the CredentialStore, load up the FoursquareApi and perform the checkin.
		 */
		Button checkinButton = (Button)findViewById(R.id.btn_checkin);
	    checkinButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new CheckinTask().execute();
			}
		});
		
		new PerformApiCallTask().execute();
	}
	
	/**
	 * Clears our credentials (token and token secret) from the shared preferences.
	 * We also setup the authorizer (without the token).
	 * After this, no more authorized API calls will be possible.
	 */
    private void clearCredentials() {
    	new SharedPreferencesCredentialStore(prefs).clearCredentials();
    	this.checkinSection.setVisibility(View.GONE);
    	this.mapView.setVisibility(View.GONE);
    }
	
    private class CheckinTask extends AsyncTask<Uri, Void, Void> {

		private String apiStatusMsg;
		
		@Override
		protected Void doInBackground(Uri...params) {
			try {
				Result<Checkin> result = getFoursquareApi().checkinsAdd(venueId, null, getString(R.string.checkin_msg), null, null,null,null,null);
				if (result.getMeta().getCode()==200) {
					apiStatusMsg = "Checked in to " + venueName;
				} else {
					apiStatusMsg = result.getMeta().getErrorDetail();
				}
			} catch (FoursquareApiException e) {
				e.printStackTrace();
			}
            return null;
		}

		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(FoursquareApiSample.this, apiStatusMsg, Toast.LENGTH_LONG).show();
		}

	}
    
    
    private class PerformApiCallTask extends AsyncTask<Uri, Void, Void> {

		private boolean apiCallSuccess=false;
		private String apiStatusMsg;
		@Override
		protected Void doInBackground(Uri...params) {
			try {
				
				Result<Setting> result = getFoursquareApi().settingsAll();
				
			    if (result.getMeta().getCode() == 200) {
			    	apiStatusMsg = "OK - user sends to Twitter = " +  result.getResult().getSendToTwitter();
			    	apiCallSuccess=true;
			    } else {
			    	apiCallSuccess=false;
			    	StringBuffer sb = new StringBuffer();
					sb.append("Error occured: ");
					sb.append("  code: " + result.getMeta().getCode());
					sb.append("  type: " + result.getMeta().getErrorType());
					sb.append("  detail: " + result.getMeta().getErrorDetail());
					apiStatusMsg = sb.toString();
			    }

			} catch (Exception ex) {
				ex.printStackTrace();
				apiResponseCode.setText("Error occured : " + ex.getMessage());
			}
            return null;
		}

		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (apiCallSuccess) {
				mapView.setVisibility(View.VISIBLE);
				if (showCheckinSection) {
					checkinText.setText(venueName + " " + venueAddress);
				}
			} else {
				mapView.setVisibility(View.GONE);
				apiResponseCode.setText(apiStatusMsg);
			}

		}

	}
    
    private class HelloItemizedOverlay extends ItemizedOverlay<OverlayItem>
    {
    	
    	/**
    	 * Calling populate here to avoid a nullpointerexception.
    	 * 
    	 * @param defaultMarker
    	 */
    	public HelloItemizedOverlay(Drawable defaultMarker) {
    		  super(boundCenterBottom(defaultMarker));
    		  populate();
    	}
    	
    	public void addOverlay(OverlayItem overlay) {
    	    mOverlays.add(overlay);
    	    populate();
    	}
    	
    	@Override
    	protected OverlayItem createItem(int i) {
    	  return mOverlays.get(i);
    	}

    	@Override
    	public int size() {
    	  return mOverlays.size();
    	}
        
    	/**
    	 * 
    	 * Triggered when the user clicks on the map.
    	 * 
    	 */
    	@Override
		public boolean onTap(GeoPoint p, MapView mapView) {
    		HelloItemizedOverlay itemizedoverlay = (HelloItemizedOverlay) mapView.getOverlays().get(0);
            OverlayItem overlayitem = new OverlayItem(p, "Location at " + "title","snippet");
            itemizedoverlay.addOverlay(overlayitem);
            mapView.invalidate();
    		popFoursquareVenueList(p.getLatitudeE6()/1E6,p.getLongitudeE6()/1E6);
			return true;
		}    

    }
    
    /**
     * 
     * when we click on the map, we launch the places list screen by passing on the
     * coordinates of the map that the user clicked on.
     * 
     * @param lat
     * @param lng
     */
    private void popFoursquareVenueList(double lat,double lng) {
		Intent intent = new Intent(getApplicationContext(),FoursquareVenueList.class);
		intent.putExtra(Constants.PLACE_LAT_FIELD, lat);
		intent.putExtra(Constants.PLACE_LNG_FIELD,lng);
		startActivity(intent);	
    }

	
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
	public FoursquareApi getFoursquareApi() {
		if (this.foursquareApi==null) {
			this.prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			CredentialStore credentialStore = new SharedPreferencesCredentialStore(prefs);
			
			AccessTokenResponse accessTokenResponse = credentialStore.read();
			this.foursquareApi = new FoursquareApi(OAuth2ClientCredentials.CLIENT_ID,
					OAuth2ClientCredentials.CLIENT_SECRET,
					OAuth2ClientCredentials.REDIRECT_URI,
					accessTokenResponse.accessToken, new DefaultIOHandler());
		}
		return this.foursquareApi;
		
	}

	
}