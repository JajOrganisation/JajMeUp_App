package jajcompany.jajmeup.utils

import android.app.Service
import android.widget.Toast
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log


class TestService : Service() {
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("HELLO", "Start service")
        Toast.makeText(this, "COMMANDE...", Toast.LENGTH_LONG).show()
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "Detruit...", Toast.LENGTH_LONG).show()
    }
}