package com.ceylonapz.profitbook.util

enum class OrderType(val type: String) {
    TP("TAKE_PROFIT_MARKET"),
    SL("STOP_MARKET"),
    LIMIT("LIMIT")
}

enum class OrderStatus {
    NEW,
    FILLED
}

enum class OrderFields {
    TP,
    SL,
    USDT
}