package it.sapienza.mysecurefolder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static okhttp3.internal.http.HttpDate.format;


public class RegistrationActivity extends AppCompatActivity {

    String currentImagePath = null;
    private static final int IMAGE_REQUEST = 1;
    Button fotoButton;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        imageView = findViewById(R.id.mimageView);
        fotoButton = findViewById(R.id.bottoneFoto);
        fotoButton.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(cameraIntent.resolveActivity(getPackageManager())!=null){
                File imageFile = null;
                try {
                    imageFile=getImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(imageFile!=null){
                    Uri imageUri  = FileProvider.getUriForFile(getApplicationContext(),"com.example.android.fileprovider",imageFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                    startActivityForResult(cameraIntent,IMAGE_REQUEST);
                }
            }
        });


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
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
        Bitmap rotate = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        imageView.setImageBitmap(rotate);
    }



    private File getImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.ITALIAN).format(new Date());
        String imageName = "jpeg_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageName,".jpg",storageDir);
        currentImagePath = imageFile.getAbsolutePath();
        return imageFile;
    }

}



