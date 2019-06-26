package it.sapienza.mysecurefolder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InitActivity extends AppCompatActivity {
    private final static String TAG = InitActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        String url = App.getBaseUrl() + "/list-group";

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            Log.d(TAG, "Risposta: ");
            response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
