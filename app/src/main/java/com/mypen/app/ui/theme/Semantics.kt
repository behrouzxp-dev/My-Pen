package com.mypen.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.mypen.app.model.EventType
import com.mypen.app.model.InsulinType
import com.mypen.app.model.PenStatus

/**
 * Semantic colour mapping. Keeping it in one place guarantees that a status or
 * type always has the same colour in every screen.
 */

fun statusColor(status: PenStatus): Color = when (status) {
    PenStatus.ACTIVE -> StatusActive
    PenStatus.OPENED -> StatusOpened
    PenStatus.DEFECTIVE -> StatusDefective
    PenStatus.EXPIRED -> StatusExpired
}

fun statusBackground(status: PenStatus): Color = when (status) {
    PenStatus.ACTIVE -> StatusActiveBg
    PenStatus.OPENED -> StatusOpenedBg
    PenStatus.DEFECTIVE -> StatusDefectiveBg
    PenStatus.EXPIRED -> StatusExpiredBg
}

fun typeColor(type: InsulinType): Color = when (type) {
    InsulinType.LONG_ACTING -> TypeLongActing
    InsulinType.SHORT_ACTING -> TypeShortActing
}

fun typeBackground(type: InsulinType): Color = when (type) {
    InsulinType.LONG_ACTING -> TypeLongActingBg
    InsulinType.SHORT_ACTING -> TypeShortActingBg
}

fun eventColor(type: EventType): Color = when (type) {
    EventType.ENTRY -> StatusActive
    EventType.OPENED -> StatusOpened
    EventType.DEFECTIVE -> StatusDefective
    EventType.EXPIRED -> StatusExpired
    EventType.EDITED -> TypeLongActing
}

fun eventBackground(type: EventType): Color = when (type) {
    EventType.ENTRY -> StatusActiveBg
    EventType.OPENED -> StatusOpenedBg
    EventType.DEFECTIVE -> StatusDefectiveBg
    EventType.EXPIRED -> StatusExpiredBg
    EventType.EDITED -> TypeLongActingBg
}

/** Accent used for shelf-life indicators, based on remaining days. */
fun expiryAccent(daysRemaining: Long): Color = when {
    daysRemaining <= 0L -> StatusExpired
    daysRemaining <= 7L -> StatusExpiringSoon
    daysRemaining <= 30L -> StatusExpiringSoon
    else -> StatusActive
}
