package jajcompany.jajmeup.Fragment

import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jajcompany.jajmeup.R
import jajcompany.jajmeup.Utils.Alarm
import jajcompany.jajmeup.Utils.Alarm.alarmManager
import jajcompany.jajmeup.Utils.FireStore
import kotlinx.android.synthetic.main.clock_layout.*


class ClockFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.clock_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        alarm.setIs24HourView(true)
        alarmSet.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked) {
                alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                Alarm.setAlarm(this.activity!!, alarm.hour, alarm.minute, alarmSet)
                var myhours = alarm.hour.toString()
                var myminutes = alarm.minute.toString()
                if (alarm.hour < 10) {
                    myhours = "0"+myhours
                }
                if (alarm.minute < 10) {
                    myminutes = "0"+myminutes
                }
                val myreveil = myhours+":"+myminutes
                FireStore.updateCurrentUser(reveilCurrent =  myreveil)
            }
            else {
                FireStore.updateCurrentUser(reveilCurrent = "down")
                Alarm.deleteAlarm(this.activity!!)
            }
        }
    }
}