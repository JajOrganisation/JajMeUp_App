package jajcompany.jajmeup.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.preference.PreferenceManager
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import jajcompany.jajmeup.R
import jajcompany.jajmeup.activity.PrincipalActivity
import jajcompany.jajmeup.utils.Alarm
import kotlinx.android.synthetic.main.clock_layout.*


class ClockFragment : Fragment() {

    private final var isRunning = false

    @SuppressLint("SetTextI18n")
    private val threadActualize = Thread(Runnable {
            while(isRunning) {
                try {
                    Log.d("HELLO", "On change")
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PrincipalActivity.applicationContext())
                    val hoursbeetween = Alarm.getBetween(sharedPreferences.getString("hours_clock", "-11:-11")!!.toString())
                    val hours = hoursbeetween.split(':')[0]
                    val minutes = hoursbeetween.split(':')[1]
                    if (hours == "00" || hours == "0")
                        if (minutes == "00" || minutes == "0") {
                            alarmbetween.text = getString(R.string.alarm_less_minute)
                            alarmbetweenvalue.visibility = View.GONE
                        }
                        else {
                            alarmbetween.text = getString(R.string.alarm_in)
                            alarmbetweenvalue.visibility = View.VISIBLE
                            alarmbetweenvalue.text = minutes+" minutes"
                        }

                    else {
                        alarmbetween.text = getString(R.string.alarm_in)
                        alarmbetweenvalue.visibility = View.VISIBLE
                        alarmbetweenvalue.text = hours+" heures "+minutes+" minutes"
                    }
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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PrincipalActivity.applicationContext())
        Log.d("HELLO", "PACKAGE"+activity!!.packageName)

        statusalarm.setOnClickListener {
            if (textstatus.text == "ON") {
                statusalarm.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.reveilDown))
                textstatus.text = "OFF"
                sharedPreferences.edit().putString("hours_clock", "-11:-11").apply()
                alarmbetween.text = getString(R.string.no_alarm)
                alarmbetweenvalue.visibility = View.GONE
                Alarm.deleteAlarm()
                Alarm.unsetNotif()
                isRunning = false

            }
            else {
                if (sharedPreferences.getString("previous_clock", "-11:-11") != "-11:-11") {
                    sharedPreferences.edit().putString("hours_clock",sharedPreferences.getString("previous_clock", "-11:-11")).apply()
                    Alarm.setAlarm(sharedPreferences.getString("previous_clock", "-11:-11").split(':')[0].toInt(), sharedPreferences.getString("previous_clock", "-11:-11").split(':')[1].toInt())
                    textstatus.text = "ON"
                    alarmbetweenvalue.visibility = View.VISIBLE
                    statusalarm.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.reveilUp))
                    setBetweentext()
                }
                else {
                    showPopClock()
                }
            }
        }

        hoursclock.setOnClickListener {
            showPopClock()
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PrincipalActivity.applicationContext())
        if (sharedPreferences.getBoolean("on_wakeup_clock", false)){
            Log.d("HELLO", "On wake up clock")
            sharedPreferences.edit().putString("hours_clock", "-11:-11").apply()
            Alarm.deleteAlarm()
            sharedPreferences.edit().putBoolean("on_wakeup_clock", false).apply()
            //alarmSet.isChecked = false
        }
        else if (sharedPreferences.getBoolean("on_wakeup_my_alarm_clock", false)){
            Log.d("HELLO", "On wake up alarm clock")
            sharedPreferences.edit().putString("hours_clock", "-11:-11").apply()
            sharedPreferences.edit().putBoolean("on_wakeup_my_alarm_clock", false).apply()
            //alarmSet.isChecked = false
            Alarm.deleteAlarm()
        }
        Log.d("HELLO", "Hours clock"+sharedPreferences.getString("hours_clock", "-11:-11"))
        if (sharedPreferences.getString("hours_clock", "-11:-11") != "-11:-11") {
            hoursclock.text = sharedPreferences.getString("hours_clock", "-11:-11")
            textstatus.text = "ON"
            statusalarm.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.reveilUp))
            setBetweentext()
        }
        else {
            if (sharedPreferences.getString("previous_clock", "-11:-11") != "-11:-11") {
                hoursclock.text = sharedPreferences.getString("previous_clock", "-11:-11")
            }
            textstatus.text = "OFF"
            alarmbetweenvalue.visibility = View.GONE
            statusalarm.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.reveilDown))
        }
        /*
            isRunning = true
            try {
                threadActualize.start()
            } catch (e: Exception){

            }
        }*/

    }

    override fun onDestroyView() {
        super.onDestroyView()
        isRunning = false
    }

    private fun showPopClock() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.alarm_popup_layout,null)
        val popupWindow = PopupWindow(
                view,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val alarmpop = view.findViewById<TimePicker>(R.id.alarmpop)
        alarmpop.setIs24HourView(true)
        val slideIn = Slide()
        slideIn.slideEdge = Gravity.TOP
        popupWindow.enterTransition = slideIn
        val slideOut = Slide()
        slideOut.slideEdge = Gravity.END
        popupWindow.exitTransition = slideOut
        popupWindow.isFocusable = true
        val closepop = view.findViewById<Button>(R.id.button_closepop_alarm)
        val setalarm = view.findViewById<Button>(R.id.button_alarmset)
        closepop.setOnClickListener{
            popupWindow.dismiss()
        }
        setalarm.setOnClickListener {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PrincipalActivity.applicationContext())
            Log.d("HELLO", "Coucou "+alarmpop.hour.toString()+":"+alarmpop.minute.toString())
            Alarm.setAlarm(alarmpop.hour, alarmpop.minute/*, alarmSet*/)
            sharedPreferences.edit().putString("hours_clock", String.format("%02d", alarmpop.hour) + ":" + String.format("%02d", alarmpop.minute)).apply()
            sharedPreferences.edit().putString("previous_clock", String.format("%02d", alarmpop.hour) + ":" + String.format("%02d", alarmpop.minute)).apply()
            Log.d("HELLO", "Coucou "+alarmpop.hour.toString()+":"+alarmpop.minute.toString())
            //val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            setBetweentext()
            hoursclock.text = String.format("%02d", alarmpop.hour) + ":" + String.format("%02d", alarmpop.minute)
            textstatus.text = "ON"
            statusalarm.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.reveilUp))
            popupWindow.dismiss()
        }
        popupWindow.showAtLocation(
                clock_layout,
                Gravity.CENTER,
                0,
                0
        )
    }

    private fun setBetweentext() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PrincipalActivity.applicationContext())
        alarmbetweenvalue.visibility = View.GONE
        alarmbetweenvalue.visibility = View.VISIBLE
        val hoursbeetween = Alarm.getBetween(sharedPreferences.getString("hours_clock", "-11:-11")!!.toString())
        val hours = hoursbeetween.split(':')[0]
        val minutes = hoursbeetween.split(':')[1]
        Log.d("HELLO", "Oui Resume"+hours+minutes)
        if (hours == "00" || hours == "0")
            if (minutes == "00" || minutes == "0") {
                alarmbetween.text = getString(R.string.alarm_less_minute)
                alarmbetweenvalue.visibility = View.GONE
            }
            else {
                alarmbetween.text = getString(R.string.alarm_in)
                alarmbetweenvalue.visibility = View.VISIBLE
                alarmbetweenvalue.text = minutes+" minutes"
            }

        else {
            alarmbetween.text = getString(R.string.alarm_in)
            alarmbetweenvalue.visibility = View.VISIBLE
            alarmbetweenvalue.text = hours+" heures "+minutes+" minutes"
        }
    }
}