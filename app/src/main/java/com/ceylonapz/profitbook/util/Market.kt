package com.ceylonapz.profitbook.util

import com.google.gson.annotations.SerializedName

data class MarketInfo(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("markPrice") val markPrice: String,
    @SerializedName("indexPrice") val indexPrice: String,
    @SerializedName("estimatedSettlePrice") val estimatedSettlePrice: String,
    @SerializedName("lastFundingRate") val lastFundingRate: String,
    @SerializedName("interestRate") val interestRate: String,
    @SerializedName("nextFundingTime") val nextFundingTime: Long,
    @SerializedName("time") val time: Long
)