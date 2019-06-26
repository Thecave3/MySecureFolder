package it.sapienza.mysecurefolder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.microsoft.cognitive.speakerrecognition.SpeakerIdentificationRestClient;
import com.microsoft.cognitive.speakerrecognition.SpeakerVerificationClient;
import com.microsoft.cognitive.speakerrecognition.SpeakerVerificationRestClient;
import com.microsoft.cognitive.speakerrecognition.contract.verification.Profile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class VoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        Properties properties = new Properties();
        InputStream inputStream =
                Objects.requireNonNull(this.getClass().getClassLoader()).getResourceAsStream("email.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SpeakerVerificationClient client = new SpeakerVerificationRestClient(properties.getProperty("apikey.voice.key1"));
        //Profile profile = client.getProfile("");

        // enrollment : registration
        // recognition

    }
}
