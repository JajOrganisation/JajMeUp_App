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
import android.util.Log
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
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
            if (isChecked) {
                //alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (sharedPreferences.getString("hours_clock", "-11:-11") == "-11:-11") {
                    Log.d("HELLO", "Coucou "+alarm.hour.toString()+":"+alarm.minute.toString())
                    Alarm.setAlarm(this.activity!!, alarm.hour, alarm.minute, alarmSet)
                    sharedPreferences.edit().putString("hours_clock", alarm.hour.toString() + ":" + alarm.minute.toString()).apply()
                }
            }
            else {
                sharedPreferences.edit().putString("hours_clock", "-11:-11").apply()
                Alarm.deleteAlarm(this.activity!!)
            }
        }
        alarm.setOnTimeChangedListener { timePicker, _, _ ->
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
            if (sharedPreferences.getString("hours_clock", "-11:-11") != "-11:-11") {
                alarm.hour = sharedPreferences.getString("hours_clock", "-11:-11")!!.split(":")[0].toInt()
                alarm.minute = sharedPreferences.getString("hours_clock", "-11:-11")!!.split(":")[1].toInt()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
        if (sharedPreferences.getBoolean("on_wakeup_clock", false)){
            sharedPreferences.edit().putString("hours_clock", "-11:-11").apply()
            Alarm.deleteAlarm(this.activity!!)
            sharedPreferences.edit().putBoolean("on_wakeup_clock", false).apply()
            alarmSet.isChecked = false
        }
        else if (sharedPreferences.getBoolean("on_wakeup_my_alarm_clock", false)){
            sharedPreferences.edit().putString("hours_clock", "-11:-11").apply()
            sharedPreferences.edit().putBoolean("on_wakeup_my_alarm_clock", false).apply()
            alarmSet.isChecked = false
            Alarm.deleteAlarm(this.activity!!)
        }
        val intent = Intent(context, Alarm.OnAlarm::class.java)
        intent.action = "onReveilRing"
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE) != null
        alarmSet.isChecked = pendingIntent
        if(alarmSet.isChecked) {
            Log.d("HELLO", "Au revoir"+sharedPreferences.getString("hours_clock", "-11:-11"))
            alarm.hour = sharedPreferences.getString("hours_clock", "-11:-11")!!.split(":")[0].toInt()
            alarm.minute = sharedPreferences.getString("hours_clock", "-11:-11")!!.split(":")[1].toInt()
        }

    }
}