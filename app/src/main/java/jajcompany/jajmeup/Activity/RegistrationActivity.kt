package jajcompany.jajmeup.Activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import jajcompany.jajmeup.R
import kotlinx.android.synthetic.main.connect_layout.*
import kotlinx.android.synthetic.main.registration_layout.*

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_layout)
        registrationButtonRegister.setOnClickListener {
            val username: String = userNameRegistration.text.toString()
            val password: String = passwordRegistration.text.toString()
            val passwordconfirm: String = passwordRegistrationConfirm.text.toString()
            if (password != passwordconfirm) {
                Toast.makeText(this, "Mots de passe differents", Toast.LENGTH_LONG).show()
            }
            else {
                //Call Server
            }
        }
    }
}