package it.sapienza.mysecurefolder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


public class RegistrationActivity extends AppCompatActivity {

    private final static String personGroupName = "users";
    TextView personNameTextField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        personNameTextField = findViewById(R.id.person_name);
    }

}
