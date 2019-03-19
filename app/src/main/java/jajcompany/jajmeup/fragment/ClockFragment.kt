package jajcompany.jajmeup.fragment

import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jajcompany.jajmeup.R
import jajcompany.jajmeup.utils.Alarm
import jajcompany.jajmeup.utils.Alarm.alarmManager
import jajcompany.jajmeup.utils.FireStore
import kotlinx.android.synthetic.main.clock_layout.*
import android.app.PendingIntent
import android.content.Intent
import android.preference.PreferenceManager
import android.widget.Toast
import jajcompany.jajmeup.activity.LoadingAlarm


class ClockFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.clock_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Toast.makeText(activity, "UP HAHA", Toast.LENGTH_SHORT).show()
        alarm.setIs24HourView(true)
        alarmSet.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked) {
                //alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                Alarm.setAlarm(this.activity!!, alarm.hour, alarm.minute, alarmSet)
            }
            else {
                Alarm.deleteAlarm(this.activity!!)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
        if (sharedPreferences.getBoolean("on_wakeup_clock", false)){
            Alarm.deleteAlarm(this.activity!!)
            sharedPreferences.edit().putBoolean("on_wakeup_clock", false).apply()
            alarmSet.isChecked = false
        }
        else if (sharedPreferences.getBoolean("on_wakeup_my_alarm_clock", false)){
            sharedPreferences.edit().putBoolean("on_wakeup_my_alarm_clock", false).apply()
            alarmSet.isChecked = false
            Alarm.deleteAlarm(this.activity!!)
        }
        val intent = Intent(context, Alarm.OnAlarm::class.java)
        intent.action = "onReveilRing"
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE) != null
        alarmSet.isChecked = pendingIntent
    }
}