package it.sapienza.mysecurefolder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class InitActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        Button signButton = findViewById(R.id.signupActivity);
        Button login = findViewById(R.id.loginActivity);
        signButton.setOnClickListener(v -> startActivity(new Intent(InitActivity.this, RegistrationActivity.class)));
        login.setOnClickListener(v -> startActivity(new Intent(InitActivity.this, CredentialsActivity.class)));
        // used just to test gallery activity, don't uncomment
        Intent galleryIntent = new Intent(InitActivity.this, GalleryActivity.class);
        startActivity(galleryIntent);
    }
}
