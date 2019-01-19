package jajcompany.jajmeup.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import jajcompany.jajmeup.R
import kotlinx.android.synthetic.main.connectregistration_layout.*

class ConnectRegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connectregistration_layout)
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            Log.d("HELLO", "Erreur deconnect Firebase : "+e)
        }
        connectButton.setOnClickListener {
            startActivity(ConnectActivity.newIntent(this))
            finish()
        }
        registrationButton.setOnClickListener {
            startActivity(RegistrationActivity.newIntent(this))
            finish()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, ConnectRegistrationActivity::class.java)
            return intent
        }
    }
}