package jajcompany.jajmeup.Activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import jajcompany.jajmeup.R
import kotlinx.android.synthetic.main.connectregistration_layout.*

class ConnectRegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connectregistration_layout)
        connectButton.setOnClickListener {
            //Start Activity Connect
        }
        registrationButton.setOnClickListener {
            //Start Activity Registration
        }
    }
}