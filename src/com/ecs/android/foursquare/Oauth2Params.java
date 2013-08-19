package com.ecs.android.foursquare;

import com.ecs.android.sample.oauth2.foursquare.FoursquareQueryParameterAccessMethod;
import com.google.api.client.auth.oauth2.Credential.AccessMethod;

/**
 * 
 * Enum that encapsulates the various OAuth2 connection parameters for the different providers
 * 
 * We capture the following properties for the demo application
 * 
 * clientId
 * clientSecret
 * scope
 * rederictUri
 * apiUrl
 * tokenServerUrl
 * authorizationServerEncodedUrl
 * accessMethod
 * 
 * @author davydewaele
 *
 */
public enum Oauth2Params {

	FOURSQUARE_OAUTH2("TGN2ARYNAOKZISNZ4KASXC0J2EFN5A2H2NVHBUOA0S2MOXQI","MTZ4HHMWIF22L5TWQKE5ZA202KG2CK1QN4MHX5CTJACLFPQT","https://foursquare.com/oauth2/access_token", "https://foursquare.com/oauth2/authenticate",FoursquareQueryParameterAccessMethod.getInstance(),"","http://localhost","foursquare","https://api.foursquare.com/v2/users/self/checkins"); 

    private String clientId;
	private String clientSecret;
	private String scope;
	private String rederictUri;
	private String userId;
	private String apiUrl;

	private String tokenServerUrl;
	private String authorizationServerEncodedUrl;
	
	private AccessMethod accessMethod;
	
	Oauth2Params(String clientId,String clientSecret,String tokenServerUrl,String authorizationServerEncodedUrl,AccessMethod accessMethod,String scope,String rederictUri,String userId,String apiUrl) {
		this.clientId=clientId;
		this.clientSecret=clientSecret;
		this.tokenServerUrl=tokenServerUrl;
		this.authorizationServerEncodedUrl=authorizationServerEncodedUrl;
		this.accessMethod=accessMethod;
		this.scope=scope;
		this.rederictUri=rederictUri;
		this.userId=userId;
		this.apiUrl=apiUrl;
	}
	
	public String getClientId() {
		if (this.clientId==null || this.clientId.length()==0) {
			throw new IllegalArgumentException("Please provide a valid clientId in the Oauth2Params class");
		}
		return clientId;
	}
	public String getClientSecret() {
		if (this.clientSecret==null || this.clientSecret.length()==0) {
			throw new IllegalArgumentException("Please provide a valid clientSecret in the Oauth2Params class");
		}
		return clientSecret;
	}
	
	public String getScope() {
		return scope;
	}
	public String getRederictUri() {
		return rederictUri;
	}
	public String getApiUrl() {
		return apiUrl;
	}
	public String getTokenServerUrl() {
		return tokenServerUrl;
	}

	public String getAuthorizationServerEncodedUrl() {
		return authorizationServerEncodedUrl;
	}
	
	public AccessMethod getAccessMethod() {
		return accessMethod;
	}
	
	public String getUserId() {
		return userId;
	}
}
