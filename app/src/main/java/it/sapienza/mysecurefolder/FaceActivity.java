package it.sapienza.mysecurefolder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import it.sapienza.mysecurefolder.user.User;

public class FaceActivity extends AppCompatActivity {

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        user = (User) getIntent().getSerializableExtra("user");
    }
}
