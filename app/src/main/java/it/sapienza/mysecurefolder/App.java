package it.sapienza.mysecurefolder;

import android.app.Application;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class App extends Application {

    public static final String BASE_URL = "https://secure-folder.herokuapp.com";

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