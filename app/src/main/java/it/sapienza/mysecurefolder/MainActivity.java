package it.sapienza.mysecurefolder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

   /*     String url = App.getBaseUrl() + "/list-group";

        Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient client = new OkHttpClient();
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                Log.d(TAG, "Risposta: " + response.body().string());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }*/

        //SERVE A VEDERE SE TROVIAMO LA CORRISPONDENZA DEL NOME NEL DB; IN CASO DI RIUSCITA DOVREMMO PASSARE AL RICONOSCIMENTO FACCIALE
        Button submit = findViewById(R.id.submitName);
        submit.setOnClickListener(v -> new Thread(() -> {
            EditText name = findViewById(R.id.insertName);
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("name", name.getText().toString())
                    .build();
            Request request = new Request.Builder()
                    .url(App.getBaseUrl() + "/verify-person")  //NON CONOSCO IL PATH SUL SERVER PER IL MATCH DEL NOME
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
        }).start());


    }
}
