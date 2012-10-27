# Getting started on Android

This part of the documentation shows you how to connect your Android mobile app with your apitrary backend.
While you can implement all backend communication yourself,  the recommended way is to use the "apitrary Android SDK", as this will save you a lot of time and work.


## 1. Setup

1. Download the [apitrary client library](http://www.apitrary.com/downloads/sdks/android/apitrary-android-library.jar) for Android.

2. Put the library into your project's 'libs' folder.

3. Add the „*android.permission.INTERNET*“ permission to your projects AndroidManifest.xml

## 2. Connecting to the backend API
After integrating the apitrary client library into your app project, you can now start to connect your app to your apitrary backend API.

### Create an *APYClient* instance
The core of the apitrary client library is the *APYClient* class. Whenever you want to execute an operation on your backend API, you’ll use an instance of this class.

`APYClient apitraryClient = new APYClient(API_BASE_URL, API_ID, API_KEY, API_VERSION);`

The constructor requires four parameters:

* **API_BASE_URL** - The base URL of the backend API (https://HOST:PORT)
* **API_ID** - The unique API ID identifying the the backend API
* **API_KEY** - The API key used to access the backend API
* **API_VERSION** - The version of the backend API

You can find all information related to your backend API in your apitrary launchpad.

### Performing requests
After creating the *APYClient* instance, you can now use that to perform requests on the backend API.  
When interacting with the *APYClient* instance you’ll also always use the *APYEntity* class.

An *APYEntity* is used to represent your entity data that is exchanged between your app and your backend. Assuming you have a collection of „tasks“ in your apitrary backend API, you can now create a new task entity like this:

    APYEntity task = new APYEntity(„tasks“);
    task.put(„title“, „Create Android app“);
    task.put(„description“, „Create an Android app using an apitrary backend API.“);

The constructor of the *APYEntity* class expects the name of the target collection. The actual data of the entity can be set by using the *put(String key, String value)* method.

Creating the task on the backend is now as simple as calling:
    
    apitraryClient.create(task);
    
To keep the application responsive at any time, long running operations, like networking operations, need to be executed off the UI thread. For doing so, you need to execute the line above in a separate thread. One possibility to do that is to execute the operation in an implementation of an AsyncTask.

To free you from handling concurreny yourself, the *APYClient* provides an additional asynchronous implementation for each of its (synchronous) operations.
Creating the task entity from above asynchronously, you’d do the following:

    apitraryClient.createAsync(task, new APYCreateCallback() {
    
        onSuccess(APYEntity createdEntity) {
            // The task was created successfully
        }
    
        onError(APYException error) {
            // There was an error
        }
    });

Using this approach you don’t have to care about the background processing. You only give your prepared APYEntity instance and a callback implementation into the method. When the request finished, the onSuccess() or onError() method is calld respectively (back on the UI thread) to inform you about the operations result.

## 3. Additional information

* **Checking connectivity**  
Before performing any networking operations, you should check the device’s connectivity. For that, you additionally need to add the „*android.permission.ACCESS_NETWORK_STATE*“ permission to your AndroidManifest.xml.

* **HTTP requests**  
For performing HTTP requests, the apitrary client library uses the *HttpsURLConnection* (*java.net.ssl*) class, as advised by the Google Android team on the [Android Developers Blog](http://android-developers.blogspot.de/2011/09/androids-http-clients.html).