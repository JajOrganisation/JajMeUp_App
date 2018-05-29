package jajcompany.jajmeup.Activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import jajcompany.jajmeup.R
import kotlinx.android.synthetic.main.connect_layout.*

class ConnectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connect_layout)
        connectButtonLogin.setOnClickListener {
            val username: String = userNameConnect.text.toString()
            val password: String = passwordConnect.text.toString()
            //Call Server
        }
    }
}