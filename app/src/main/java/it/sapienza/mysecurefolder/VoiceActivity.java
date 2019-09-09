package it.sapienza.mysecurefolder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VoiceActivity extends AppCompatActivity {

    private static final String TAG = RegistrationActivity.class.getSimpleName();
    private static final int RECORD_REQUEST = 2;


    private static final int MY_AUDIO_PERMISSION_CODE = 100;


    Button audioButton, btnSendRecord;
    EditText nameEditText;


    // Both the ID are taken during name registration and when they need to be passed singularly the field has to be personId
    String personIdAudio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        audioButton = findViewById(R.id.btnStartRecord);
        btnSendRecord = findViewById(R.id.btnSendRecord);


        audioButton.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_AUDIO_PERMISSION_CODE);
            } else {
                recordEnrollment();
            }
        });


        btnSendRecord.setOnClickListener(view -> sendAudioForEnrollment());
    }


    private void recordEnrollment() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALIAN).format(new Date());
        String audioPath = "/" + timeStamp;
        String externalFilePath = Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_MUSIC)).getPath();

        String filePath = externalFilePath + audioPath + ".wav";

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


    public void sendAudioForEnrollment() {
        //Send audio file to server
        new Thread(() -> {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALIAN).format(new Date());
            String audioPath = "/" + timeStamp;
            String externalFilePath = Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_MUSIC)).getPath();

            String filePath = externalFilePath + audioPath + ".wav";
            File audioFileToUpload = new File(filePath);

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("audio", audioFileToUpload.getName(),
                            RequestBody.create(MediaType.parse("audio/wav"), audioFileToUpload))
                    .addFormDataPart("per sonId", personIdAudio)
                    .build();

            Request request = new Request.Builder()
                    .url(App.getBaseUrl() + "/audio/enrollment")
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
                        nameEditText.setEnabled(true);
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    });
                } else {
                    String bodySt = responseBody.toString();
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), bodySt, Toast.LENGTH_LONG).show());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        ).start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RECORD_REQUEST: {
                if (resultCode == RESULT_OK) {
                    recordEnrollment();
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(TAG, "onActivityResult: User has canceled the recording");
                }

                Log.d(TAG, "onActivityResult: User has recorded and saved the audio files");
                audioButton.setVisibility(View.INVISIBLE);
                btnSendRecord.setVisibility(View.VISIBLE);
                break;
            }


            default:
                Log.e(TAG, "onActivityResult: boh");
        }

    }


}