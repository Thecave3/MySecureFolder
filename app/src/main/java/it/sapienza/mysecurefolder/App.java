package it.sapienza.mysecurefolder;

import android.app.Application;

import okhttp3.OkHttpClient;


public class App extends Application {

    private static final String BASE_URL = "https://secure-folder.herokuapp.com" ;//"http://192.168.1.50:3000";

    public static OkHttpClient getHTTPClient() {
        return client;
    }

    public static OkHttpClient client = new OkHttpClient();


    public static String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

}