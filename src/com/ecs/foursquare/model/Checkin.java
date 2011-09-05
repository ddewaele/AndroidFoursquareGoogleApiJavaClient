package com.ecs.foursquare.model;

import java.io.Serializable;

import com.google.api.client.util.Key;

public class Checkin implements Serializable{

	private static final long serialVersionUID = 4954893174144414294L;

	@Key
	public int count;
	
	@Key
	public CheckinItem[] items;
}
