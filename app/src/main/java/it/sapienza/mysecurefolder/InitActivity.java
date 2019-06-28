package it.sapienza.mysecurefolder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InitActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        Button signup = findViewById(R.id.signupActivity);
        Button login = findViewById(R.id.loginActivity);
        signup.setOnClickListener(v -> startActivity(new Intent(InitActivity.this, RegistrationActivity.class)));
        login.setOnClickListener(v -> startActivity(new Intent(InitActivity.this, FaceActivity.class)));

    }
}
