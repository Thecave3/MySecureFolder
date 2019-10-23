package it.sapienza.mysecurefolder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;

import java.util.concurrent.ThreadPoolExecutor;

public class PopActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width= dm.widthPixels;
        int height= dm.heightPixels;

        getWindow().setLayout((int)(width*.9),(int)(height*.8));

        WindowManager.LayoutParams params= getWindow().getAttributes();
        params.gravity= Gravity.CENTER;
        params.x =0;
        params.y= -20;

        getWindow().setAttributes(params);
    }
}
