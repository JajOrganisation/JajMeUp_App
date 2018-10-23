package jajcompany.jajmeup.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import jajcompany.jajmeup.R
import android.util.Log
import com.bumptech.glide.request.RequestOptions
import jajcompany.jajmeup.MainActivity
import kotlinx.android.synthetic.main.registration_layout.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import jajcompany.jajmeup.Models.User
import jajcompany.jajmeup.Utils.FireStore
import com.google.firebase.auth.UserProfileChangeRequest
import jajcompany.jajmeup.Utils.StorageUtil
import jajcompany.jajmeup.glide.GlideApp
import kotlinx.android.synthetic.main.profilepicturesettings_layout.*
import java.io.ByteArrayOutputStream


class RegistrationActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private val RC_SELECT_IMAGE = 2
    private var selectedImageBytes: ByteArray = byteArrayOf()
    private var pictureJustChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_layout)
        mAuth = FirebaseAuth.getInstance()
        profilePictureRegistration.setOnClickListener {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }
            startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)
        }
        registrationButtonRegister.setOnClickListener {
            val usermail: String = emailRegistration.text.toString()
            val userpseudo: String = pseudoRegistration.text.toString()
            val password: String = passwordRegistration.text.toString()
            val passwordconfirm: String = passwordRegistrationConfirm.text.toString()
            if (password != passwordconfirm) {
                Toast.makeText(this, "Mots de passe differents", Toast.LENGTH_LONG).show()
            }
            else if (selectedImageBytes.isEmpty()) {
                Toast.makeText(this, "Ajoute une photo", Toast.LENGTH_LONG).show()
            }
            else {
                mAuth?.createUserWithEmailAndPassword(usermail, password)
                        ?.addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Log.d("RegistrationActivity", "createUserWithEmail:success")
                                StorageUtil.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                                    sharedPreferences.edit().putString("profilepicture_link", imagePath).apply()
                                    FireStore.initCurrentUser(userpseudo, imagePath) {
                                        startActivity(MainActivity.newIntent(this))
                                    }
                                }
                            } else {
                                Log.w("RegistrationActivity", "createUserWithEmail:failure", task.exception)
                                Toast.makeText(this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show()
                            }
                        }
            }
        }
    }
    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, RegistrationActivity::class.java)
            return intent
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
                data != null && data.data != null) {
            val selectedImagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media
                    .getBitmap(contentResolver, selectedImagePath)

            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectedImageBytes = outputStream.toByteArray()

            GlideApp.with(this)
                    .load(selectedImageBytes)
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profilePictureRegistration)

            pictureJustChanged = true
        }
    }
}