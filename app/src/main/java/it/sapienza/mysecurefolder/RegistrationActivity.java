package it.sapienza.mysecurefolder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;


public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = RegistrationActivity.class.getSimpleName();
    String currentImagePath = null;
    private static final int IMAGE_REQUEST = 1;
    private static final int MY_CAMERA_PERMISSION_CODE = 128;
    private static final int RECORD_REQUEST = 2;

    Button fotoButton;
    Button saveButton;
    ImageView imageView;
    Button btnStartRecord, btnStopRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        btnStartRecord = findViewById(R.id.btnStartRecord);
        btnStopRecord = findViewById(R.id.btnStopRecord);
        imageView = findViewById(R.id.mimageView);
        fotoButton = findViewById(R.id.bottoneFoto);
        btnStartRecord = findViewById(R.id.btnStartRecord);
        btnStopRecord = findViewById(R.id.btnStopRecord);
        saveButton = findViewById(R.id.bottoneSalva);


        fotoButton.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, MY_CAMERA_PERMISSION_CODE);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {

                    File imageFile = null;
                    try {
                        imageFile = getImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (imageFile != null) {
                        Uri imageUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.android.fileprovider", imageFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(cameraIntent, IMAGE_REQUEST);
                    }
                }
            }
        });


        btnStartRecord.setOnClickListener(
                view -> {
                    String filePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath() + "/recorded_audio.wav";
                    Log.d(TAG, filePath);
                    int color = getResources().getColor(R.color.colorPrimaryDark);

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
                });

        btnStopRecord.setOnClickListener(view -> {
            // btnStartRecord.setEnabled(true);
            // btnStopRecord.setEnabled(false);
        });
/*
        saveButton.setOnClickListener(v -> new Thread(() -> {
            EditText name = findViewById(R.id.name);
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("name", name.getText().toString())
                    .build();
            Request request = new Request.Builder()
                    .url(BASE_URL + "/create-new-person")
                    .post(formBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show());
                //Log.d("Risposta: ",response.message());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start());*/
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RECORD_REQUEST: {
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "onActivityResult:  Great! User has recorded and saved the audio file");
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(TAG, "onActivityResult:  Oops! User has canceled the recording");
                }
                break;
            }

            case IMAGE_REQUEST: {
                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);
                Bitmap rotate = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                imageView.setImageBitmap(rotate);
                break;
            }

            default:
                Log.e(TAG, "onActivityResult: boh");
        }
    }

    private File getImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALIAN).format(new Date());
        String imageName = "jpeg_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        currentImagePath = imageFile.getAbsolutePath();
        return imageFile;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, IMAGE_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}