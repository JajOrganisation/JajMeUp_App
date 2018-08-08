package jajcompany.jajmeup.Models

import java.util.*

data class Vote(val lien: String, val votant:String, val time:Date) {
    constructor(): this("", "", Date(0))
}