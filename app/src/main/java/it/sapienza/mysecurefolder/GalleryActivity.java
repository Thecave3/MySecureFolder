package it.sapienza.mysecurefolder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

import it.sapienza.mysecurefolder.user.User;

public class GalleryActivity extends AppCompatActivity {
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        //user = (User) getIntent().getSerializableExtra("user");

        GridView imageGrid = findViewById(R.id.gridview);
        ArrayList<Bitmap> bitmapList = new ArrayList<>();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

//        bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.face_1, options));
//        bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.male, options));
//        bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.tre, options));
//        bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.quattro, options));
//        bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.cinque, options));

        imageGrid.setAdapter(new ImageAdapter(this, bitmapList));
    }
}


class ImageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Bitmap> bitmapList;

    ImageAdapter(Context context, ArrayList<Bitmap> bitmapList) {
        this.context = context;
        this.bitmapList = bitmapList;
    }

    @Override
    public int getCount() {
        return this.bitmapList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(115, 115));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(this.bitmapList.get(position));
        return imageView;
    }
}