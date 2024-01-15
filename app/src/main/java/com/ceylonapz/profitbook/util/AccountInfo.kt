package com.ceylonapz.profitbook.util

data class AccountInfo(
    val accountAlias: String,
    val asset: String,
    val balance: String,
    val crossWalletBalance: String,
    val crossUnPnl: String,
    val availableBalance: String,
    val maxWithdrawAmount: String,
    val marginAvailable: Boolean,
    val updateTime: Long
)