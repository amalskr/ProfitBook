package com.ceylonapz.profitbook.util

data class ExchangeInfo(
    val timezone: String,
    val serverTime: Long,
    val rateLimits: List<RateLimit>
)

data class RateLimit(
    val rateLimitType: String,
    val interval: String,
    val intervalNum: Int,
    val limit: Int,
)
