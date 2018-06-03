package jajcompany.jajmeup.Activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import jajcompany.jajmeup.R
import kotlinx.android.synthetic.main.connect_layout.*

class ConnectActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connect_layout)
        mAuth = FirebaseAuth.getInstance()
        connectButtonLogin.setOnClickListener {
            val username: String = userNameConnect.text.toString()
            val password: String = passwordConnect.text.toString()
            mAuth!!.signInWithEmailAndPassword(username!!, password!!)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with signed-in user's information
                            Log.d("LoginActivity", "signInWithEmail:success")
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e("LoginActivity", "signInWithEmail:failure", task.exception)
                            Toast.makeText(this@ConnectActivity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                        }
            }
        }
    }
}