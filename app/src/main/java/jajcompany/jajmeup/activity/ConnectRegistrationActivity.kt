package jajcompany.jajmeup.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import jajcompany.jajmeup.R
import jajcompany.jajmeup.utils.Jajinternet
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
            if (Jajinternet.getStatusInternet(this)) {
                startActivity(ConnectActivity.newIntent(this))
                finish()
            }
            else {
                Toast.makeText(this, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
            }

        }
        registrationButton.setOnClickListener {
            if (Jajinternet.getStatusInternet(this)) {
                startActivity(RegistrationActivity.newIntent(this))
                finish()
            }
            else {
                Toast.makeText(this, getString(R.string.erreur_internet), Toast.LENGTH_LONG).show()
            }
        }
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("HELLO", "Permission to record denied")
            makeRequest()
        }
    }

    override fun onResume() {
        super.onResume()
        //checkBattery()
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("HELLO", "Permission to record denied")
            makeRequest()
        }
    }

   /* fun checkBattery() {
        val packageName = applicationContext.packageName
        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        Log.d("HELLO", packageName)
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intentt = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intentt.data = Uri.parse("package:"+applicationContext.packageName)
            startActivity(intentt)
        }
    }*/

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            142 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d("HELLO", "Permission OK")
                } else {
                    finish()
                }
                return
            }
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                142)
    }


    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, ConnectRegistrationActivity::class.java)
            return intent
        }
    }
}