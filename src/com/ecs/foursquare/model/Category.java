package com.ecs.foursquare.model;

import java.io.Serializable;

import com.google.api.client.util.Key;

public class Category  implements Serializable{
	
	private static final long serialVersionUID = -4553521178456858700L;

	@Key
	public String id;
	
	@Key
	public String name;
	
	@Key
	public String pluralName;
	
	@Key
	public String icon;
	
	@Key
	public String[] parents;
	
	@Key
	public boolean primary;
	
}
