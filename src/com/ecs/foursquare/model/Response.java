package com.ecs.foursquare.model;

import java.io.Serializable;

import com.google.api.client.util.Key;

public class Response  implements Serializable {

	private static final long serialVersionUID = -8744243204974447941L;

	@Key
	public Group[] groups;
	
}
