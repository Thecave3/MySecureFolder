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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.exifinterface.media.ExifInterface;

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

public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = RegistrationActivity.class.getSimpleName();
    private static final int IMAGE_REQUEST = 1;
    private static final int RECORD_REQUEST = 2;
    private int records = 0;

    private static final int MY_CAMERA_PERMISSION_CODE = 128;


    Button photoButton, saveNameButton, audioButton, sendRecordButton;
    ImageView profileImage;
    EditText nameEditText;
    ProgressBar progressBar1, progressBar2, progressBar3;

    // Both the ID are taken during name registration and when they need to be passed singularly the field has to be personId
    String personIdFace, personIdAudio, currentImagePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        audioButton = findViewById(R.id.btnStartRecord);
        sendRecordButton = findViewById(R.id.btnSendRecord);
        profileImage = findViewById(R.id.imageView);
        photoButton = findViewById(R.id.buttonPhoto);
        saveNameButton = findViewById(R.id.buttonSave);
        nameEditText = findViewById(R.id.name);

        progressBar1 = findViewById(R.id.progress_bar1);
        progressBar2 = findViewById(R.id.progress_bar2);
        progressBar3 = findViewById(R.id.progress_bar3);

        photoButton.setEnabled(false);
        audioButton.setEnabled(false);
        saveNameButton.setEnabled(true);

        photoButton.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_CAMERA_PERMISSION_CODE);
            } else {
                takePicture();
            }
        });

        audioButton.setOnClickListener(view -> {
            recordEnrollment();
            records++;
        });

        saveNameButton.setOnClickListener(v -> {
            String username = String.valueOf(nameEditText.getText());
            if (TextUtils.isEmpty(username)) {
                nameEditText.setError("You did not enter a username");
                return;
            }
            nameEditText.setEnabled(false);
            createNewPerson(username);
            photoButton.setEnabled(true);
            saveNameButton.setEnabled(false);
        });

        sendRecordButton.setOnClickListener(view -> sendAudioForEnrollment());

        Spinner dropdown = findViewById(R.id.spinner1);
        String[] items = new String[]{
                "I am going to make him an offer he cannot refuse.",
                "Be yourself, anyone else is already taken.",
                "Houston we have had a problem.",
                "My voice is my passport verify me.",
                "Apple juice tastes funny after toothpaste.",
                "You can get in without your password.",
                "You can activate security system now.",
                "My voice is stronger than passwords.",
                "My password is not your business.",
                "My name is unknown to you."
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
    }


    /**
     * Take a picture in order to create the face features on server
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
                    audioButton.setEnabled(true);
                    photoButton.setEnabled(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * Records an enrollment in order to create the audio features on server
     */
    private void recordEnrollment() {
        String audioPath = "/" + records;
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

    /**
     * Create new person in the database
     *
     * @param username name of the user
     */
    private void createNewPerson(String username) {
        progressBar1.setVisibility(View.VISIBLE);
        saveNameButton.setVisibility(View.INVISIBLE);
        new Thread(() -> {
            RequestBody formBody = new FormBody.Builder()
                    .add("name", username)
                    .build();
            Request request = new Request.Builder()
                    .url(App.getBaseUrl() + "/create-new-person")
                    .post(formBody)
                    .build();
            try {
                Response response = App.getHTTPClient().newCall(request).execute();
                JSONObject responseBody = new JSONObject(Objects.requireNonNull(response.body()).string());
                if (responseBody.has("error")) {
                    String error = responseBody.getString("error");
                    runOnUiThread(() -> {
                        nameEditText.setEnabled(true);
                        saveNameButton.setEnabled(true);
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                        progressBar1.setVisibility(View.INVISIBLE);
                        saveNameButton.setVisibility(View.VISIBLE);
                    });
                } else {
                    if (responseBody.has("personIdFace") && responseBody.has("verificationProfileId")) {
                        personIdFace = responseBody.getString("personIdFace");
                        personIdAudio = responseBody.getString("verificationProfileId");
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "Name accepted", Toast.LENGTH_LONG).show();
                            progressBar1.setVisibility(View.INVISIBLE);
                            saveNameButton.setVisibility(View.VISIBLE);
                            saveNameButton.setEnabled(false);
                        });
                    } else {
                        String bodyStr = responseBody.toString();
                        Log.d(TAG, bodyStr);
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), bodyStr, Toast.LENGTH_LONG).show();
                            progressBar1.setVisibility(View.INVISIBLE);
                            saveNameButton.setVisibility(View.VISIBLE);
                        });
                    }
                    saveNameButton.setEnabled(false);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Sends the audio taken before to the server in order to enroll and extract the feature
     */
    public void sendAudioForEnrollment() {
        progressBar3.setVisibility(View.VISIBLE);
        sendRecordButton.setVisibility(View.INVISIBLE);
        //Send audio file to server
        new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                String audioPath = "/" + i;
                String externalFilePath = Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_MUSIC)).getPath();

                String filePath = externalFilePath + audioPath + ".wav";
                File audioFileToUpload = new File(filePath);

                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("audio", audioFileToUpload.getName(),
                                RequestBody.create(MediaType.parse("audio/wav"), audioFileToUpload))
                        .addFormDataPart("personId", personIdAudio)
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
                            progressBar3.setVisibility(View.INVISIBLE);
                            sendRecordButton.setVisibility(View.INVISIBLE);
                            sendRecordButton.setEnabled(false);
                            audioButton.setEnabled(true);
                            audioButton.setVisibility(View.VISIBLE);
                        });
                    } else {
                        String bodySt = responseBody.toString();
                        if (i == 2) {
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Registration completed", Toast.LENGTH_LONG).show());
                            finish();
                        } else {
                            int step = i + 1;
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Audio n." + step + " processed", Toast.LENGTH_LONG).show());
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Sends the picture taken before to the server in order to extract feature
     */
    private void sendImage() {
        progressBar2.setVisibility(View.VISIBLE);
        photoButton.setVisibility(View.INVISIBLE);
        new Thread(() -> {
            File fileToUpload = new File(currentImagePath);
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("face", fileToUpload.getName(),
                            RequestBody.create(MediaType.parse("image/jpeg"), fileToUpload))
                    .addFormDataPart("personId", personIdFace)
                    .build();

            Request request = new Request.Builder()
                    .url(App.getBaseUrl() + "/add-face")
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
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                        progressBar2.setVisibility(View.INVISIBLE);
                        photoButton.setVisibility(View.VISIBLE);
                        photoButton.setEnabled(true);
                    });
                } else if (responseBody.has("persistedFaceId")) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Face registered!", Toast.LENGTH_LONG).show();
                        progressBar2.setVisibility(View.INVISIBLE);
                        photoButton.setVisibility(View.VISIBLE);
                    });
                } else {
                    runOnUiThread(() -> {
                        progressBar2.setVisibility(View.INVISIBLE);
                        photoButton.setVisibility(View.VISIBLE);
                        photoButton.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Please try again", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Take the picture from the external storage, used just to save the picture locally before sending it
     *
     * @return file representing the picture taken before
     * @throws IOException if the file is not found or if the external folder does not exists
     */
    private File getImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALIAN).format(new Date());
        String imageName = "jpeg_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        currentImagePath = imageFile.getAbsolutePath();
        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RECORD_REQUEST: {
                if (resultCode == RESULT_OK) {
                    if (records < 3) {
                        recordEnrollment();
                        records++;
                    } else {
                        records = 0;
                    }

                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(TAG, "onActivityResult: User has canceled the recording");
                }

                Log.d(TAG, "onActivityResult: User has recorded and saved the audio files");
                audioButton.setVisibility(View.INVISIBLE);
                sendRecordButton.setVisibility(View.VISIBLE);
                break;
            }

            case IMAGE_REQUEST: {
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
                break;
            }

            default:
                Log.e(TAG, "onActivityResult: boh");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted.", Toast.LENGTH_LONG).show();
                takePicture();
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_LONG).show();
            }
        }
    }


}