package jajcompany.jajmeup.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import jajcompany.jajmeup.R
import jajcompany.jajmeup.Utils.FireStore
import jajcompany.jajmeup.Utils.StorageUtil
import jajcompany.jajmeup.glide.GlideApp
import kotlinx.android.synthetic.main.profilepicturesettings_layout.*
import java.io.ByteArrayOutputStream

class SettingsActivity : AppCompatActivity() {
    private val RC_SELECT_IMAGE = 2
    private lateinit var selectedImageBytes: ByteArray
    private var pictureJustChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profilepicturesettings_layout)
        //fragmentManager.beginTransaction().replace(R.id.fragment_settings,
       //         PrefsFragmentTest()).commit()
        setTheme(R.style.PreferencesTheme)
        profilePictureSettings.setOnClickListener {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }
            startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)
        }
       /* layoutPreferenceSettings.setOnClickListener{
            startActivity(SettingsPreferenceActivity.newIntent(this))
        }*/
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
                    .into(profilePictureSettings)

            pictureJustChanged = true
            StorageUtil.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
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

    class PrefsFragmentTest : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

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
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, SettingsActivity::class.java)
            return intent
        }
    }
}