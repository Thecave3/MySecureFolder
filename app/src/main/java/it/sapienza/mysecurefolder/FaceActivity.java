package it.sapienza.mysecurefolder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
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


import it.sapienza.mysecurefolder.user.User;

public class FaceActivity extends AppCompatActivity {

    private User user;
    private static final String TAG = CredentialsActivity.class.getSimpleName();
    private static final int IMAGE_REQUEST = 1;
    ImageView profileImage;
    Button photoButton;
    private static final int MY_CAMERA_PERMISSION_CODE = 128;
    String currentImagePath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        user = (User) getIntent().getSerializableExtra("user");
        photoButton = findViewById(R.id.bottoneFoto);
        profileImage = findViewById(R.id.mimageView);

        photoButton.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_CAMERA_PERMISSION_CODE);
            } else {
                takePicture();
            }
        });
    }

        private void takePicture() {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                try {
                    File imageFile = getImageFile();
                    if (imageFile != null) {
                        Uri imageUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.android.fileprovider", imageFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(cameraIntent, IMAGE_REQUEST);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

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

    private void sendImage() {
        new Thread(() -> {
            File fileToUpload = new File(currentImagePath);
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("face", fileToUpload.getName(),
                            RequestBody.create(MediaType.parse("image/jpeg"), fileToUpload))
                    .addFormDataPart("personId", user.getFaceId())
                    .build();

            Request request = new Request.Builder()
                    .url(App.getBaseUrl() + "/detect")
                    .post(requestBody)
                    .build();

            try {
                Response response = App.getHTTPClient().newCall(request).execute();
                assert response.body() != null;
                String resBody = response.body().string();
                Log.d(TAG, resBody);
                JSONObject responseBody = new JSONObject(resBody);

                if (responseBody.has("error")) {
                    String error = responseBody.getString("error");
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show());
                } else {
                    String bodySt = responseBody.toString();
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), bodySt, Toast.LENGTH_LONG).show());
                    verify(bodySt);
            }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private void verify(String bodySt){
        RequestBody verifyBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("face", bodySt)
                .addFormDataPart("personId", user.getFaceId())
                .build();
        Request verify = new Request.Builder()
                .url(App.getBaseUrl() + "/verify")
                .post(verifyBody)
                .build();
        try {
            Response responseVerify = App.getHTTPClient().newCall(verify).execute();
            assert responseVerify.body() != null;
            String resVerify = responseVerify.body().string();
            Log.d(TAG, resVerify);
            JSONObject responseVerifyBody = new JSONObject(resVerify);

            if (responseVerifyBody.has("error")) {
                String error = responseVerifyBody.getString("error");
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show());
            } else {
                String VerifyReturn = responseVerify.toString();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), VerifyReturn, Toast.LENGTH_LONG).show());
            }
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(currentImagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int orientation = Objects.requireNonNull(exifInterface).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(270);
                    break;
                default:
            }
            Bitmap rotate = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            profileImage.setImageBitmap(rotate);
            sendImage();
            }

    }

