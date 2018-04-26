package jajcompany.jajmeup

import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import jajcompany.jajmeup.Fragment.CommunityFragment
import jajcompany.jajmeup.Fragment.HistoryFragment
import jajcompany.jajmeup.Fragment.ClockFragment
import kotlinx.android.synthetic.main.main_layout.*

class MainActivity : AppCompatActivity() {

    private var fragment = Fragment()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_clock -> {
                fragment = Fragment.instantiate(this@MainActivity,
                        ClockFragment::class.java!!.getName()) as ClockFragment
                fragmentManager.beginTransaction().replace(R.id.fragmentlayout, fragment).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_history -> {
                fragment = Fragment.instantiate(this@MainActivity,
                        HistoryFragment::class.java!!.getName()) as HistoryFragment
                fragmentManager.beginTransaction().replace(R.id.fragmentlayout, fragment).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_community -> {
                fragment = Fragment.instantiate(this@MainActivity,
                        CommunityFragment::class.java!!.getName()) as CommunityFragment
                fragmentManager.beginTransaction().replace(R.id.fragmentlayout, fragment).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.navigation) as BottomNavigationView
        bottomNavigationView.selectedItemId = R.id.navigation_clock
        fragment = Fragment.instantiate(this@MainActivity,
                ClockFragment::class.java!!.getName()) as ClockFragment
        fragmentManager.beginTransaction().replace(R.id.fragmentlayout, fragment).commit()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
