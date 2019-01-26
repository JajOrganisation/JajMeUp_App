package jajcompany.jajmeup.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.PreferenceScreen
import android.provider.MediaStore
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import jajcompany.jajmeup.R
import jajcompany.jajmeup.glide.GlideApp
import jajcompany.jajmeup.utils.FireStore
import jajcompany.jajmeup.utils.StorageUtil
import kotlinx.android.synthetic.main.profilepicturesettings_layout.*
import java.io.ByteArrayOutputStream


class SettingsActivity : AppCompatActivity() {
    private val RCSELECTIMAGE = 2
    private lateinit var selectedImageBytes: ByteArray
    private var pictureJustChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profilepicturesettings_layout)
        setTheme(R.style.PreferencesTheme)
        profilePictureSettings.setOnClickListener {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }
            startActivityForResult(Intent.createChooser(intent, "Select Image"), RCSELECTIMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RCSELECTIMAGE && resultCode == Activity.RESULT_OK &&
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
                    .into(profilePictureSettings)

            pictureJustChanged = true
            StorageUtil.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                FireStore.updateCurrentUser(profilePicture = imagePath)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        FireStore.getCurrentUser { user ->
                if (!pictureJustChanged && user.profilePicture != null) {
                    GlideApp.with(this)
                            .load(StorageUtil.pathToReference(user.profilePicture))
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(profilePictureSettings)
                    Glide.with(this).load(StorageUtil.pathToReference(user.profilePicture)).apply(RequestOptions.circleCropTransform()).into(profilePictureSettings)
                }

            }
    }

    class PrefsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferencesettings)
        }

        override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }
        override fun onSharedPreferenceChanged(sharedPref: SharedPreferences, key: String) {
            when (key) {
                "default_reveil" -> {
                    findPreference("default_reveil").editor.putString("reveil_default", "").apply()
                }
                "visibility_preference" -> {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    when(sharedPreferences.getString("visibility_preference", "WORLD")) {
                        "WORLD" -> FireStore.updateCurrentUser(authorization = 2)
                        "FRIENDS" -> FireStore.updateCurrentUser(authorization = 1)
                        "PRIVATE" -> FireStore.updateCurrentUser(authorization = 0)
                    }
                }
            }
        }

        override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen?, preference: Preference?): Boolean {
            if (preference!!.key == "changepassword"){
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.changepassword_popup_layout,null)
                val popupWindow = PopupWindow(
                        view,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popupWindow.enterTransition = slideIn
                val slideOut = Slide()
                slideOut.slideEdge = Gravity.END
                popupWindow.exitTransition = slideOut
                popupWindow.isFocusable = true
                val closepop = view.findViewById<Button>(R.id.button_closepop_password)
                val changepass = view.findViewById<Button>(R.id.button_change_password)
                closepop.setOnClickListener{
                    popupWindow.dismiss()
                }
                changepass.setOnClickListener {
                    val oldpass = view.findViewById<EditText>(R.id.oldpassword)
                    val firstpass = view.findViewById<EditText>(R.id.fistpassword)
                    val secondpass = view.findViewById<EditText>(R.id.secondpassword)
                    if (firstpass.text.toString() == "" || secondpass.text.toString() == "" || oldpass.text.toString() == "") {
                        Toast.makeText(activity, "Champs vide", Toast.LENGTH_LONG).show()
                    }
                    else if (firstpass.text.toString() != secondpass.text.toString()) {
                        Toast.makeText(activity, "Mot de passe différent", Toast.LENGTH_LONG).show()
                    }
                    else {
                        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                        val user = FirebaseAuth.getInstance().currentUser
                        mAuth.signInWithEmailAndPassword(user!!.email.toString(), oldpass.text.toString())
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("HELLO", "Connecte new")
                                        val cred = EmailAuthProvider.getCredential(user.email.toString(), oldpass.text.toString())
                                        user.reauthenticate(cred)?.addOnCompleteListener {
                                            user.updatePassword(firstpass.text.toString()).addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(activity, "Mot de passe changé", Toast.LENGTH_LONG).show()
                                                    popupWindow.dismiss()
                                                } else {
                                                    Toast.makeText(activity, "Erreur lors du changement de mot de passe", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                                    .addOnFailureListener { e -> Log.d("HELLO", "Error change password", e) }
                                        }
                                    } else {
                                        Log.e("HELLO", "Erreur ancien mot de passe", task.exception)
                                        Toast.makeText(activity, "Ancien mot de passe incorrect", Toast.LENGTH_LONG).show()
                                    }
                                }
                    }
                }
                popupWindow.showAtLocation(
                        view,
                        Gravity.CENTER,
                        0,
                        0
                )
            }
            else if (preference.key == "deconnect") {
                val sendDeconnect = LocalBroadcastManager.getInstance(context)
                sendDeconnect
                        .sendBroadcast(Intent("deconnectUser"))
                activity.finish()
            }
            else if (preference.key == "deleteaccount") {
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.deleteaccount_popup_layout,null)
                val popupWindow = PopupWindow(
                        view,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popupWindow.enterTransition = slideIn
                val slideOut = Slide()
                slideOut.slideEdge = Gravity.END
                popupWindow.exitTransition = slideOut
                popupWindow.isFocusable = true
                val closepop = view.findViewById<Button>(R.id.button_closepop_password)
                val changepass = view.findViewById<Button>(R.id.button_change_password)
                closepop.setOnClickListener{
                    popupWindow.dismiss()
                }
                changepass.setOnClickListener {
                    val firstpass = view.findViewById<EditText>(R.id.fistpassword)
                    val secondpass = view.findViewById<EditText>(R.id.secondpassword)
                    if (firstpass.text.toString() == "" || secondpass.text.toString() == "") {
                        Toast.makeText(activity, "Champs vide", Toast.LENGTH_LONG).show()
                    }
                    else if (firstpass.text.toString() != secondpass.text.toString()) {
                        Toast.makeText(activity, "Mot de passe différent", Toast.LENGTH_LONG).show()
                    }
                    else {
                        val mAuth: FirebaseAuth? = FirebaseAuth.getInstance()
                        val user = FirebaseAuth.getInstance().currentUser
                        mAuth!!.signInWithEmailAndPassword(user!!.email.toString(), firstpass.text.toString())
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("HELLO", "Connecte new")
                                        val cred = EmailAuthProvider.getCredential(user.email.toString(), firstpass.text.toString())
                                        user.reauthenticate(cred)?.addOnCompleteListener {
                                            val fireStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
                                            fireStoreInstance.collection("users")
                                                    .document(user.uid).delete()
                                                    .addOnCompleteListener {
                                                        user.delete()
                                                        .addOnCompleteListener {task ->
                                                            if (task.isSuccessful) {
                                                                val sendDeconnect = LocalBroadcastManager.getInstance(context)
                                                                sendDeconnect
                                                                        .sendBroadcast(Intent("deconnectUser"))
                                                                activity.finish()
                                                            }
                                                        }
                                                        .addOnFailureListener { e -> Log.d("HELLO", "Error delete account", e) }
                                                    }
                                                    .addOnFailureListener { e-> Log.d("HELLO", "Error delete account data", e) }
                                        }
                                    } else {
                                        Log.e("HELLO", "Erreur ancien mot de passe", task.exception)
                                        Toast.makeText(activity, "Ancien mot de passe incorrect", Toast.LENGTH_LONG).show()
                                    }
                                }
                    }
                }
                popupWindow.showAtLocation(
                        view,
                        Gravity.CENTER,
                        0,
                        0
                )
            }
           return super.onPreferenceTreeClick(preferenceScreen, preference)
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, SettingsActivity::class.java)
            return intent
        }
    }
}