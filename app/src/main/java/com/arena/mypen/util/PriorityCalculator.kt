package com.arena.mypen.util

import com.arena.mypen.model.InsulinPen

object PriorityCalculator {

    fun assignPrioritiesAndDays(pens: MutableList<InsulinPen>, today: String) {
        if (pens.isEmpty()) return

        // 1. Sort by expiry date ascending, then name, then id
        pens.sortWith(
            compareBy<InsulinPen> { it.expiryDate }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                .thenBy { it.id }
        )

        // 2. Assign consumption priority rank (equal expiry = equal priority rank)
        var currentRank = 1
        var lastExpiry: String? = null

        for (pen in pens) {
            val days = DateUtils.calculateDaysRemaining(pen.expiryDate, today)
            pen.daysRemaining = days

            if (lastExpiry == null) {
                currentRank = 1
                lastExpiry = pen.expiryDate
            } else if (pen.expiryDate != lastExpiry) {
                currentRank++
                lastExpiry = pen.expiryDate
            }

            pen.priority = currentRank
        }
    }
}
