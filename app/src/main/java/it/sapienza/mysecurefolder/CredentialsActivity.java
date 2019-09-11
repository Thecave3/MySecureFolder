package it.sapienza.mysecurefolder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials);
        nameEditText = findViewById(R.id.insertName);
        submitButton = findViewById(R.id.submitName);
        submitButton.setOnClickListener(v -> sendCredentials(nameEditText.getText().toString()));
    }

    /**
     * Sends the user name and every other possible fields for identification to the server in order to retrieve the FaceId and the VoiceId
     *
     * @param name name representing the user
     *
     */
    private void sendCredentials(String name) {
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
                    });
                } else if (responseBody.has("name")) {
                    User user = new User(responseBody);
                    Intent faceIntent = new Intent(CredentialsActivity.this, FaceActivity.class);
                    faceIntent.putExtra("user", user);
                    startActivity(faceIntent);
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplicationContext(), responseBody.toString(), Toast.LENGTH_LONG).show());
                    Log.d(TAG, "Risposta: " + response.message());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
