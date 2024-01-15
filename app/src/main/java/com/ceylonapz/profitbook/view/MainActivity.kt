package com.ceylonapz.profitbook.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.binance.connector.futures.client.exceptions.BinanceClientException
import com.binance.connector.futures.client.exceptions.BinanceConnectorException
import com.binance.connector.futures.client.impl.UMFuturesClientImpl
import com.ceylonapz.profitbook.ui.theme.ProfitBookTheme
import com.ceylonapz.profitbook.model.AccountInfo
import com.ceylonapz.profitbook.model.MarketInfo
import com.ceylonapz.profitbook.model.Order
import com.ceylonapz.profitbook.util.PrivateConfig
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : ComponentActivity() {

    companion object {
        val order_buy = "BUY"
        val order_sell = "SELL"
        val symbol = "ADAUSDT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfitBookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val statusRem = remember { mutableStateOf("Good Luck") }
                    val infoRem = remember { mutableStateOf("Loading...") }

                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { context ->

                                WebView.setWebContentsDebuggingEnabled(true)
                                WebView(context).apply {
                                    val webSettings: WebSettings = settings
                                    webSettings.javaScriptEnabled = true
                                    webSettings.useWideViewPort = true

                                    val layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    this.layoutParams = layoutParams

                                    val widgetUrl =
                                        "https://www.tradingview.com/chart/2ggYQWEq/"
                                    loadUrl(widgetUrl)
                                }
                            }
                        )

                        //Bottom Trade Info
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.BottomCenter)
                                .background(color = Color(0f, 0f, 0f, 0.7f))
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                AccountTextView(infoRem)
                                callBinanceInfo(infoRem, statusRem)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    ActionButton(order_buy, statusRem)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    ActionButton(order_sell, statusRem)
                                }

                                StatusTextView(statusRem)
                            }

                            val lightWhite = Color(0xFFF2F2F2)
                            val context = LocalContext.current

                            //open app button
                            IconButton(
                                modifier = Modifier
                                    .align(Alignment.TopStart),
                                onClick = { openBinance() }
                            ) {
                                Icon(
                                    Icons.Filled.ShoppingCart,
                                    tint = lightWhite,
                                    contentDescription = "Open Binance"
                                )
                            }

                            //refresh button
                            IconButton(
                                modifier = Modifier
                                    .align(Alignment.TopEnd),
                                onClick = { callBinanceInfo(infoRem, null) }
                            ) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    tint = lightWhite,
                                    contentDescription = "Reload Balance"
                                )
                            }
                        }


                    }

                }
            }
        }
    }

    private fun openBinance() {
        val pkgName = "com.binance.dev"
        val intent = packageManager.getLaunchIntentForPackage(pkgName)

        if (intent != null) {
            startActivity(intent)
        } else {
            val webIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.binance.com/en/futures"))
            startActivity(webIntent)
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
private fun callBinanceTrades() {
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val client =
                UMFuturesClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY)

            //market price
            val markParams = LinkedHashMap<String, Any>()
            markParams["symbol"] = MainActivity.symbol
            markParams["orderId"] = "38159326976"


            //get account info
            val runOrderResult: String = client.account().queryCurrentOpenOrder(markParams)
            println("runOrderResult " + runOrderResult)

        } catch (e: Exception) {
            Log.e("ApiCall: runOrderResult", e.message, e)
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
private fun callBinanceInfo(infoRem: MutableState<String>, statusRem: MutableState<String>?) {
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val client =
                UMFuturesClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY)

            //market price
            val markParams = LinkedHashMap<String, Any>()
            markParams["symbol"] = MainActivity.symbol
            val resultMark: String = client.market().markPrice(markParams)
            val marketData = Gson().fromJson(resultMark, MarketInfo::class.java)
            val originalBigDecimal = BigDecimal(marketData.markPrice)
            val entryPrice = originalBigDecimal.setScale(4, RoundingMode.HALF_UP)
            statusRem?.value = "ADAUSDT $entryPrice"

            //get account info
            val result: String = client.account().futuresAccountBalance(linkedMapOf())
            val usdtBalance = roundWithTwoDecimals(displayUsdtAvailableBalance(result).toDouble())
            infoRem.value = "USDT $usdtBalance"

        } catch (e: Exception) {
            infoRem.value = e.message.toString()
            Log.e("ApiCall: Info", e.message, e)
        }
    }
}

fun roundWithTwoDecimals(amount: Double): String {
    val bigDecimal = BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP)
    return bigDecimal.toString()
}

fun displayUsdtAvailableBalance(jsonString: String): String {
    val assetBalances = Gson().fromJson(jsonString, Array<AccountInfo>::class.java)
    val usdtBalance = assetBalances.find { it.asset == "USDT" }
    return if (usdtBalance != null) {
        usdtBalance.availableBalance
    } else {
        "0"
    }
}

