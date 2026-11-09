package com.mypen.app.model

import org.json.JSONObject
import java.io.Serializable

data class HistoryEvent(
    var id: Long = 0L,
    var penId: String = "",
    var eventType: EventType = EventType.ENTRY,
    var eventDate: String = "",
    var eventTime: String = "",
    var description: String = "",
    var penName: String? = null,
    var penType: InsulinType? = null
) : Serializable {

    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("penId", penId)
        obj.put("eventType", eventType.name)
        obj.put("eventDate", eventDate)
        obj.put("eventTime", eventTime)
        obj.put("description", description)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): HistoryEvent {
            return HistoryEvent(
                id = obj.optLong("id", 0L),
                penId = obj.getString("penId"),
                eventType = EventType.fromString(obj.optString("eventType", "ENTRY")),
                eventDate = obj.getString("eventDate"),
                eventTime = obj.optString("eventTime", "${obj.getString("eventDate")} 00:00:00"),
                description = obj.getString("description")
            )
        }
    }
}
