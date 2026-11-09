package com.mypen.app.model

import org.json.JSONObject
import java.io.Serializable

data class NotificationSettings(
    var notify30Days: Boolean = true,
    var notify14Days: Boolean = true,
    var notify7Days: Boolean = true
) : Serializable {

    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("notify30Days", notify30Days)
        obj.put("notify14Days", notify14Days)
        obj.put("notify7Days", notify7Days)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject?): NotificationSettings {
            val settings = NotificationSettings()
            if (obj != null) {
                settings.notify30Days = obj.optBoolean("notify30Days", true)
                settings.notify14Days = obj.optBoolean("notify14Days", true)
                settings.notify7Days = obj.optBoolean("notify7Days", true)
            }
            return settings
        }
    }
}
