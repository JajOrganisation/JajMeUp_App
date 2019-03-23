package jajcompany.jajmeup.fragment

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jajcompany.jajmeup.R
import jajcompany.jajmeup.utils.Alarm
import kotlinx.android.synthetic.main.clock_layout.*


class ClockFragment : Fragment() {

    private final var isRunning = false

    @SuppressLint("SetTextI18n")
    private val threadActualize = Thread(Runnable {
            while(isRunning) {
                try {
                    Log.d("HELLO", "On change")
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
                    val hoursbeetween = Alarm.getBetween(sharedPreferences.getString("hours_clock", "-11:-11")!!.toString())
                    val hours = hoursbeetween.split(':')[0]
                    val minutes = hoursbeetween.split(':')[1]
                    if (hours == "00" || hours == "0")
                        alarmbetween.text = getString(R.string.alarm_in)+" "+minutes+" minutes"
                    else
                        alarmbetween.text = getString(R.string.alarm_in)+" "+hours+" heures "+minutes+" minutes"

                    Thread.sleep(1000 * 5)
                }catch (e: Exception) {

                }
            }
        })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.clock_layout, container, false)
    }

    @SuppressLint("SetTextI18n")
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
                    Log.d("HELLO", "Coucou "+alarm.hour.toString()+":"+alarm.minute.toString())
                    //val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    val hoursbeetween = Alarm.getBetween(sharedPreferences.getString("hours_clock", "-11:-11")!!.toString())
                    val hours = hoursbeetween.split(':')[0]
                    val minutes = hoursbeetween.split(':')[1]
                    Log.d("HELLO", "Oui "+hours+minutes)
                    if (hours == "00" || hours == "0")
                        alarmbetween.text = getString(R.string.alarm_in)+" "+minutes+" minutes"
                    else
                        alarmbetween.text = getString(R.string.alarm_in)+hours+" heures "+minutes+" minutes"
                    isRunning = true
                    try {
                        threadActualize.start()
                    } catch (e: Exception){

                    }
                }
            }
            else {
                sharedPreferences.edit().putString("hours_clock", "-11:-11").apply()
                sharedPreferences.edit().putString("between_time", "-11:-11").apply()
                alarmbetween.text = getString(R.string.no_alarm)
                Alarm.deleteAlarm(this.activity!!)
                isRunning = false
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
        alarmSet.isChecked = sharedPreferences.getString("hours_clock", "-11:-11") != "-11:-11"
        if(alarmSet.isChecked) {
            Log.d("HELLO", "Au revoir"+sharedPreferences.getString("hours_clock", "-11:-11"))
            val hoursbeetween = Alarm.getBetween(sharedPreferences.getString("hours_clock", "-11:-11")!!.toString())
            val hours = hoursbeetween.split(':')[0]
            val minutes = hoursbeetween.split(':')[1]
            Log.d("HELLO", "Oui "+hours+minutes)
            if (hours == "00" || hours == "0")
                alarmbetween.text = getString(R.string.alarm_in)+" "+minutes+" minutes"
            else
                alarmbetween.text = getString(R.string.alarm_in)+" "+hours+" heures "+minutes+" minutes"
            isRunning = true
            try {
                threadActualize.start()
            } catch (e: Exception){

            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        isRunning = false
    }
}