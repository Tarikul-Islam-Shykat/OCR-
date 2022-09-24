package com.example.textscannerjava;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.UUID;

public class CropperActivity extends AppCompatActivity {

    String result ;
    Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper);
        Toast.makeText(this, "Cropper Activity", Toast.LENGTH_SHORT).show();
        readIntent();

        String destination_uri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();

        UCrop.Options options = new UCrop.Options();
       // options.setCircleDimmedLayer(true); // this is for Circular Image Cropper.

        UCrop.of(fileUri,Uri.fromFile(new File(getCacheDir(), destination_uri)))
                .withOptions(options)
                .withAspectRatio(0,0) // automatically select from your screen image size., 0,0 will provide all type of cropper.
                .useSourceImageAspectRatio()
                .withMaxResultSize(2000,2000)// for maximum quality
                .start(CropperActivity.this);

    }

    private void readIntent() {
        Intent intent = getIntent();

        if(intent.getExtras() != null)
        {
            result = intent.getStringExtra("image_data_to_crop_activity");
            fileUri = Uri.parse(result);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK  && requestCode == UCrop.REQUEST_CROP)
        {
            final  Uri  resultUri = UCrop.getOutput(data);
            Intent returnIntent = new Intent();
            returnIntent.putExtra("RESULT_FROM_UCROP", resultUri+"");
            setResult(RESULT_OK, returnIntent);
            finish();
        }
        else{
            final Throwable cropError = UCrop.getError(data);
        }
    }

   /* @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(CropperActivity.this, MainActivity.class));
        finish();
    }*/
}