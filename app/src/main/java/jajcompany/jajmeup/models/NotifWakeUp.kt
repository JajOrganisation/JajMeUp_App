package jajcompany.jajmeup.models

import java.util.*

data class NotifWakeUp(var notificationtype: String, var lien: String, var videoname: String, var whowokeup:String, var timeNotif: Date, var status: String) {
    constructor(): this("Wakeup", "", "", "", Date(0), "")
}