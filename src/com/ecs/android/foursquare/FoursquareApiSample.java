package com.ecs.android.foursquare;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ecs.android.foursquare.oauth2.OAuthAccessTokenActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.auth.oauth2.Credential;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.Checkin;
import fi.foyt.foursquare.api.entities.Setting;
import fi.foyt.foursquare.api.io.DefaultIOHandler;

public class FoursquareApiSample extends FragmentActivity {

	private ArrayList<Marker> markers = new ArrayList<Marker>();
	
	private FoursquareApi foursquareApi;
	
	private SharedPreferences prefs;
	private TextView apiResponseCode; 
	private TextView checkinText;
	
	private GoogleMap googleMap;

	String venueName = null;
	String venueAddress = null;
	String venueId = null;
	 
	View checkinSection;
	
	private boolean showCheckinSection = false;

	private SupportMapFragment mapFragment;

	private OAuth2Helper oAuth2Helper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		oAuth2Helper = new OAuth2Helper(this.prefs);
		
		this.checkinSection = findViewById(R.id.checkin_section);
		this.checkinText = (TextView) findViewById(R.id.checkin_text);
		this.apiResponseCode = (TextView) findViewById(R.id.response_code);
		
		mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		googleMap = mapFragment.getMap();
		googleMap.setMyLocationEnabled(true);
		
		googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng point) {
	    		popFoursquareVenueList(point.latitude,point.longitude);
				
			}
		});
		
		this.checkinSection.setVisibility(View.GONE);
		
		if (getIntent().getExtras()!=null) {
	    	Double lat = getIntent().getExtras().getDouble(Constants.PLACE_LAT_FIELD);
	    	Double lng = getIntent().getExtras().getDouble(Constants.PLACE_LNG_FIELD);
	    	venueAddress = getIntent().getExtras().getString(Constants.PLACE_ADDRESS_FIELD);
	    	venueName = getIntent().getExtras().getString(Constants.PLACE_NAME_FIELD);
	    	venueId = getIntent().getExtras().getString(Constants.PLACE_ID_FIELD);
	    	
	        addMarkerToMap(new LatLng(lat,lng), venueName, venueAddress);
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
				try {
					clearCredentials();
					new PerformApiCallTask().execute();
				} catch (IOException e) {
					e.printStackTrace();
				}
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
	
	public void addMarkerToMap(LatLng latLng,String title,String snippet) {
	    Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng)
	             .title("title")
	             .snippet("snippet"));
	    markers.add(marker);
	    
	    googleMap.animateCamera(
	    		CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 10)) 
        );           	    

	}
	
	/**
	 * Clears our credentials (token and token secret) from the shared preferences.
	 * We also setup the authorizer (without the token).
	 * After this, no more authorized API calls will be possible.
	 * @throws IOException 
	 */
    private void clearCredentials() throws IOException {
    	this.oAuth2Helper.clearCredentials();
    	this.checkinSection.setVisibility(View.GONE);
    	hideMap();
    }
	
    private void hideMap() {
//    	FragmentManager fm = getSupportFragmentManager();
//    	FragmentTransaction ft = fm.beginTransaction();
//    	ft.hide(this.mapFragment).commit();		
	}

    private void showMap() {
//    	FragmentManager fm = getSupportFragmentManager();
//    	FragmentTransaction ft = fm.beginTransaction();
//    	ft.show(this.mapFragment).commit();		
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
			} catch (Exception e) {
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
				showMap();
				if (showCheckinSection) {
					checkinText.setText(venueName + " " + venueAddress);
				}
			} else {
				hideMap();
				apiResponseCode.setText(apiStatusMsg);
			}

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

	public FoursquareApi getFoursquareApi() throws IOException {
		if (this.foursquareApi==null) {
			this.prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			Credential credential = this.oAuth2Helper.loadCredential();
			this.foursquareApi = new FoursquareApi(Constants.OAUTH2PARAMS.getClientId(),
					Constants.OAUTH2PARAMS.getClientSecret(),
					Constants.OAUTH2PARAMS.getRederictUri(),
					credential.getAccessToken(), new DefaultIOHandler());
		}
		return this.foursquareApi;
		
	}

	
}