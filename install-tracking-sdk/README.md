# LoopMe Install-Tracking-SDK #

Integrating the LoopMe install tracking SDK is very simple and should take less than 5 minutes. 

NOTE The LoopMe SDK requires a minimum of Android 2.2 (API Level 8). 

## SDK Integration ##

* Download the latest Install Tracking SDK
* Copy the JAR file into the libs in your project root {project_root}\libs\.
* In your AndroidManifest.xml file in your project root, add the permissions:
```xml     
     <uses-permission android:name="android.permission.INTERNET" />
```	 
* Add the following to your code in onCreate method. This will send the app install event 
to our reporting backend servers when the app is launched the first time.
```java
      LoopMeManager.loopmeTrackInstallation(getApplicationContext());
```