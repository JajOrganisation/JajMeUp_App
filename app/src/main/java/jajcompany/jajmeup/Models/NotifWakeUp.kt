package jajcompany.jajmeup.Models

import java.util.*

data class NotifWakeUp(val notificationtype: String, val lien: String, val videoname: String, val whowokeup:String, val time: Date) {
    constructor(): this("Wakeup", "", "", "", Date(0))
}