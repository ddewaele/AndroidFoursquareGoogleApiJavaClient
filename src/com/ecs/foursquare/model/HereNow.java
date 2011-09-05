package com.ecs.foursquare.model;

import java.io.Serializable;

import com.google.api.client.util.Key;

public class HereNow  implements Serializable{

	private static final long serialVersionUID = -2307624173279738888L;

	@Key
	public int count;
}
