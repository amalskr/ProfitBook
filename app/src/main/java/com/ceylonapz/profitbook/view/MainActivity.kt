package com.ceylonapz.profitbook.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ceylonapz.profitbook.ui.theme.ProfitBookTheme
import com.ceylonapz.profitbook.viewmodel.MainViewModel

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
                MainContent()
            }
        }
    }

    @Composable
    private fun MainContent() {

        val mainVM: MainViewModel = viewModel()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

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

                        AccountTextView(mainVM)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (!mainVM.isTradeRunning.value) {
                                ActionButton(order_buy, mainVM)
                                Spacer(modifier = Modifier.width(16.dp))
                                ActionButton(order_sell, mainVM)
                            } else {
                                RunningTextView(mainVM)
                            }
                        }

                        StatusTextView(mainVM)
                    }

                    val lightWhite = Color(0xFFF2F2F2)

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
                        onClick = { mainVM.callBinanceInfo(isReload = true) }
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


@Composable
fun AccountTextView(mainVm: MainViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = mainVm.accountBalance.value,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Green
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

@Composable
fun RunningTextView(mainVm: MainViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = mainVm.tradeRunStatus.value,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White
            ),
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
fun StatusTextView(mainVm: MainViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = mainVm.infoTxt.value,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White
            ),
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
fun ActionButton(btnName: String, mainVm: MainViewModel, modifier: Modifier = Modifier) {

    val btnColor = if (btnName == MainActivity.order_buy) {
        Color(android.graphics.Color.parseColor("#1B5E20"))
    } else {
        Color(android.graphics.Color.parseColor("#D50000"))
    }

    Button(
        onClick = { mainVm.callBinanceTrade(btnName) },
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