package com.mypen.app.model

enum class InsulinType(val persianLabel: String) {
    LONG_ACTING("بلنداثر"),
    SHORT_ACTING("کوتاه‌اثر");

    companion object {
        fun fromString(str: String?): InsulinType {
            if (str == null) return LONG_ACTING
            val s = str.trim()
            return when {
                s.equals("LONG_ACTING", ignoreCase = true) || s == "بلنداثر" -> LONG_ACTING
                s.equals("SHORT_ACTING", ignoreCase = true) || s == "کوتاه‌اثر" -> SHORT_ACTING
                else -> LONG_ACTING
            }
        }
    }
}
