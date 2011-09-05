package com.ecs.foursquare.model;

import java.io.Serializable;
import java.util.List;

import com.google.api.client.util.Key;

public class VenuesList implements Serializable {
	
	private static final long serialVersionUID = -31166334584553076L;

	@Key
	public List<Venue> items;
}
