package com.ecs.foursquare.model;

import java.io.Serializable;

import com.google.api.client.util.Key;

public class CheckinItem implements Serializable {

	private static final long serialVersionUID = -6210250259719738812L;

	@Key
	public String id;
	
	@Key
	public long createdAt;
	
	@Key
	public String type;
	
	@Key
	public String timeZone;
	
	@Key
	public Venue venue;
	
}
