package com.mypen.app.model

data class DashboardMetrics(
    val totalActive: Int = 0,
    val longActingCount: Int = 0,
    val shortActingCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0
)
