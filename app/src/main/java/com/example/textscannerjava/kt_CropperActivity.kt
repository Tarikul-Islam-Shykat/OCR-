package com.example.textscannerjava

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*

class kt_CropperActivity : AppCompatActivity() {
    lateinit var  result: String
    lateinit var fileUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kt_cropper)

        readIntent() // getting the fileUri of image

        var desitation_uri : String = StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()


        val options = UCrop.Options()

        UCrop.of(fileUri, Uri.fromFile(File(cacheDir,desitation_uri)))
            .withOptions(options)
            .withAspectRatio(0F,0F)
            .useSourceImageAspectRatio()
            .withMaxResultSize(2000,2000)
            .start(this)
    }

    private fun readIntent() {
      var intent: Intent = intent
        if (intent.extras != null)
        {
            result = intent.getStringExtra("image_data_to_KT_crop_activity")!!
            fileUri = Uri.parse(result)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == UCrop.REQUEST_CROP &&  resultCode == RESULT_OK)
        {
            val resultUri = UCrop.getOutput(data!!)
            val returnIntent = Intent()
            returnIntent.putExtra("RESULT_FROM_KT_UCROP", resultUri.toString() + "")
            setResult(RESULT_OK, returnIntent)
            finish()
        } else {
            val cropError = UCrop.getError(data!!)
        }

    }


}