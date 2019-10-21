package it.sapienza.mysecurefolder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import it.sapienza.mysecurefolder.user.User;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VoiceActivity extends AppCompatActivity {

    private static final int MY_AUDIO_PERMISSION_CODE = 100;
    private static final String TAG = RegistrationActivity.class.getSimpleName();
    private static final int RECORD_REQUEST = 2;

    private User user;
    private boolean isFaceTestPassed;
    private String filePath;

    private Button audioButton;
    private ProgressBar progressBar;

    private VoiceActivity self;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        user = (User) getIntent().getSerializableExtra("user");
        isFaceTestPassed = getIntent().getBooleanExtra("isFaceTestPassed", false);

        progressBar = findViewById(R.id.progress_bar);
        audioButton = findViewById(R.id.btnStartRecord);

        audioButton.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_AUDIO_PERMISSION_CODE);
            } else {
                recordEnrollment();
            }
        });

        self = this;
    }

    /**
     * Records an enrollment in order to verify the correspondence with the audio features on server
     */
    private void recordEnrollment() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALIAN).format(new Date());
        String audioPath = "/" + timeStamp;
        String externalFilePath = Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_MUSIC)).getPath();

        filePath = externalFilePath + audioPath + ".wav";

        Log.d(TAG, filePath);
        int color = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);

        AndroidAudioRecorder.with(this)
                .setFilePath(filePath)
                .setColor(color)
                .setRequestCode(RECORD_REQUEST)
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.MONO)
                .setSampleRate(AudioSampleRate.HZ_16000)
                .setAutoStart(true)
                .setKeepDisplayOn(true)
                .record();

    }

    /**
     * Sends and enrollment audio recorded to the server
     */
    public void sendAudioForEnrollment() {
        progressBar.setVisibility(View.VISIBLE);
        audioButton.setVisibility(View.INVISIBLE);
        //Send audio file to server
        new Thread(() -> {
            File audioFileToUpload = new File(filePath);
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("audio", audioFileToUpload.getName(),
                            RequestBody.create(MediaType.parse("audio/wav"), audioFileToUpload))
                    .addFormDataPart("personId", user.getAudioId())
                    .build();

            Request request = new Request.Builder()
                    .url(App.getBaseUrl() + "/audio/verify")
                    .post(requestBody)
                    .build();

            try {
                Response response = App.getHTTPClient().newCall(request).execute();
                assert response.body() != null;
                String resBody = response.body().string();
                JSONObject responseBody = new JSONObject(resBody);

                if (responseBody.has("error")) {
                    String error = responseBody.getString("error");
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        audioButton.setVisibility(View.VISIBLE);
                    });
                } else if (responseBody.has("result")) {
                    Log.d(TAG, responseBody.toString());
                    if (responseBody.getString("result").equals("Accept")) {
                        if (isFaceTestPassed) {
                            Intent galleryIntent = new Intent(VoiceActivity.this, GalleryActivity.class);
                            galleryIntent.putExtra("user", user);
                            startActivity(galleryIntent);
                            self.finish();
                        } else {
                            Intent speakerIntent = new Intent(VoiceActivity.this, SpeakerActivity.class);
                            speakerIntent.putExtra("user", user);
                            startActivity(speakerIntent);
                            self.finish();
                        }
                    } else {
                        // in this condition Voice test is not passed so user must do the speaker identification even if face test is passed.
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), resBody, Toast.LENGTH_LONG).show();
                            Intent speakerIntent = new Intent(VoiceActivity.this, SpeakerActivity.class);
                            speakerIntent.putExtra("user", user);
                            startActivity(speakerIntent);
                            self.finish();
                            progressBar.setVisibility(View.INVISIBLE);
                            audioButton.setVisibility(View.VISIBLE);
                        });
                    }
                } else {
                    String bodySt = responseBody.toString();
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), bodySt, Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        audioButton.setVisibility(View.VISIBLE);
                    });
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Saved the audio file.");
                sendAudioForEnrollment();
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "User has canceled the recording");
            }
        } else {
            Log.e(TAG, "onActivityResult: boh");
        }

    }
}