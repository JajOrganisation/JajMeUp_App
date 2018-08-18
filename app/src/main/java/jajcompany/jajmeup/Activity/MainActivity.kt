package jajcompany.jajmeup


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.ListPreference
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import jajcompany.jajmeup.Activity.ConnectRegistrationActivity
import jajcompany.jajmeup.Activity.TestSettings
import jajcompany.jajmeup.Fragment.ClockFragment
import jajcompany.jajmeup.Fragment.CommunityFragment
import jajcompany.jajmeup.Fragment.HistoryFragment
import kotlinx.android.synthetic.main.main_layout.*
import android.preference.PreferenceManager


class MainActivity : AppCompatActivity() {

    private var fragment = Fragment()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_clock -> {
                replaceFragment(ClockFragment())
                true
            }
            R.id.navigation_history -> {
                replaceFragment(HistoryFragment())
                true
            }
            R.id.navigation_community -> {
                replaceFragment(CommunityFragment())
                true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        PreferenceManager.setDefaultValues(this, R.xml.preferencesettings, false)

        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(ConnectRegistrationActivity.newIntent(this))
            finish()
        }
        super.onCreate(savedInstanceState)
       setContentView(R.layout.main_layout)
        replaceFragment(CommunityFragment())


        navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_clock -> {
                    replaceFragment(ClockFragment())
                    true
                }
                R.id.navigation_history -> {
                    replaceFragment(HistoryFragment())
                    true
                }
                R.id.navigation_community -> {
                    replaceFragment(CommunityFragment())
                    true
                }
            }
            false
        }
        /*val bottomNavigationView: BottomNavigationView = findViewById(R.id.navigation) as BottomNavigationView
        bottomNavigationView.selectedItemId = R.id.navigation_clock
        fragment = Fragment.instantiate(this@MainActivity,
                ClockFragment::class.java!!.getName()) as ClockFragment
        fragmentManager.beginTransaction().replace(R.id.fragmentlayout, fragment).commit()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)*/
        val intent: Intent = getIntent()
        val action: String? = intent.action
        var type: String? = intent.type

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()

       val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        if (!sharedPreferences.getBoolean("history_preference", true))
            navigation.menu.setGroupEnabled(R.id.history_menu, false)
        else
            navigation.menu.setGroupEnabled(R.id.history_menu, true)

        if (sharedPreferences.getString("visibility_preference", "WORLD") == "PRIVATE")
            navigation.menu.setGroupEnabled(R.id.community_menu, false)
        else
            navigation.menu.setGroupEnabled(R.id.community_menu, true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.settings -> {
                //startActivity(SettingsActivity.newIntent(this))
                startActivity(TestSettings.newIntent(this))
            }
            else->super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    fun handleSendText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            Log.d("YOUTUBE_SHARE", sharedText)
            val communityFragment = CommunityFragment.newInstance(sharedText)
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentlayout, communityFragment, CommunityFragment::class.java!!.getName())
                    .commit()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, MainActivity::class.java)
            return intent
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentlayout, fragment)
                .commit()
    }
}
