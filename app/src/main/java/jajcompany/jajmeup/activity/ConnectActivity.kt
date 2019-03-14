package jajcompany.jajmeup.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import jajcompany.jajmeup.R
import jajcompany.jajmeup.utils.FireStore
import jajcompany.jajmeup.utils.Jajinternet
import jajcompany.jajmeup.utils.YoutubeInformation
import kotlinx.android.synthetic.main.connect_layout.*

class ConnectActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connect_layout)
        mAuth = FirebaseAuth.getInstance()
        connectButtonLogin.setOnClickListener {
            if (Jajinternet.getStatusInternet(this)) {
                val username: String = userNameConnect.text.toString()
                val password: String = passwordConnect.text.toString()
                if (!Patterns.EMAIL_ADDRESS.matcher(username).matches() || username.replace("\\s".toRegex(), "") == "") {
                    Toast.makeText(this, getString(R.string.courriel_incorrect), Toast.LENGTH_LONG).show()
                } else {
                    mAuth!!.signInWithEmailAndPassword(username, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    FireStore.getCurrentUser { user ->
                                        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("default_reveil", user.reveilDefaultLink).apply()
                                        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("default_reveil_name", YoutubeInformation.getTitleQuietly(YoutubeInformation.getIDFromURL(user.reveilDefaultLink))).apply()
                                        Log.d("HELLO", "PREF "+user.authorization.toString())
                                        when(user.authorization){
                                            0 -> PreferenceManager.getDefaultSharedPreferences(this).edit().putString("visibility_preference", "PRIVATE").apply()
                                            1 -> PreferenceManager.getDefaultSharedPreferences(this).edit().putString("visibility_preference", "FRIENDS").apply()
                                            2 -> PreferenceManager.getDefaultSharedPreferences(this).edit().putString("visibility_preference", "WORLD").apply()
                                        }
                                        startActivity(PrincipalActivity.newIntent(this))
                                        Log.d("LoginActivity", "signInWithEmail:success")
                                    }
                                } else {
                                    Log.e("LoginActivity", "signInWithEmail:failure", task.exception)
                                    Toast.makeText(this@ConnectActivity, getString(R.string.erreur_identification),
                                            Toast.LENGTH_SHORT).show()
                                }
                            }
                }
            }
            else {
                Toast.makeText(this, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
            }
        }

        connectForgotPass.setOnClickListener {
            if (Jajinternet.getStatusInternet(this)) {
                val username: String = userNameConnect.text.toString()
                if (!Patterns.EMAIL_ADDRESS.matcher(username).matches() || username.replace("\\s".toRegex(), "") == "") {
                    Toast.makeText(this, getString(R.string.courriel_incorrect), Toast.LENGTH_LONG).show()
                } else {
                    mAuth!!.sendPasswordResetEmail(username)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, getString(R.string.courriel_envoye), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, getString(R.string.courriel_introuvable), Toast.LENGTH_SHORT).show()
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
            val intent = Intent(context, ConnectActivity::class.java)
            return intent
        }
    }
}