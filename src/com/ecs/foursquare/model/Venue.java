package com.ecs.foursquare.model;

import java.io.Serializable;

import com.google.api.client.util.Key;

public class Venue implements Serializable {

	private static final long serialVersionUID = 7011532850140949409L;

	@Key
	public String id;
	
	@Key
	public String name;
	
	@Key
	public String itemId;
	
	@Key
	public Location location;
	
	@Key
	public Category[] categories;
	
	@Key
	public boolean verified;
	
	@Key
	public Statistics stats;
	
	@Key
	public HereNow hereNow;

}
