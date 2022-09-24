package com.example.textscannerjava;

import static android.Manifest.permission.CAMERA;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.TestLooperManager;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.textscannerjava.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.yalantis.ucrop.UCrop;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding am_bind;
    private  static final int REQUEST_CAMERA_CODE = 1;
    private Bitmap imageBitmap; // A bitmap is a type of memory organization or image file format used to store digital images.
    int SELECT_IMAGE_CODE = 2;
    TextToSpeech t1;
    StringBuilder result_2;
    ActivityResultLauncher<String> mGetContent;
    StringBuilder final_builder = new StringBuilder();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        am_bind = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(am_bind.getRoot());

        getSupportActionBar().setTitle("OCR [JAVA]");

        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {  // only initialize the language that you want speech
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR)
                {
                    t1.setLanguage(Locale.ENGLISH);
                }
            }
        });

        am_bind.btnCapture.setOnClickListener(new View.OnClickListener() { // for opening camera, if no permission given asks permission
            @Override
            public void onClick(View v) {
                if(checkPermissions())
                {   openCamera();   }

                else
                {   requestPersmisson();    }
            }
        });

        // copy button for copying text
        am_bind.btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", am_bind.txtResult.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();
            }
        });


        am_bind.btnDetectText.setOnClickListener(new View.OnClickListener() { // go to function for detecting text in picture,
            @Override
            public void onClick(View v) {
                try {  // avoided errors like no image / no text in the image /  no text detected
                   detectText();
                }
                catch (Exception e)
                {
                    Toast.makeText(MainActivity.this, "No Text Detected", Toast.LENGTH_SHORT).show();
                }
            }
        });


        am_bind.btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "title"), SELECT_IMAGE_CODE);
            }
        }); // for getting  the image from gallery


        am_bind.floatTextToSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = am_bind.txtResult.getText().toString();
                t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }); // this will turn on the speaker in english, previous method was just for initialize the english language


        am_bind.floatingCropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });


        // for openning the gallery
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                Intent intent = new Intent(MainActivity.this, CropperActivity.class);
                intent.putExtra("image_data_to_crop_activity", result.toString());
                startActivityForResult(intent, 101);
            }
        }); // sending the selected picture from open gallery for corpping
    }


    private  boolean checkPermissions(){
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return  cameraPermission == PackageManager.PERMISSION_GRANTED;
    } // for checking camera permission


    private void requestPersmisson()
    {
        int PERMISSION_CODE= 200;
        ActivityCompat.requestPermissions(this, new  String[]{CAMERA}, PERMISSION_CODE);
    } // if the permission is denied again ask for permission


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            am_bind.imgCapute.setImageBitmap(imageBitmap);
        }

        if(requestCode == SELECT_IMAGE_CODE && resultCode == RESULT_OK)
        {
            Uri uri =data.getData(); //we get the image in uri formate.
            am_bind.imgCapute.setImageURI(uri);
            Bundle extras = data.getExtras();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(resultCode == RESULT_OK && requestCode == 101)
        {
            String result =  data.getStringExtra("RESULT_FROM_UCROP");
            Uri resultUri = null;

            if(result != null)
            {
                resultUri = Uri.parse(result);
            }
            am_bind.imgCapute.setImageURI(resultUri);
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } //


    // openCamera
    private void openCamera(){
        Toast.makeText(this, "opennning", Toast.LENGTH_SHORT).show();
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture. resolveActivity(getPackageManager())!= null)
        {
            startActivityForResult(takePicture, REQUEST_CAMERA_CODE);
        }
    } // open the camera


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length> 0)
        {
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if(cameraPermission)
            {
                Toast.makeText(getApplicationContext(), "Permission is granted", Toast.LENGTH_SHORT).show();
                openCamera();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Permission is denied", Toast.LENGTH_SHORT).show();
                requestPersmisson();
            }
        }
    }  // checking wheather the user granted the permission or not


    public void detectText() {
        result_2 = new StringBuilder();

        InputImage image = InputImage.fromBitmap(imageBitmap, 0); // Creates an InputImage from a Bitmap.
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                for(Text.TextBlock block: text.getTextBlocks()){
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for (Text.Line line: block.getLines())
                    {
                        String lineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();

                        for(Text.Element element : line.getElements())
                        {
                            String elementText = element.getText();
                           // Toast.makeText(MainActivity.this, elementText, Toast.LENGTH_SHORT).show();
                            result_2.append(elementText+" ");
                        }
                    }
                }
                final_builder.append(result_2);
                returnText(final_builder);
               //Toast.makeText(MainActivity.this, "here :" +final_builder, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void returnText(StringBuilder result_t)
    {
        Toast.makeText(this, result_t, Toast.LENGTH_SHORT).show();
        am_bind.txtResult.setText(result_t);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.item_3:
                startActivity(new Intent(MainActivity.this, kotlin_main_activity.class));
            case R.id.item_2:
                Toast.makeText(this, "Java Version", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}