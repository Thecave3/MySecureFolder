package it.sapienza.mysecurefolder;

import android.app.Application;

import okhttp3.OkHttpClient;


public class App extends Application {

    private static final boolean DEBUG_HOME = false;

    private static final String BASE_URL = (DEBUG_HOME) ? "http://192.168.1.50:3000" : "https://secure-folder.herokuapp.com";

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