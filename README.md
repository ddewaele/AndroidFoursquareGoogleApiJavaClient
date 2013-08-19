##Introduction

##Software requirements


##Project setup

### m2e-android plugin

This project is built using the [m2e-android plugin](http://rgladwell.github.io/m2e-android/index.html) to handle its external dependencies.

When using Eclipse ADT, it assumes that the following components are installed :

- Eclipse Market Client
- m2e-android plugin

### Eclipse MarkerPlace

If you don't have the Eclipse Marker Client installed, you can install it by clicking on 

```Help → Install new Software → Switch to the Juno Repository → General Purpose Tools → Marketplace Client```

![](https://dl.dropboxusercontent.com/u/13246619/Blog%20Articles/AndroidMavenSetup/1_available_soft.PNG)

Once you have the Eclipse Market Client installed, you can proceed to install the m2e-android plugin

![](https://dl.dropboxusercontent.com/u/13246619/Blog%20Articles/AndroidMavenSetup/2_marketplace.PNG)

```Help -> Eclipse Marketplace... and search for "android m2e".```

![](https://dl.dropboxusercontent.com/u/13246619/Blog%20Articles/AndroidMavenSetup/3_android_m2e.PNG)

More instructions can be found on the [m2e-android plugin](http://rgladwell.github.io/m2e-android/index.html) site.

### Environment setup

Make sure you have your ANDROID_HOME variable pointing to your SDK

![](https://dl.dropboxusercontent.com/u/13246619/Blog%20Articles/AndroidMavenSetup/4_env_variable.PNG)

Make sure you import Google Play Services into your Eclipse to avoid the dependency error below. 
M2E expects all library project dependencies to be present in the workspace as Maven projects.

![](https://dl.dropboxusercontent.com/u/13246619/Blog%20Articles/AndroidMavenSetup/5_playservices_import.PNG)
![](https://dl.dropboxusercontent.com/u/13246619/Blog%20Articles/AndroidMavenSetup/6_dependency_warning.PNG)

### Android Maps V2 API Key

This project uses the Google Maps V2 API for Android and requires you to retrieve an API key that you need to put in your manifest.
This API key is bound to the keystore that will be used to build/package your application.

there are a couple of steps you need to follow

- Register a project in the API console

![](https://dl.dropboxusercontent.com/u/13246619/Blog%20Articles/AndroidMavenSetup/7_register_key.PNG)

- Enable the Maps V2 API

![](https://dl.dropboxusercontent.com/u/13246619/Blog%20Articles/AndroidMavenSetup/8_enable_api.PNG)

- Create an Android Key

![](https://dl.dropboxusercontent.com/u/13246619/Blog%20Articles/AndroidMavenSetup/9_android_key.PNG)

- Specify your keystore SHA1 fingerprint.

![](https://dl.dropboxusercontent.com/u/13246619/Blog%20Articles/AndroidMavenSetup/10_android_keystore.PNG)

- Specify your package name

![](https://dl.dropboxusercontent.com/u/13246619/Blog%20Articles/AndroidMavenSetup/11_android_manifest.PNG)

- Add the API key to your Manifest.

Use the following element (child element of the application element)

	<meta-data android:name="com.google.android.maps.v2.API_KEY" android:value="INSERT_YOUR_API_KEY"/>
        