package com.ecs.android.foursquare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ecs.foursquare.model.FoursquareResponse;
import com.ecs.foursquare.model.Venue;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.JsonObjectParser;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;
import fi.foyt.foursquare.api.io.DefaultIOHandler;

public class FoursquareVenueList extends ListActivity {

	private static final String FOURSQUARE_API_ENDPOINT = "https://api.foursquare.com/v2/venues/search";

	private FoursquareApi foursquareApi;
	private List<CompactVenue> veneusMap = new ArrayList<CompactVenue>();
	private SharedPreferences prefs;
	private OAuth2Helper oAuth2Helper;
	private double lat;
	private double lng;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.places_list);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		this.oAuth2Helper = new OAuth2Helper(this.prefs);

		if (getIntent().getExtras() != null) {
			lat = getIntent().getExtras().getDouble(Constants.PLACE_LAT_FIELD);
			lng = getIntent().getExtras().getDouble(Constants.PLACE_LNG_FIELD);
		}

		getListView().setOnItemClickListener(
			new AdapterView.OnItemClickListener() {
	
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					
					CompactVenue venue = veneusMap.get((int) id);
					Intent intent = new Intent(getApplicationContext(),FoursquareApiSample.class);
					intent.putExtra(Constants.PLACE_ID_FIELD, venue.getId());
					intent.putExtra(Constants.PLACE_LAT_FIELD, venue.getLocation().getLat());
					intent.putExtra(Constants.PLACE_LNG_FIELD, venue.getLocation().getLng());
					intent.putExtra(Constants.PLACE_NAME_FIELD, venue.getName());
					intent.putExtra(Constants.PLACE_ADDRESS_FIELD, venue.getLocation().getAddress());
					startActivity(intent);
				}
	
			}
		);

		new PlacesListRefresher().execute();
		
	}
	
	public FoursquareApi getFoursquareApi() throws IOException {
		if (this.foursquareApi==null) {
			Credential credential = oAuth2Helper.loadCredential();
			this.foursquareApi = new FoursquareApi(Constants.OAUTH2PARAMS.getClientId(),
					Constants.OAUTH2PARAMS.getClientSecret(),
					Constants.OAUTH2PARAMS.getRederictUri(),
					credential.getAccessToken(), new DefaultIOHandler());
		}
		return this.foursquareApi;
		
	}

	private class PlacesListRefresher extends AsyncTask<Uri, Void, Void> {

		@Override
		protected Void doInBackground(Uri... params) {

			try {
				
				//performFoursquareApiCallUsingGoogleApiJavaClient();
				
				Log.i(Constants.TAG, "Retrieving places at " + lat + "," + lng);
				Result<VenuesSearchResult> venues = getFoursquareApi().venuesSearch(
						lat + "," + lng, null, null, null, null, null, null,
						null, null, null, null);
				CompactVenue[] compactVenues = venues.getResult().getVenues();
				Log.i(Constants.TAG, "found " + compactVenues.length
						+ " places");
				for (CompactVenue compactVenue : compactVenues) {
					veneusMap.add(compactVenue);
				}
				
				
				
				
			} catch (Exception ex) {
				Log.e(Constants.TAG, "Error retrieving venues", ex);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			setListAdapter(new FoursquareTableAdapter(veneusMap));
		}

	}
	
	
	
	public void performFoursquareApiCallUsingGoogleApiJavaClient() throws Exception {
		GenericUrl genericUrl = new GenericUrl(FOURSQUARE_API_ENDPOINT);
		genericUrl.put("ll",lat + "," + lng);
		HttpRequest httpRequest = oAuth2Helper.buildGetRequest(genericUrl);
		HttpResponse httpResponse = httpRequest.execute();
		JSONObject object = new JSONObject(httpResponse.parseAsString());
		JSONObject fourSquareResponse = (JSONObject) object.get("response");
		JSONArray groups = (JSONArray) fourSquareResponse.get("groups");
		JSONObject group = (JSONObject)groups.get(0);
		JSONArray items = (JSONArray)group.get("items");
		Log.i(Constants.TAG, "Found venues " + items);
		
		httpRequest = oAuth2Helper.buildGetRequest(genericUrl);
		httpRequest.setParser(new JsonObjectParser(new com.google.api.client.json.jackson2.JacksonFactory()));
		httpResponse = httpRequest.execute();
		FoursquareResponse foursquareResponse2 = httpResponse.parseAs(FoursquareResponse.class);
		Venue[] venues = foursquareResponse2.response.groups[0].items;
		Log.i(Constants.TAG, "Found venues " + venues);
		
	}
	

	private CompactVenue getVenueMapFromAdapter(int position) {
		return (((FoursquareTableAdapter) getListAdapter()).getItem(position));
	}

	class FoursquareTableAdapter extends ArrayAdapter<CompactVenue> {
		FoursquareTableAdapter(List<CompactVenue> list) {
			super(FoursquareVenueList.this, R.layout.places_list_row, list);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.places_list_row, parent, false);
				holder = new ViewHolder();
				holder.txtPlaceName = (TextView) convertView.findViewById(R.id.row_placename);
				holder.txtPlaceAddress = (TextView) convertView.findViewById(R.id.row_placeaddress);
				holder.layout = (RelativeLayout) convertView.findViewById(R.id.row_layout);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			CompactVenue venue = getVenueMapFromAdapter(position);

			try {
				holder.txtPlaceName.setText(venue.getName());
				if (venue.getLocation().getAddress() != null && venue.getLocation().getAddress().length() > 0) {
					holder.txtPlaceAddress.setText(venue.getLocation().getAddress());
				} else {
					holder.txtPlaceAddress.setText(R.string.no_address_info_found);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			return (convertView);
		}
	}

	static class ViewHolder {
		TextView txtPlaceName;
		TextView txtPlaceAddress;
		RadioButton radio;
		RelativeLayout layout;
	}
}
