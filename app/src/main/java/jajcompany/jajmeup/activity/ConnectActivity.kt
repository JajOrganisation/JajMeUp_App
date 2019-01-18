package jajcompany.jajmeup.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
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
            if (!Patterns.EMAIL_ADDRESS.matcher(username).matches() || username.replace("\\s".toRegex(), "") == ""){
                Toast.makeText(this, "Courriel incorrect", Toast.LENGTH_LONG).show()
            }
            else {
                mAuth!!.signInWithEmailAndPassword(username, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with signed-in user's information
                                startActivity(PrincipalActivity.newIntent(this))
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

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, ConnectActivity::class.java)
            return intent
        }
    }
}