package it.sapienza.mysecurefolder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import it.sapienza.mysecurefolder.user.User;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CredentialsActivity extends AppCompatActivity {

    private final static String TAG = CredentialsActivity.class.getSimpleName();
    EditText nameEditText;
    Button submitButton;
    ProgressBar progressBar;
    CredentialsActivity self;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials);
        nameEditText = findViewById(R.id.insertName);
        submitButton = findViewById(R.id.submitName);
        progressBar = findViewById(R.id.progress_bar);
        submitButton.setOnClickListener(v -> sendCredentials(nameEditText.getText().toString()));
        self = this;
    }

    /**
     * Sends the user name and every other possible fields for identification to the server in order to retrieve the FaceId and the VoiceId
     *
     * @param name name representing the user
     *
     */
    private void sendCredentials(String name) {
        submitButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            RequestBody formBody = new FormBody.Builder()
                    .add("name", name)
                    .build();
            Request request = new Request.Builder()
                    .url(App.getBaseUrl() + "/person")
                    .post(formBody)
                    .build();
            try {
                Response response = App.getHTTPClient().newCall(request).execute();
                JSONObject responseBody = new JSONObject(Objects.requireNonNull(response.body()).string());
                if (responseBody.has("error")) {
                    String error = responseBody.getString("error");
                    runOnUiThread(() -> {
                        nameEditText.setEnabled(true);
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                        submitButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    });
                } else if (responseBody.has("name")) {
                    User user = new User(responseBody);
                    Intent faceIntent = new Intent(CredentialsActivity.this, FaceActivity.class);
                    faceIntent.putExtra("user", user);
                    startActivity(faceIntent);
                    self.finish();
                } else {
                    runOnUiThread(()->{
                        Toast.makeText(getApplicationContext(), responseBody.toString(), Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Risposta: " + response.message());
                        nameEditText.setEnabled(true);
                        submitButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    });
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
