package jajcompany.jajmeup.models

import java.util.*

data class Vote(val lien: String, val videoname: String, val votant:String, val message: String, val time:Date) {
    constructor(): this("", "", "", "", Date(0))
}