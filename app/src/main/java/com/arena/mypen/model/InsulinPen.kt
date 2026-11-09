package com.arena.mypen.model

import org.json.JSONObject
import java.io.Serializable

data class InsulinPen(
    var id: String = "",
    var name: String = "",
    var type: InsulinType = InsulinType.LONG_ACTING,
    var expiryDate: String = "",
    var status: PenStatus = PenStatus.ACTIVE,
    var entryDate: String = "",
    var openedDate: String? = null,
    var defectiveDate: String? = null,
    var defectiveReason: String? = null,
    var priority: Int = 1,
    var daysRemaining: Long = 0L
) : Serializable {

    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("type", type.name)
        obj.put("expiryDate", expiryDate)
        obj.put("status", status.name)
        obj.put("entryDate", entryDate)
        obj.put("openedDate", openedDate ?: JSONObject.NULL)
        obj.put("defectiveDate", defectiveDate ?: JSONObject.NULL)
        obj.put("defectiveReason", defectiveReason ?: JSONObject.NULL)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): InsulinPen {
            return InsulinPen(
                id = obj.getString("id"),
                name = obj.getString("name"),
                type = InsulinType.fromString(obj.optString("type", "LONG_ACTING")),
                expiryDate = obj.getString("expiryDate"),
                status = PenStatus.fromString(obj.optString("status", "ACTIVE")),
                entryDate = obj.getString("entryDate"),
                openedDate = if (!obj.isNull("openedDate")) obj.optString("openedDate") else null,
                defectiveDate = if (!obj.isNull("defectiveDate")) obj.optString("defectiveDate") else null,
                defectiveReason = if (!obj.isNull("defectiveReason")) obj.optString("defectiveReason") else null
            )
        }
    }
}
