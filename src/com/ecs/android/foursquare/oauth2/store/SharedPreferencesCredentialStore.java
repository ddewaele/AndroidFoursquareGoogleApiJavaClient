package com.ecs.android.foursquare.oauth2.store;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;

public class SharedPreferencesCredentialStore implements CredentialStore {

	private static final String ACCESS_TOKEN = "access_token";
	private static final String EXPIRES_IN = "expires_in";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String SCOPE = "scope";

	private SharedPreferences prefs;
	
	public SharedPreferencesCredentialStore(SharedPreferences prefs) {
		this.prefs = prefs;
	}
	
	@Override
	public AccessTokenResponse read() {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
			accessTokenResponse.accessToken = prefs.getString(ACCESS_TOKEN, "");
			accessTokenResponse.expiresIn = prefs.getLong(EXPIRES_IN, 0);
			accessTokenResponse.refreshToken = prefs.getString(REFRESH_TOKEN, "");
			accessTokenResponse.scope = prefs.getString(SCOPE, "");
		return accessTokenResponse;
	}

	@Override
	public void write(AccessTokenResponse accessTokenResponse) {
		Editor editor = prefs.edit();
		if (accessTokenResponse.accessToken!=null) editor.putString(ACCESS_TOKEN,accessTokenResponse.accessToken);
		if (accessTokenResponse.expiresIn!=null) editor.putLong(EXPIRES_IN,accessTokenResponse.expiresIn);
		if (accessTokenResponse.refreshToken!=null) editor.putString(REFRESH_TOKEN,accessTokenResponse.refreshToken);
		if (accessTokenResponse.scope!=null) editor.putString(SCOPE,accessTokenResponse.scope);
		editor.commit();
	}
	
	@Override
	public void clearCredentials() {
		Editor editor = prefs.edit();
		editor.remove(ACCESS_TOKEN);
		editor.remove(EXPIRES_IN);
		editor.remove(REFRESH_TOKEN);
		editor.remove(SCOPE);
		editor.commit();
	}
}
