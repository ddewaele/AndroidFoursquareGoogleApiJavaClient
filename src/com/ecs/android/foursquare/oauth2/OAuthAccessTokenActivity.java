package com.ecs.android.foursquare.oauth2;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ecs.android.foursquare.FoursquareApiSample;
import com.ecs.android.foursquare.oauth2.store.CredentialStore;
import com.ecs.android.foursquare.oauth2.store.SharedPreferencesCredentialStore;
import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest.AuthorizationCodeGrant;
import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
import com.google.api.client.auth.oauth2.draft10.AuthorizationRequestUrl;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 * Execute the OAuthRequestTokenTask to retrieve the request, and authorize the request.
 * After the request is authorized by the user, the callback URL will be intercepted here.
 * 
 */
public class OAuthAccessTokenActivity extends Activity {

	final String TAG = getClass().getName();
	
	private SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting task to retrieve request token.");
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		WebView webview = new WebView(this);
        webview.getSettings().setJavaScriptEnabled(true);  
        webview.setVisibility(View.VISIBLE);
        setContentView(webview);
        
        /* WebViewClient must be set BEFORE calling loadUrl! */  
        webview.setWebViewClient(new WebViewClient() {  

        	@Override  
            public void onPageStarted(WebView view, String url,Bitmap bitmap)  {  
        		System.out.println("onPageStarted : " + url);
            }
        	@Override  
            public void onPageFinished(WebView view, String url)  {  
            	
            	if (url.startsWith(OAuth2ClientCredentials.REDIRECT_URI)) {
            		try {
						
            			if (url.indexOf("code=")!=-1) {
            			
	            			String code = extractCodeFromUrl(url);
							
	            	        AuthorizationCodeGrant request = new AuthorizationCodeGrant(new NetHttpTransport(),
	            	                new JacksonFactory(),
	            	                OAuth2ClientCredentials.ACCESS_TOKEN_URL,
	            	                OAuth2ClientCredentials.CLIENT_ID, 
	            	                OAuth2ClientCredentials.CLIENT_SECRET,
	            	                code,
	            	                OAuth2ClientCredentials.REDIRECT_URI);
	            	            AccessTokenResponse accessTokenResponse = request.execute();

	            	            CredentialStore credentialStore = new SharedPreferencesCredentialStore(prefs);
							credentialStore.write(accessTokenResponse );
				  		      view.setVisibility(View.INVISIBLE);
				  		      startActivity(new Intent(OAuthAccessTokenActivity.this,FoursquareApiSample.class));
            			} else if (url.indexOf("error=")!=-1) {
            				view.setVisibility(View.INVISIBLE);
            				new SharedPreferencesCredentialStore(prefs).clearCredentials();
            				startActivity(new Intent(OAuthAccessTokenActivity.this,FoursquareApiSample.class));
            			}
            			
					} catch (HttpResponseException e) {
						try {
							System.out.println("Error occured " + e.response.parseAsString());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					} catch (Exception e) {
							e.printStackTrace();
						}

            	}
                System.out.println("onPageFinished : " + url);
  		      
            }
			private String extractCodeFromUrl(String url) {
				return url.substring(OAuth2ClientCredentials.REDIRECT_URI.length()+6,url.length());
			}  
        });  
        
        AuthorizationRequestUrl authorizationRequestUrl = new AuthorizationRequestUrl(OAuth2ClientCredentials.AUTHORIZATION_URL);
		authorizationRequestUrl.clientId = OAuth2ClientCredentials.CLIENT_ID;
		authorizationRequestUrl.redirectUri = OAuth2ClientCredentials.REDIRECT_URI;
        webview.loadUrl(authorizationRequestUrl.build());		
	}

}
