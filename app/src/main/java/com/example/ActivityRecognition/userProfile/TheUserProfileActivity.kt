package com.example.ActivityRecognition.userProfile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.ActivityRecognition.R
import com.example.ActivityRecognition.Util
import java.io.File
import java.io.FileOutputStream

// copy from the user interface (myruns1)
class TheUserProfileActivity : AppCompatActivity() {
    // variable for change button
    private lateinit var imageView: ImageView

    private lateinit var tempImgUri: Uri
    private lateinit var imgUri: Uri
    private val tempImgFileName = "tempImg.jpg"
    private val imgFileName = "image.jpg"
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var albumResult: ActivityResultLauncher<Intent>

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_the_user_profile)

        title = "MyRuns2-TheUserProfile"
        // ask user for permission of camera
        Util.checkPermissions(this)

        // load data saved when open app
        val view: View = findViewById(R.id.info);
        onLoad(view)

        imageView = findViewById(R.id.imageProfile)

        val tempImgFile = File(getExternalFilesDir(null), tempImgFileName)
        val imgFile = File(getExternalFilesDir(null), imgFileName)
        tempImgUri = FileProvider.getUriForFile(this, "com.example.theDatabase", tempImgFile)
        imgUri =  FileProvider.getUriForFile(this, "com.example.theDatabase", imgFile)

        if(imgFile.exists()) {
            val bitmap = Util.getBitmap(this, imgUri)
            var bitmapRotate = rotateBimap(this, -90, bitmap)
            imageView.setImageBitmap(bitmapRotate)
        }

        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                // here rotate for -90 degrees aas the image saved is 90 degrees rotated
                val bitmap = Util.getBitmap(this, tempImgUri)
                var bitmapRotate = rotateBimap(this, -90, bitmap)
                imageView.setImageBitmap(bitmapRotate)
            }
        }

        albumResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                val albumUrl : Uri = data?.data!!
                compressImg(this, File(getExternalFilesDir(null), tempImgFileName), albumUrl)
                // here rotate for -90 degrees aas the image saved is 90 degrees rotated
                val bitmap = Util.getBitmap(this, albumUrl)
                var bitmapRotate = rotateBimap(this, -90, bitmap)

                imageView.setImageBitmap(bitmapRotate)
            }
        }
    }
    // about how to use setItem
    // reference: https://stackoverflow.com/questions/52532679/how-to-make-alert-dialog-items
    // change button action
    fun onChangeButtonClicked(view: View) {
        // set up a alertdialog for user to choose take a photo or from album
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Profile Picture")
        builder.setItems(arrayOf("Open Camera", "Select from Gallery")) { _, pos ->
            when(pos){
                0->{
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
                    cameraResult.launch(intent)
                }
                else->{
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    albumResult.launch(intent)
                }
            }

        }
        builder.create().show()
    }
    // here use sharedpreferences to save and reload data after app is destroyed
    // reference: https://blog.csdn.net/MR_Condingson/article/details/58586419?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522166361683416781432921475%2522%252C%2522scm%2522%253A%252220140713.130102334..%2522%257D&request_id=166361683416781432921475&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~all~top_positive~default-1-58586419-null-null.142^v47^pc_rank_34_1,201^v3^add_ask&utm_term=sharedpreferences%E7%94%A8%E6%B3%95&spm=1018.2226.3001.4187
    fun onSaveButtonClicked(view: View) {

        if(File(getExternalFilesDir(null), tempImgFileName).exists()){
            val tempImgFile = File(getExternalFilesDir(null), tempImgFileName)
            val imgFile = File(getExternalFilesDir(null), imgFileName)
            tempImgFile.renameTo(imgFile)
        }
        val sp: SharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sp.edit()

        val name: EditText = findViewById(R.id.edit_message_name)
        editor.putString("Name", name.text.toString().trim())
        val email: EditText = findViewById(R.id.edit_message_email)
        editor.putString("Email", email.text.toString().trim())
        val phone: EditText = findViewById(R.id.edit_message_phone)
        editor.putString("Phone", phone.text.toString().trim())
        val gender: RadioGroup = findViewById(R.id.edit_message_gender)
        editor.putInt("Gender", gender.checkedRadioButtonId)
        val classNum: EditText = findViewById(R.id.edit_message_class)
        editor.putString("ClassNum", classNum.text.toString().trim())
        val major: EditText = findViewById(R.id.edit_message_major)
        editor.putString("Major", major.text.toString().trim())

        editor.apply()
        Toast.makeText(applicationContext, "saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    fun onCancelButtonClicked(view: View) {
        onLoad(view)
        val tempImgFile = File(getExternalFilesDir(null), tempImgFileName)
        tempImgFile.delete()
        val sp: SharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sp.edit()

        editor.apply();
        finish()
    }

    fun onLoad(view: View) {
        val sp: SharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE)

        val name: EditText = findViewById(R.id.edit_message_name)
        name.setText(sp.getString("Name", ""))
        val email: EditText = findViewById(R.id.edit_message_email)
        email.setText(sp.getString("Email", ""))
        val phone: EditText = findViewById(R.id.edit_message_phone)
        phone.setText(sp.getString("Phone", ""))
        val gender: RadioGroup = findViewById(R.id.edit_message_gender)
        gender.check(sp.getInt("Gender", 0))
        val classNum: EditText = findViewById(R.id.edit_message_class)
        classNum.setText(sp.getString("ClassNum", ""))
        val major: EditText = findViewById(R.id.edit_message_major)
        major.setText(sp.getString("Major", ""))
    }

    // function to rotate bitmap
    // reference: https://blog.csdn.net/u012246458/article/details/82147460?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522166362183216800186543713%2522%252C%2522scm%2522%253A%252220140713.130102334.pc%255Fall.%2522%257D&request_id=166362183216800186543713&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~all~first_rank_ecpm_v1~pc_rank_34-2-82147460-null-null.142^v47^pc_rank_34_1,201^v3^add_ask&utm_term=android%20studio%20%E6%A0%B9%E6%8D%AEuri%E8%8E%B7%E5%8F%96%E5%9B%BE%E5%83%8F%E6%97%8B%E8%BD%AC%E8%A7%92%E5%BA%A6&spm=1018.2226.3001.4187
    private fun rotateBimap(context: Context, degree: Int, srcBitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.reset()
        matrix.setRotate(degree.toFloat())
        return Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.width, srcBitmap.height, matrix, true)
    }

    // action for menu
    // to show the button on the right top corner
    // reference: https://blog.csdn.net/qq_40246759/article/details/112278716?spm=1001.2101.3001.6650.2&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-2-112278716-blog-109692704.pc_relevant_default&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-2-112278716-blog-109692704.pc_relevant_default&utm_relevant_index=4
    // create button
        override fun onCreateOptionsMenu(menu: Menu): Boolean {
            menu.add(Menu.NONE, 1, 1, "setting")
            return true
        }
    // add refection after selected option
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> Toast.makeText(applicationContext, "setting", Toast.LENGTH_SHORT).show()
        }
        return true
    }
    // compress img
    // reference: https://blog.csdn.net/chaseDreamer_/article/details/121207390?ops_request_misc=&request_id=&biz_id=102&utm_term=compress%20kotlin&utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-1-121207390.142^v51^pc_rank_34_2,201^v3^control_1&spm=1018.2226.3001.4187
    fun compressImg(context: Context, tempImaFile : File,  tempImgUri: Uri){
        var fileOutputStream  = FileOutputStream(tempImaFile)
        var bitmapTemp = BitmapFactory.decodeStream(contentResolver.openInputStream(tempImgUri))
        bitmapTemp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
    }


}