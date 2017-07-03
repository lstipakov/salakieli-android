package fi.stipakov.salakieli;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final static String LOG_TAG = "MainActivity";
    private ImageView iv;

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(LOG_TAG,"Permission is granted");
                return true;
            } else {

                Log.v(LOG_TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(LOG_TAG,"Permission is granted");
            return true;
        }
    }

    void loadImage(CharSequence s) {
        ImageView iv = (ImageView) findViewById(R.id.imageView);

        String uri = Uri.parse("http://stipakov.fi:8000/salakieli")
                .buildUpon()
                .appendQueryParameter("text", s.toString())
                .build().toString();
        Picasso.with(getApplicationContext()).
                load(uri).into(iv);
    }

    void shareImage() {

        File sdCard = Environment.getExternalStorageDirectory();
        try {
            File file = File.createTempFile("file", "*.jpg", sdCard);

            FileOutputStream fos = new FileOutputStream(file);

            iv.setDrawingCacheEnabled(true);
            iv.buildDrawingCache(true);

            Bitmap b = iv.getDrawingCache();
            b.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            startActivity(Intent.createChooser(shareIntent, "Share image using"));

            fos.close();

            iv.setDrawingCacheEnabled(false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isStoragePermissionGranted();

        iv = (ImageView) findViewById(R.id.imageView);

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage();
            }
        });

        EditText et = (EditText)findViewById(R.id.editText);

        loadImage(et.getText());

        et.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                loadImage(s);
            }
        });
    }
}
