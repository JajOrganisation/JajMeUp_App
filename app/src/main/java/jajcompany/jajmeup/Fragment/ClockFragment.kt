package jajcompany.jajmeup.Fragment

import android.app.AlarmManager
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jajcompany.jajmeup.Activity.YouTubeJAJActivity
import jajcompany.jajmeup.R
import jajcompany.jajmeup.Utils.Alarm
import jajcompany.jajmeup.Utils.Alarm.alarmManager
import kotlinx.android.synthetic.main.clock_layout.*


class ClockFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.clock_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        alarm.setIs24HourView(true)
        alarmset.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked) {
                alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                Alarm.setAlarm(activity, alarm.currentHour, alarm.currentMinute)
            }
            else {
                Alarm.deleteAlarm(activity)
            }
        }
    }

}