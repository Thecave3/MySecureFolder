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
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.exifinterface.media.ExifInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import it.sapienza.mysecurefolder.user.User;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FaceActivity extends AppCompatActivity {

    private User user;
    private static final String TAG = CredentialsActivity.class.getSimpleName();
    private static final int IMAGE_REQUEST = 1;
    private ImageView profileImage;
    private Button photoButton;
    private static final int MY_CAMERA_PERMISSION_CODE = 128;
    private String currentImagePath;
    private ProgressBar progressBar;
    private FaceActivity self;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        user = (User) getIntent().getSerializableExtra("user");
        photoButton = findViewById(R.id.buttonPhoto);
        profileImage = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progress_bar);

        photoButton.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_CAMERA_PERMISSION_CODE);
            } else {
                takePicture();
            }
        });

        self = this;

    }

    /**
     * Take a picture in order to send it to the server
     */
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

    /**
     * Sends a picture in order to detect the facial features
     * If a face is detected than the flow goes on to verify the picture with the person
     */
    private void sendImage() {
        new Thread(() -> {
            photoButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            File fileToUpload = new File(currentImagePath);
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("face", fileToUpload.getName(),
                            RequestBody.create(MediaType.parse("image/jpeg"), fileToUpload))
                    .addFormDataPart("recognitionModel", "recognition_02")
                    .build();

            Request request = new Request.Builder()
                    .url(App.getBaseUrl() + "/detect")
                    .post(requestBody)
                    .build();

            final Runnable please_try_again = () -> {
                Toast.makeText(getApplicationContext(), "Please try again", Toast.LENGTH_LONG).show();
                photoButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            };
            try {
                Response response = App.getHTTPClient().newCall(request).execute();
                assert response.body() != null;
                String resBody = response.body().string();
                Log.d(TAG, resBody);
                JSONArray responseArray = new JSONArray(resBody);
                if (responseArray.length() > 0) {
                    JSONObject responseBody = responseArray.getJSONObject(0);
                    if (responseBody.has("error")) {
                        String error = responseBody.getString("error");
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                            photoButton.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                        });
                    } else {
                        //String bodySt = responseBody.toString();
                        //runOnUiThread(() -> Toast.makeText(getApplicationContext(), bodySt, Toast.LENGTH_LONG).show());
                        verify(responseBody.getString("faceId"));
                    }
                } else {
                    runOnUiThread(please_try_again);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(please_try_again);
            }

        }).start();
    }

    /**
     * Verify the correspondence between user and picture
     *
     * @param faceId id representing the picture
     */
    private void verify(String faceId) {
        JSONObject body = new JSONObject();
        try {
            body.put("faceId", faceId);
            body.put("personId", user.getFaceId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody verifyBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), body.toString());

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
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    photoButton.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                });

            } else if (responseVerifyBody.has("isIdentical") && responseVerifyBody.getBoolean("isIdentical")) {
                Intent voiceIntent = new Intent(FaceActivity.this, VoiceActivity.class);
                voiceIntent.putExtra("user", user);
                startActivity(voiceIntent);
                self.finish();
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Please try again, response: " + responseVerify, Toast.LENGTH_LONG).show();
                    photoButton.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                });
            }
        } catch (IOException | JSONException e) {
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

