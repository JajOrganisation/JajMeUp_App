package jajcompany.jajmeup.Activity


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import jajcompany.jajmeup.Fragment.ClockFragment
import jajcompany.jajmeup.Fragment.CommunityFragment
import jajcompany.jajmeup.Fragment.HistoryFragment
import jajcompany.jajmeup.R
import jajcompany.jajmeup.Utils.FireStore
import kotlinx.android.synthetic.main.main_layout.*


class PrincipalActivity : AppCompatActivity() {

    var link: String = ""
    private lateinit var textCartItemCountFriends: TextView
    private lateinit var textCartItemCountNotifications: TextView
    private lateinit var askingFriendsCount: ListenerRegistration
    private lateinit var notificationsCount: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {

        PreferenceManager.setDefaultValues(this, R.xml.preferencesettings, false)

        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(ConnectRegistrationActivity.newIntent(this))
            finish()
        }
        super.onCreate(savedInstanceState)
       setContentView(R.layout.main_layout)

        navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_history -> {
                    replaceFragment(HistoryFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_clock -> {
                    replaceFragment(ClockFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_community -> {
                    replaceFragment(CommunityFragment())
                    return@setOnNavigationItemSelectedListener true
                }
            }
            true
        }
        navigation.selectedItemId = R.id.navigation_community
        val intent: Intent = intent
        val action: String? = intent.action
        var type: String? = intent.type

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                if (navigation.selectedItemId != R.id.navigation_community) {
                    navigation.selectedItemId = R.id.navigation_community
                }
                handleSendText(intent)
            }
        }
        checkPref()
    }

    override fun onResume() {
        checkPref()
        super.onResume()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("on_wakeup", false)){
            replaceFragment(CommunityFragment())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unsetCountFriendsAsking()
        unsetCountNotifications()
    }

        override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        initAppMenu(menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.settings -> {
                startActivity(SettingsActivity.newIntent(this))
            }
            R.id.friends_notification -> {
                startActivity(AskingFriendsActivity.newIntent(this))
            }
            else->super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleSendText(intent: Intent){

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            Log.d("YOUTUBE_SHARE", sharedText)
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            sharedPreferences.edit().putString("current_link", sharedText).apply()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, PrincipalActivity::class.java)
            return intent
        }

    }

    private fun setupNotifNumberFriends(count: Int) {
        if (count == 0) {
            if (textCartItemCountFriends.visibility != View.GONE) {
                textCartItemCountFriends.visibility = View.GONE
            }
        } else {
            textCartItemCountFriends.text = count.toString()
            if (textCartItemCountFriends.visibility != View.VISIBLE) {
                textCartItemCountFriends.visibility = View.VISIBLE
            }
        }
    }

    private fun setupNotifNumber(count: Int) {

        if (count == 0) {
            if (textCartItemCountNotifications.visibility != View.GONE) {
                textCartItemCountNotifications.visibility = View.GONE
            }
        } else {
            textCartItemCountNotifications.text = count.toString()
            if (textCartItemCountNotifications.visibility != View.VISIBLE) {
                textCartItemCountNotifications.visibility = View.VISIBLE
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentlayout, fragment)
                .commit()
    }

    private fun checkPref() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@PrincipalActivity)
        if(!sharedPreferences.getBoolean("history_preference", true)) {
            navigation.menu.getItem(0).isEnabled = false
            if(navigation.menu.getItem(0).isChecked) {
                navigation.selectedItemId = R.id.navigation_clock
            }
        }
        else
            navigation.menu.getItem(0).isEnabled = true

        if (sharedPreferences.getString("visibility_preference", "WORLD") == "PRIVATE") {
            navigation.menu.getItem(2).isEnabled = false
            if (navigation.menu.getItem(2).isChecked) {
                navigation.selectedItemId = R.id.navigation_clock
            }
        }
        else
            navigation.menu.getItem(2).isEnabled = true
    }

    fun setCountFriendsAsking() {
        askingFriendsCount = FireStore.askingFriendCount(this::setupNotifNumberFriends)
    }

    fun setCountNotifications() {
        notificationsCount = FireStore.askingFriendCount(this::setupNotifNumber)
    }

    fun unsetCountFriendsAsking() {
        FireStore.removeListener(askingFriendsCount)
    }

    fun unsetCountNotifications() {
        FireStore.removeListener(notificationsCount)
    }

    private fun initAppMenu(menu: Menu) {

        val friendsItem = menu.findItem(R.id.friends_notification)
        val friendsActionView = MenuItemCompat.getActionView(friendsItem)
        textCartItemCountFriends = friendsActionView.findViewById<View>(R.id.cart_badge_friends) as TextView

        friendsActionView.setOnClickListener { onOptionsItemSelected(friendsItem) }

        val notificationsItem = menu.findItem(R.id.notifications)
        val notificationsActionView = MenuItemCompat.getActionView(notificationsItem)
        textCartItemCountNotifications = notificationsActionView.findViewById<View>(R.id.cart_badge_notification) as TextView

        notificationsActionView.setOnClickListener { onOptionsItemSelected(notificationsItem) }

        setCountFriendsAsking()
        setCountNotifications()
    }
}
