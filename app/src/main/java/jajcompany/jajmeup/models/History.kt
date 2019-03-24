package jajcompany.jajmeup.models

import java.util.*

data class History(var lien: String, var videoname: String, var whowokeup:String, var time: Date) {
    constructor(): this("", "", "", Date(0))
}