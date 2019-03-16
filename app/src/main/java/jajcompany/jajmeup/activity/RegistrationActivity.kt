package jajcompany.jajmeup.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import jajcompany.jajmeup.R
import jajcompany.jajmeup.glide.GlideApp
import jajcompany.jajmeup.utils.FireStore
import jajcompany.jajmeup.utils.Jajinternet
import jajcompany.jajmeup.utils.StorageUtil
import kotlinx.android.synthetic.main.registration_layout.*
import java.io.ByteArrayOutputStream


class RegistrationActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private val RC_SELECT_IMAGE = 2
    private var selectedImageBytes: ByteArray = byteArrayOf()
    private var pictureJustChanged = false
    private var flagRegistration = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_layout)
        mAuth = FirebaseAuth.getInstance()
        profilePictureRegistration.setOnClickListener {
            if (Jajinternet.getStatusInternet(this)) {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }
                startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)
            }
            else {
                Toast.makeText(this, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
            }
        }
        registrationButtonRegister.setOnClickListener {
            if (Jajinternet.getStatusInternet(this)) {
                val usermail: String = emailRegistration.text.toString()
                val userpseudo: String = pseudoRegistration.text.toString()
                val password: String = passwordRegistration.text.toString()
                val passwordconfirm: String = passwordRegistrationConfirm.text.toString()
                if (password != passwordconfirm || password.replace("\\s".toRegex(), "") == "") {
                    Toast.makeText(this, getString(R.string.mot_de_passe_differents), Toast.LENGTH_LONG).show()
                } else if (userpseudo.replace("\\s".toRegex(), "") == "") {
                    Toast.makeText(this, getString(R.string.pseudo_incorrect), Toast.LENGTH_LONG).show()
                } else if (selectedImageBytes.isEmpty()) {
                    Toast.makeText(this, getString(R.string.ajout_photo), Toast.LENGTH_LONG).show()
                } else if (!Patterns.EMAIL_ADDRESS.matcher(usermail).matches() || usermail.replace("\\s".toRegex(), "") == "") {
                    Toast.makeText(this, getString(R.string.courriel_incorrect), Toast.LENGTH_LONG).show()
                } else {
                    if (!flagRegistration) {
                        mAuth?.createUserWithEmailAndPassword(usermail, password)
                                ?.addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        flagRegistration = true
                                        FireStore.checkIfUsername(userpseudo) {
                                            Log.d("RegistrationActivity", "createUserWithEmail:success")
                                            if (it == "NOTEXIST") {
                                                StorageUtil.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                                                    if (imagePath == "ERROR") {
                                                        Toast.makeText(this, getString(R.string.erreur_inscription),
                                                                Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        FireStore.initCurrentUser(userpseudo, imagePath) {resultinit ->
                                                            if (resultinit == "ERROR") {
                                                                Toast.makeText(this, getString(R.string.erreur_inscription),
                                                                        Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                startActivity(PrincipalActivity.newIntent(this))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            else {
                                                Toast.makeText(this, getString(R.string.erreur_user_existe),
                                                        Toast.LENGTH_SHORT).show()
                                                mAuth?.signOut()
                                            }
                                        }
                                    } else {
                                        Log.w("RegistrationActivity", "createUserWithEmail:failure", task.exception)
                                        Toast.makeText(this, getString(R.string.erreur_inscription),
                                                Toast.LENGTH_SHORT).show()
                                    }
                                }
                    }
                    else {
                        mAuth?.signInWithEmailAndPassword(usermail, password)
                                ?.addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        FireStore.checkIfUsername(userpseudo) {
                                            if (it == "NOTEXIST") {
                                                flagRegistration = true
                                                Log.d("RegistrationActivity", "createUserWithEmail:success")
                                                StorageUtil.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                                                    if (imagePath == "ERROR") {
                                                        Toast.makeText(this, getString(R.string.erreur_inscription),
                                                                Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        FireStore.initCurrentUser(userpseudo, imagePath) {
                                                            if (it == "ERROR") {
                                                                Toast.makeText(this, getString(R.string.erreur_inscription),
                                                                        Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                startActivity(PrincipalActivity.newIntent(this))
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(this, getString(R.string.erreur_user_existe),
                                                        Toast.LENGTH_SHORT).show()
                                            }

                                        }
                                    } else {
                                        Log.w("RegistrationActivity", "createUserWithEmail:failure", task.exception)
                                        Toast.makeText(this, getString(R.string.erreur_inscription),
                                                Toast.LENGTH_SHORT).show()
                                    }
                        }
                    }
                }
            }
            else {
                Toast.makeText(this, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
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