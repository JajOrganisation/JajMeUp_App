package jajcompany.jajmeup.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(ConnectRegistrationActivity.newIntent(this))
            finish()
        }
        else {
            Log.d("HELLO", "CONNECTED")
            startActivity(PrincipalActivity.newIntent(this))
            finish()
        }
    }
}