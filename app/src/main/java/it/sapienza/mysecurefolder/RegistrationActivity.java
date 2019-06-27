package it.sapienza.mysecurefolder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static it.sapienza.mysecurefolder.App.BASE_URL;
import static okhttp3.internal.http.HttpDate.format;


public class RegistrationActivity extends AppCompatActivity {

    String currentImagePath = null;
    private static final int IMAGE_REQUEST = 1;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    final int MY_VOICE_PERMISSION_CODE = 1000;
    Button fotoButton;
    Button saveButton;
    ImageView imageView;
    Button btnStartRecord, btnStopRecord;
    String pathSave = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        /*if (!checkPermissionFromDevice()) {
            requestPermission();
        }*/

        btnStartRecord = findViewById(R.id.btnStartRecord);
        btnStopRecord = findViewById(R.id.btnStopRecord);
        imageView = findViewById(R.id.mimageView);
        fotoButton = findViewById(R.id.bottoneFoto);
        btnStartRecord = findViewById(R.id.btnStartRecord);
        btnStopRecord = findViewById(R.id.btnStopRecord);
        saveButton = findViewById(R.id.bottoneSalva);
        fotoButton.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
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
        /*
        btnStartRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissionFromDevice()) {


                    pathSave = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + UUID.randomUUID().toString() + "_audio_record.wav";
                    setupMediaRecorder();
                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    Toast.makeText(RegistrationActivity.this, "Recording...", Toast.LENGTH_SHORT).show();
                } else {
                    requestPermission();
                }
            }
        });

        btnStopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.stop()
                btnStartRecord.setEnabled(true);
                btnStopRecord.setEnabled(false);
            }
        });
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
        /*Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);


        Bitmap bitmap = BitmapFactory.decodeFile(getIntent().getStringExtra("image_path"));
        imageView.setImageBitmap(bitmap);
         Bitmap bitmap = BitmapFactory.decodeFile(getIntent().getStringExtra("image_path"));
        */
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);
        Bitmap rotate = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        imageView.setImageBitmap(rotate);
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
/*
    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setOutputFile(pathSave);
    }

    private void requestPermission() {
        requestPermission(RegistrationActivity.this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, MY_VOICE_PERMISSION_CODE);
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }
*/

}



