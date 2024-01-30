package com.ceylonapz.profitbook.model

data class TradingPosition(
    val symbol: String,
    val positionAmt: Int,
    val entryPrice: Double,
    val breakEvenPrice: String,
    val markPrice: String,
    val unRealizedProfit: String,
    val liquidationPrice: String,
    val leverage: String,
    val maxNotionalValue: String,
    val marginType: String,
    val isolatedMargin: String,
    val isAutoAddMargin: Boolean,
    val positionSide: String,
    val notional: String,
    val isolatedWallet: String,
    val updateTime: Long,
    val isolated: Boolean,
    val adlQuantile: Int
)