package com.mypen.app.model

enum class PenStatus(val persianLabel: String) {
    ACTIVE("موجود"),
    OPENED("بازشده"),
    DEFECTIVE("معیوب"),
    EXPIRED("منقضی‌شده");

    companion object {
        fun fromString(str: String?): PenStatus {
            if (str == null) return ACTIVE
            return when (str.trim().uppercase()) {
                "ACTIVE", "موجود" -> ACTIVE
                "OPENED", "بازشده" -> OPENED
                "DEFECTIVE", "معیوب" -> DEFECTIVE
                "EXPIRED", "منقضی‌شده" -> EXPIRED
                else -> ACTIVE
            }
        }
    }
}
