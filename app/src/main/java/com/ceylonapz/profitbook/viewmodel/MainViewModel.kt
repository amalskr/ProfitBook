package com.ceylonapz.profitbook.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binance.connector.futures.client.impl.UMFuturesClientImpl
import com.ceylonapz.profitbook.model.AccountInfo
import com.ceylonapz.profitbook.model.MarketInfo
import com.ceylonapz.profitbook.model.Order
import com.ceylonapz.profitbook.util.OrderFields
import com.ceylonapz.profitbook.util.OrderStatus
import com.ceylonapz.profitbook.util.OrderType
import com.ceylonapz.profitbook.util.PrivateConfig
import com.ceylonapz.profitbook.util.getLimitOrderValues
import com.ceylonapz.profitbook.view.MainActivity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class MainViewModel : ViewModel() {

    private var futureClient: UMFuturesClientImpl =
        UMFuturesClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY)

    var showToast = mutableStateOf(Pair(false, ""))
    var showNotification = mutableStateOf(Pair(false, ""))
    var accountBalance = mutableStateOf("USDT")
    var infoTxt = mutableStateOf("Loading...")
    var isTradeRunning = mutableStateOf(true)
    var tradeRunStatus = mutableStateOf("Checking...")

    private var orderIdTP: Long = 0
    private var orderIdSL: Long = 0
    private var orderIdLIMIT: Long = 0

    fun checkStatusClose() {
        viewModelScope.launch {
            while (isTradeRunning.value) {
                checkTradeStatusAndClose()
                delay(2000) // 2Sec
            }
        }
    }

    private fun checkTradeStatusAndClose() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var isOpenTP = false
                var isOpenSL = false
                var isOpenLIMIT = false

                val markParams = LinkedHashMap<String, Any>()
                markParams["symbol"] = MainActivity.symbol
                val result: String = futureClient.account().currentAllOpenOrders(markParams)
                val marketData: List<Order> =
                    Gson().fromJson(result, Array<Order>::class.java).toList()

                for (order in marketData) {
                    isOpenTP =
                        order.type == OrderType.TP.type && order.status == OrderStatus.NEW.name
                    isOpenSL =
                        order.type == OrderType.SL.type && order.status == OrderStatus.NEW.name
                    isOpenLIMIT =
                        order.type == OrderType.LIMIT.type && order.status == OrderStatus.NEW.name
                }

                /*
                * if LIMIT order is done, then close all of open trades
                * */
                if (marketData.isEmpty()) {
                    isTradeRunning.value = false
                } else if (marketData.size == 1) {
                    //close positions
                    cancelAllOpenOrders()
                    val tradeCloseStatus = if (isOpenTP) {
                        "Loss"
                    } else if (isOpenSL) {
                        "Profit"
                    } else {
                        ""
                    }

                    infoTxt.value = "Trade $tradeCloseStatus!"
                    showNotification.value = Pair(true, infoTxt.value)

                } else if (isOpenLIMIT && (!isOpenTP || !isOpenSL)) {
                    /*
                    * if isOpenTP and isOpenSL both are true then need to check is LIMIT order is running or not
                    * if LIMIT FILLED the trade is still running
                    *
                    * if LIMIT status is NEW and isOpenTP or isOpenSL both of one status is FILLED, then close all of trades
                    * */

                    //close positions
                    cancelAllOpenOrders()

                    if (!isOpenTP) {
                        cancelTradeOrder(orderIdTP)
                    }

                    /*if (!isOpenSL) {
                        cancelTradeOrder(orderIdSL)
                    }*/

                    infoTxt.value = "Invalid Trade closed...!"
                    showNotification.value = Pair(true, infoTxt.value)

                } else {
                    tradeRunStatus.value = "Trade is running..."
                }

            } catch (e: Exception) {
                tradeRunStatus.value = "Error " + e.message
                Log.e("trade status", e.toString())
            }
        }
    }

    private fun cancelAllOpenOrders() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val markParams = LinkedHashMap<String, Any>()
                markParams["symbol"] = MainActivity.symbol
                val result: String = futureClient.account().cancelAllOpenOrders(markParams)
                Log.d("trade status", "CancelAll " + result)
            } catch (e: Exception) {
                Log.e("trade status cancelAllOpenOrders", e.toString())
            }

            isTradeRunning.value = false
            callBinanceInfo(isReload = true)
        }
    }

    private fun cancelTradeOrder(orderId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val markParams = LinkedHashMap<String, Any>()
                markParams["symbol"] = MainActivity.symbol
                markParams["orderId"] = orderId
                val result: String = futureClient.account().cancelOrder(markParams)
                Log.d("trade status", "CancelTrade " + result)
            } catch (e: Exception) {
                Log.e("trade status", e.toString())
            }

            isTradeRunning.value = false
        }
    }

    fun callBinanceInfo(isReload: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!isReload) {
                    //market price
                    val markParams = LinkedHashMap<String, Any>()
                    markParams["symbol"] = MainActivity.symbol
                    val resultMark: String = futureClient.market().markPrice(markParams)
                    val marketData = Gson().fromJson(resultMark, MarketInfo::class.java)
                    val originalBigDecimal = BigDecimal(marketData.markPrice)
                    val entryPrice = originalBigDecimal.setScale(4, RoundingMode.HALF_UP)
                    infoTxt.value = "ADA-USDT $entryPrice"
                }

                //get account info
                val result: String = futureClient.account().futuresAccountBalance(linkedMapOf())
                val usdtBalance =
                    roundWithTwoDecimals(displayUsdtAvailableBalance(result).toDouble())
                accountBalance.value = "USDT $usdtBalance"

            } catch (e: Exception) {
                infoTxt.value = e.message.toString()
                Log.e("ApiCall: Info", e.message, e)
            }
        }
    }

    fun callBinanceTrade(selectedType: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val sBuilder = AnnotatedString.Builder()

            try {
                infoTxt.value = "Connecting...."

                val orderSide = if (selectedType == MainActivity.order_buy) {
                    MainActivity.order_buy
                } else {
                    MainActivity.order_sell
                }

                val orderReversSide = if (selectedType == MainActivity.order_buy) {
                    MainActivity.order_sell
                } else {
                    MainActivity.order_buy
                }

                val orderValues = getLimitOrderValues(context)

                val savedTP = orderValues[OrderFields.TP.name]
                val savedSL = orderValues[OrderFields.SL.name]
                val qty = getAdaQty(orderValues[OrderFields.USDT.name])

                val endDateTime = System.currentTimeMillis() + 10000
                val addProfit = savedTP?.let { BigDecimal(it).divide(BigDecimal(10000)) }
                val deductLoss = savedSL?.let { BigDecimal(it).divide(BigDecimal(10000)) }

                //GET MARKET PRICE
                val markParams = LinkedHashMap<String, Any>()
                markParams["symbol"] = MainActivity.symbol
                val result: String = futureClient.market().markPrice(markParams)
                val marketData = Gson().fromJson(result, MarketInfo::class.java)
                val originalBigDecimal = BigDecimal(marketData.markPrice)
                val entryPrice = originalBigDecimal.setScale(4, RoundingMode.HALF_UP)
                val markPrice = entryPrice.toString()

                //MAKE ORDER
                val paramOrder = LinkedHashMap<String, Any>()
                paramOrder["symbol"] = MainActivity.symbol
                paramOrder["side"] = orderSide
                paramOrder["quantity"] = qty
                paramOrder["type"] = "LIMIT"
                paramOrder["price"] = markPrice
                paramOrder["timeinforce"] = "GTC"
                val resultOrder: String = futureClient.account().newOrder(paramOrder)

                //TAKE PROFIT
                val markTP = if (selectedType == MainActivity.order_buy) {
                    entryPrice.add(addProfit)
                } else {
                    entryPrice.subtract(addProfit)
                }

                val paramOrderTP = LinkedHashMap<String, Any>()
                paramOrderTP["symbol"] = MainActivity.symbol
                paramOrderTP["side"] = orderReversSide
                paramOrderTP["type"] = "TAKE_PROFIT_MARKET"
                paramOrderTP["quantity"] = qty
                paramOrderTP["stopPrice"] = markTP
                paramOrderTP["timestamp"] = endDateTime
                paramOrderTP["closePosition"] = true
                val resultOrderTP: String = futureClient.account().newOrder(paramOrderTP)

                //STOP LOSS
                var resultOrderSL  = "NO SL"

                if (savedSL != null && savedSL > 0) {

                    val markSL = if (selectedType == MainActivity.order_buy) {
                        entryPrice.subtract(deductLoss)
                    } else {
                        entryPrice.add(deductLoss)
                    }

                    val paramOrderSL = LinkedHashMap<String, Any>()
                    paramOrderSL["symbol"] = MainActivity.symbol
                    paramOrderSL["side"] = orderReversSide
                    paramOrderSL["type"] = "STOP_MARKET"
                    paramOrderSL["quantity"] = qty
                    paramOrderSL["stopPrice"] = markSL
                    paramOrderSL["timestamp"] = endDateTime
                    paramOrderSL["closePosition"] = true
                    resultOrderSL = futureClient.account().newOrder(paramOrderSL)
                }

                //PRINT STATUS
                sBuilder.append(getOrderStatus(resultOrder))
                sBuilder.append(" | ")
                sBuilder.append(getOrderStatus(resultOrderTP))
                sBuilder.append("\n")
                sBuilder.append(getOrderStatus(resultOrderSL))
                infoTxt.value = sBuilder.toAnnotatedString().text

                isTradeRunning.value = true
                checkStatusClose()

            } catch (e: Exception) {
                isTradeRunning.value = false

                sBuilder.append("\n")
                sBuilder.append(e.message.toString())
                infoTxt.value = sBuilder.toAnnotatedString().text
                Log.e("ApiCall: Client", e.message, e)
            }
        }
    }

    private fun getAdaQty(usdt: Int?): Int {
        return if (usdt != null) {
            if (usdt == 0) {
                10
            } else {
                usdt * 40
            }
        } else {
            10
        }
    }

    private fun getOrderStatus(result: String): String {
        val orderData = Gson().fromJson(result, Order::class.java)

        when (orderData.type) {
            OrderType.LIMIT.type -> orderIdLIMIT = orderData.orderId
            OrderType.TP.type -> orderIdTP = orderData.orderId
            OrderType.SL.type -> orderIdSL = orderData.orderId
        }

        val price = if (orderData.price == "0.00000") {
            orderData.stopPrice
        } else {
            orderData.price
        }

        val type = getShortFormatType(orderData.origType)
        return "$type ${formatPrice(price.toDouble())}"
    }

    private fun formatPrice(number: Double): String {
        return String.format("%.4f", number)
    }

    private fun getShortFormatType(type: String): String {
        return if (type.contains("_")) {
            val words = type.split("_")
            when (val shortType = words.joinToString("") { it.first().uppercase() }) {
                "TPM" -> "PROFIT"
                "SM" -> "STOP"
                else -> shortType
            }
        } else {
            type
        }
    }

    private fun roundWithTwoDecimals(amount: Double): String {
        val bigDecimal = BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP)
        return bigDecimal.toString()
    }

    private fun displayUsdtAvailableBalance(jsonString: String): String {
        val assetBalances = Gson().fromJson(jsonString, Array<AccountInfo>::class.java)
        val usdtBalance = assetBalances.find { it.asset == "USDT" }
        return if (usdtBalance != null) {
            usdtBalance.availableBalance
        } else {
            "0"
        }
    }
}