@OptIn(DelicateCoroutinesApi::class)
private fun callBinanceTrade(selectedType: String, statusRem: MutableState<String>) {
    GlobalScope.launch(Dispatchers.IO) {
        val sBuilder = AnnotatedString.Builder()

        try {
            statusRem.value = "Connecting...."

            val client =
                UMFuturesClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY)

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


            val qty = 10 //ADA 40 = USDT 1
            val endDateTime = System.currentTimeMillis() + 10000
            val addProfit = BigDecimal("0.0004")
            val deductLoss = BigDecimal("0.0015")

            //GET MARKET PRICE
            val markParams = LinkedHashMap<String, Any>()
            markParams["symbol"] = MainActivity.symbol
            val result: String = client.market().markPrice(markParams)
            val marketData = Gson().fromJson(result, MarketInfo::class.java)
            val originalBigDecimal = BigDecimal(marketData.markPrice)
            val entryPrice = originalBigDecimal.setScale(4, RoundingMode.HALF_UP)
            val markPrice = entryPrice.toString()

            val markTP = if (selectedType == MainActivity.order_buy) {
                entryPrice.add(addProfit)
            } else {
                entryPrice.subtract(addProfit)
            }

            val markSL = if (selectedType == MainActivity.order_buy) {
                entryPrice.subtract(deductLoss)
            } else {
                entryPrice.add(deductLoss)
            }

            //MAKE ORDER
            val paramOrder = LinkedHashMap<String, Any>()
            paramOrder["symbol"] = MainActivity.symbol
            paramOrder["side"] = orderSide
            paramOrder["quantity"] = qty
            paramOrder["type"] = "LIMIT"
            paramOrder["price"] = markPrice
            paramOrder["timeinforce"] = "GTC"
            val resultOrder: String = client.account().newOrder(paramOrder)

            //TAKE PROFIT
            val paramOrderTP = LinkedHashMap<String, Any>()
            paramOrderTP["symbol"] = MainActivity.symbol
            paramOrderTP["side"] = orderReversSide
            paramOrderTP["type"] = "TAKE_PROFIT_MARKET"
            paramOrderTP["quantity"] = qty
            paramOrderTP["stopPrice"] = markTP
            paramOrderTP["timestamp"] = endDateTime
            paramOrderTP["closePosition"] = true
            val resultOrderTP: String = client.account().newOrder(paramOrderTP)

            //STOP LOSS
            val paramOrderSL = LinkedHashMap<String, Any>()
            paramOrderSL["symbol"] = MainActivity.symbol
            paramOrderSL["side"] = orderReversSide
            paramOrderSL["type"] = "STOP_MARKET"
            paramOrderSL["quantity"] = qty
            paramOrderSL["stopPrice"] = markSL
            paramOrderSL["timestamp"] = endDateTime
            paramOrderSL["closePosition"] = true
            val resultOrderSL: String = client.account().newOrder(paramOrderSL)

            //PRINT STATUS
            sBuilder.append(getOrderStatus(resultOrder))
            sBuilder.append(" | ")
            sBuilder.append(getOrderStatus(resultOrderTP))
            sBuilder.append("\n")
            sBuilder.append(getOrderStatus(resultOrderSL))
            statusRem.value = sBuilder.toAnnotatedString().text

        } catch (e: BinanceConnectorException) {
            sBuilder.append("\n")
            sBuilder.append(e.message.toString())
            statusRem.value = sBuilder.toAnnotatedString().text
            Log.e("ApiCall: Connect", e.message, e)
        } catch (e: BinanceClientException) {
            sBuilder.append("\n")
            sBuilder.append(e.message.toString())
            statusRem.value = sBuilder.toAnnotatedString().text
            Log.e("ApiCall: Client", e.message, e)
        }
    }
}

fun getOrderStatus(result: String): String {
    val orderData = Gson().fromJson(result, Order::class.java)

    val price = if (orderData.price == "0.00000") {
        orderData.stopPrice
    } else {
        orderData.price
    }

    val type = getShortFormatType(orderData.origType)
    return "$type ${orderData.side} ${formatPrice(price.toDouble())}"
}

fun getOrderPrice(result: String): String {
    val orderData = Gson().fromJson(result, Order::class.java)
    return orderData.price
}

fun formatPrice(number: Double): String {
    return String.format("%.4f", number)
}

fun getShortFormatType(type: String): String {
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


@Composable
fun AccountTextView(info: MutableState<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = info.value,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Green
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

@Composable
fun StatusTextView(status: MutableState<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = status.value,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White
            ),
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
fun ActionButton(btnName: String, statusRem: MutableState<String>, modifier: Modifier = Modifier) {

    val btnColor = if (btnName == MainActivity.order_buy) {
        Color(android.graphics.Color.parseColor("#1B5E20"))
    } else {
        Color(android.graphics.Color.parseColor("#D50000"))
    }

    Button(
        onClick = { callBinanceTrade(btnName, statusRem) },
        colors = ButtonDefaults.textButtonColors(
            contentColor = Color.White,
            containerColor = btnColor
        )
    ) {
        Text(
            text = btnName,
            modifier = modifier
        )
    }
}