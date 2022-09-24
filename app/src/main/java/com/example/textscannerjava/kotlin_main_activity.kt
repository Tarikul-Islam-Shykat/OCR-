package com.example.textscannerjava

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.textscannerjava.databinding.ActivityKotlinMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException
import java.util.*

class kotlin_main_activity : AppCompatActivity() {

    lateinit var textToSpeech: TextToSpeech
    private  lateinit var  binding: ActivityKotlinMainBinding
    lateinit var imageBitmap: Bitmap //The “lateinit” keyword in Kotlin as the name suggests is used to declare those variables that are guaranteed to be initialized in the future. “lateinit” variable:

    private val REQUEST_CAMERA_CODE = 1
    private val SELECT_IMAGE_CODE = 2

    lateinit var mGetContent: ActivityResultLauncher<String>

    var final_builder = java.lang.StringBuilder()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKotlinMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.title = "OCR [Kotlin]"

        textToSpeech = TextToSpeech(this) { status -> // only initialize the language that you want speech
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.ENGLISH)
            }
        }

        binding.ktBtnCapture.setOnClickListener {
            if(checkPermissions()) openCamera()
            else requestPermission()
        }

        binding.ktBtnGallery.setOnClickListener {
            var intent: Intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent,"title"), SELECT_IMAGE_CODE)
        }

        binding.ktFloatTextToSpeech.setOnClickListener{
            var text: String = binding.ktTxtResult.text.toString()
            textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null)
        }

      binding.ktFloatingCropBtn.setOnClickListener {
          mGetContent.launch("image/*")
      }


        // for openning the gallery
        mGetContent = registerForActivityResult<String, Uri>(
            ActivityResultContracts.GetContent()
        ) { result ->
            val intent = Intent(this, kt_CropperActivity::class.java)
            intent.putExtra("image_data_to_KT_crop_activity", result.toString())
            startActivityForResult(intent, 101)
        }
    }

    private fun checkPermissions() : Boolean {
       var cameraPermission :Int = ContextCompat.checkSelfPermission(this, CAMERA)
        return  cameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun openCamera()
    {
        var takePicture: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if(takePicture.resolveActivity(packageManager) != null)
        {
            startActivityForResult(takePicture,REQUEST_CAMERA_CODE);
        }
    }

    private fun requestPermission()
    {
        var PERMISSION_CODE = 200
        ActivityCompat.requestPermissions(this, arrayOf(CAMERA), PERMISSION_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK){
            val extras = data!!.extras
            imageBitmap = extras!!.get("data") as Bitmap
            binding.ktImgCapute.setImageBitmap(imageBitmap)
        }

        if(requestCode == SELECT_IMAGE_CODE && resultCode == RESULT_OK)
        {
            val uri:Uri = data?.data!!
            binding.ktImgCapute.setImageURI(uri)

            val extras = data!!.extras
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,uri)
            }
             catch (e:IOException) {
                 e.printStackTrace()
             }
        }

        if(requestCode == 101 && resultCode == RESULT_OK)
        {
            var result = data?.getStringExtra("RESULT_FROM_KT_UCROP")

            var resultUri: Uri? = null

            if(resultUri != null)
            {
                resultUri = Uri.parse(result)
            }
            binding.ktImgCapute.setImageURI(resultUri)

            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,resultUri)
            }
            catch (e:IOException) {
                e.printStackTrace()
            }
        }
    }

    fun detectText(): StringBuilder? {
        val result_2 = StringBuilder()
        val image = InputImage.fromBitmap(imageBitmap, 0) // Creates an InputImage from a Bitmap.
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = recognizer.process(image).addOnSuccessListener { text ->
            for (block in text.textBlocks) {
                val blockText = block.text
                val blockCornerPoint = block.cornerPoints
                val blockFrame = block.boundingBox
                for (line in block.lines) {
                    val lineText = line.text
                    val lineCornerPoint = line.cornerPoints
                    val lineRect = line.boundingBox
                    for (element in line.elements) {
                        val elementText = element.text
                        result_2.append("$elementText ")
                    }
                }
            }
            final_builder.append(result_2)
            returnText(final_builder)
        }.addOnFailureListener {
            Toast.makeText(
                this,
                "Failed to detect",
                Toast.LENGTH_SHORT
            ).show()
        }
        Toast.makeText(this, result_2, Toast.LENGTH_SHORT).show()
        return result_2
    }

    fun returnText(result_t: java.lang.StringBuilder?) {
        Toast.makeText(this, result_t, Toast.LENGTH_SHORT).show()
        binding.ktTxtResult.setText(result_t)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_2 -> {
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }
            R.id.item_3 -> Toast.makeText(this, "Kotlin Version", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }
}