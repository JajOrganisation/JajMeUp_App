package jajcompany.jajmeup.activity

import android.app.Activity
import android.content.ClipboardManager
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
import android.transition.TransitionManager
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
import jajcompany.jajmeup.utils.Jajinternet
import jajcompany.jajmeup.utils.StorageUtil
import jajcompany.jajmeup.utils.YoutubeInformation
import kotlinx.android.synthetic.main.profilepicturesettings_layout.*
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern


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
            if (!Jajinternet.getStatusInternet(context)) {
                Toast.makeText(context, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
            }
            findPreference("default_alarm").summary = PreferenceManager.getDefaultSharedPreferences(context).getString("default_reveil_name", "Rick Astley - Never Gonna Give You Up (Official Music Video)")
            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == 3 && resultCode == Activity.RESULT_OK &&
                    data != null && data.data != null) {
                val audioFilePath = data.data
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("last_alarm", audioFilePath.toString()).apply()
            }
        }

        override fun onSharedPreferenceChanged(sharedPref: SharedPreferences, key: String) {
            if (Jajinternet.getStatusInternet(context)) {
                when (key) {
                    "visibility_preference" -> {
                        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                        when (sharedPreferences.getString("visibility_preference", "WORLD")) {
                            "WORLD" -> FireStore.updateCurrentUser(authorization = 2)
                            "FRIENDS" -> FireStore.updateCurrentUser(authorization = 1)
                            "PRIVATE" -> FireStore.updateCurrentUser(authorization = 0)
                        }
                    }
                }
            } else {
                Toast.makeText(context, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
            }
        }

        override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen?, preference: Preference?): Boolean {
            if (Jajinternet.getStatusInternet(context)) {
                if (preference!!.key == "changepassword") {
                    val inflater = LayoutInflater.from(context)
                    val view = inflater.inflate(R.layout.changepassword_popup_layout, null)
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
                    closepop.setOnClickListener {
                        popupWindow.dismiss()
                    }
                    changepass.setOnClickListener {
                        val oldpass = view.findViewById<EditText>(R.id.oldpassword)
                        val firstpass = view.findViewById<EditText>(R.id.fistpassword)
                        val secondpass = view.findViewById<EditText>(R.id.secondpassword)
                        if (firstpass.text.toString() == "" || secondpass.text.toString() == "" || oldpass.text.toString() == "") {
                            Toast.makeText(activity, "Champs vide", Toast.LENGTH_LONG).show()
                        } else if (firstpass.text.toString() != secondpass.text.toString()) {
                            Toast.makeText(activity, "Mot de passe différent", Toast.LENGTH_LONG).show()
                        } else {
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
                } else if (preference.key == "deconnect") {
                    val sendDeconnect = LocalBroadcastManager.getInstance(context)
                    sendDeconnect
                            .sendBroadcast(Intent("deconnectUser"))
                    activity.finish()
                } else if (preference.key == "deleteaccount") {
                    val inflater = LayoutInflater.from(context)
                    val view = inflater.inflate(R.layout.deleteaccount_popup_layout, null)
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
                    closepop.setOnClickListener {
                        popupWindow.dismiss()
                    }
                    changepass.setOnClickListener {
                        val firstpass = view.findViewById<EditText>(R.id.fistpassword)
                        val secondpass = view.findViewById<EditText>(R.id.secondpassword)
                        if (firstpass.text.toString() == "" || secondpass.text.toString() == "") {
                            Toast.makeText(activity, "Champs vide", Toast.LENGTH_LONG).show()
                        } else if (firstpass.text.toString() != secondpass.text.toString()) {
                            Toast.makeText(activity, "Mot de passe différent", Toast.LENGTH_LONG).show()
                        } else {
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
                                                                    .addOnCompleteListener { task ->
                                                                        if (task.isSuccessful) {
                                                                            val sendDeconnect = LocalBroadcastManager.getInstance(context)
                                                                            sendDeconnect
                                                                                    .sendBroadcast(Intent("deconnectUser"))
                                                                            activity.finish()
                                                                        }
                                                                    }
                                                                    .addOnFailureListener { e -> Log.d("HELLO", "Error delete account", e) }
                                                        }
                                                        .addOnFailureListener { e -> Log.d("HELLO", "Error delete account data", e) }
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
                } else if (preference.key == "last_alarm") {
                    val intent = Intent().apply {
                        type = "audio/*"
                        action = Intent.ACTION_OPEN_DOCUMENT
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/mpeg"))
                    }
                    startActivityForResult(Intent.createChooser(intent, "Select Audio"), 3)
                } else if (preference.key == "default_alarm") {
                    val myClipboard: ClipboardManager? = activity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                    val primary = myClipboard?.primaryClip
                    val item = primary?.getItemAt(0)
                    val inflater = LayoutInflater.from(context)
                    val view = inflater.inflate(R.layout.defaultalarm_popup_layout, null)
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
                    val validationpop = view.findViewById<Button>(R.id.button_change_default_alarm)
                    val edityt = view.findViewById<EditText>(R.id.youtube_link_default_alarm)
                    val pattern = "(?<=watch\\?v=|/videos/|embed\\/|https://youtu.be/)[^#\\&\\?]*"
                    val compiledPattern = Pattern.compile(pattern)
                    if(compiledPattern.matcher(item!!.text.toString()).find()) {
                        edityt.setText(item.text, TextView.BufferType.EDITABLE)
                    }else if (PreferenceManager.getDefaultSharedPreferences(context).getString("current_link", "123456") != "123456") {
                        edityt.setText(PreferenceManager.getDefaultSharedPreferences(context).getString("current_link", "123456"), TextView.BufferType.EDITABLE)
                    }else {
                        PreferenceManager.getDefaultSharedPreferences(context).getString("default_reveil", "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                    }

                    closepop.setOnClickListener {
                        popupWindow.dismiss()
                    }
                    validationpop.setOnClickListener {
                        val matcher = compiledPattern.matcher(edityt.text.toString())
                        if (matcher.find()) {
                            val titlevideo = YoutubeInformation.getTitleQuietly(matcher.group())
                            if (titlevideo != "ERROR") {
                                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("default_reveil", edityt.text.toString()).apply()
                                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("default_reveil_name", YoutubeInformation.getTitleQuietly(YoutubeInformation.getIDFromURL(edityt.text.toString()))).apply()
                                FireStore.updateCurrentUser(reveilDefault = edityt.text.toString())
                                preference.summary = PreferenceManager.getDefaultSharedPreferences(context).getString("default_reveil_name", "Rick Astley - Never Gonna Give You Up (Official Music Video)")
                                popupWindow.dismiss()
                            } else {
                                Toast.makeText(activity, getString(R.string.lien_yt_invalide), Toast.LENGTH_LONG).show()
                            }

                        } else {
                            Toast.makeText(activity, getString(R.string.lien_yt_invalide), Toast.LENGTH_LONG).show()
                        }
                    }
                    popupWindow.showAtLocation(
                            view,
                            Gravity.CENTER,
                            0,
                            0
                    )
                }
            }
            else {
                Toast.makeText(context, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
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