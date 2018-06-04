package jajcompany.jajmeup.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import jajcompany.jajmeup.R
import kotlinx.android.synthetic.main.registration_layout.*
import android.util.Log
import jajcompany.jajmeup.MainActivity


class RegistrationActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_layout)
        mAuth = FirebaseAuth.getInstance()
        registrationButtonRegister.setOnClickListener {
            val username: String = userNameRegistration.text.toString()
            val password: String = passwordRegistration.text.toString()
            val passwordconfirm: String = passwordRegistrationConfirm.text.toString()
            if (password != passwordconfirm) {
                Toast.makeText(this, "Mots de passe differents", Toast.LENGTH_LONG).show()
            }
            else {
                mAuth?.createUserWithEmailAndPassword(username, password)
                        ?.addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("RegistrationActivity", "createUserWithEmail:success")
                                val user = mAuth?.currentUser
                                startActivity(MainActivity.newIntent(this))
                            } else {
                                // If sign in fails, display a message to the user.
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
}