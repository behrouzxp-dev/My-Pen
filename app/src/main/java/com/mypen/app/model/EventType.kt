package com.mypen.app.model

enum class EventType(val persianLabel: String) {
    ENTRY("ورود به موجودی"),
    OPENED("باز شدن قلم"),
    DEFECTIVE("ثبت خرابی"),
    EXPIRED("انقضای قلم"),
    EDITED("ویرایش مشخصات");

    companion object {
        fun fromString(str: String?): EventType {
            if (str == null) return ENTRY
            return when (str.trim().uppercase()) {
                "ENTRY", "ورود به موجودی" -> ENTRY
                "OPENED", "باز شدن قلم" -> OPENED
                "DEFECTIVE", "ثبت خرابی" -> DEFECTIVE
                "EXPIRED", "انقضای قلم" -> EXPIRED
                "EDITED", "ویرایش مشخصات" -> EDITED
                else -> ENTRY
            }
        }
    }
}
