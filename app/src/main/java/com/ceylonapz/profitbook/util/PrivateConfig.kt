package com.ceylonapz.profitbook.util

import com.ceylonapz.profitbook.BuildConfig

/**
 * Binance API credentials, read from `local.properties` at build time:
 *
 *     BINANCE_API_KEY=...
 *     BINANCE_SECRET_KEY=...
 */
class PrivateConfig {
    companion object {
        const val API_KEY: String = BuildConfig.BINANCE_API_KEY
        const val SECRET_KEY: String = BuildConfig.BINANCE_SECRET_KEY
    }

}
