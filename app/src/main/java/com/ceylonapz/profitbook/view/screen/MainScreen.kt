package com.ceylonapz.profitbook.view.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ceylonapz.profitbook.util.NotificationHelper
import com.ceylonapz.profitbook.view.MainActivity
import com.ceylonapz.profitbook.viewmodel.MainViewModel

@Composable
fun MainScreen(navController: NavHostController) {

    val mainVM: MainViewModel = viewModel()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {


        val context = LocalContext.current
        showToastMessage(mainVM, context)
        showNotificationMessage(mainVM, context)

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
                            ActionButton(MainActivity.order_buy, mainVM)
                            Spacer(modifier = Modifier.width(16.dp))
                            ActionButton(MainActivity.order_sell, mainVM)
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
                    onClick = { openBinance(context) }
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

                //settings button
                IconButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd),
                    onClick = { navController.navigate("settings") }
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        tint = lightWhite,
                        contentDescription = "Open Settings"
                    )
                }

            }
        }
    }
}

fun showNotificationMessage(mainVM: MainViewModel, context: Context) {
    if (mainVM.showNotification.value.first) {
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(
            "ProfitBook Trading",
            mainVM.showNotification.value.second
        )
    }
}

fun showToastMessage(mainVM: MainViewModel, context: Context) {
    if (mainVM.showToast.value.first) {
        Toast.makeText(context, mainVM.showToast.value.second, Toast.LENGTH_SHORT).show()
    }
}

private fun openBinance(context: Context) {
    val pkgName = "com.binance.dev"
    val intent = context.packageManager.getLaunchIntentForPackage(pkgName)

    if (intent != null) {
        context.startActivity(intent)
    } else {
        val webIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.binance.com/en/futures"))
        context.startActivity(webIntent)
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
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.Yellow
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

    val context = LocalContext.current

    val btnColor = if (btnName == MainActivity.order_buy) {
        Color(android.graphics.Color.parseColor("#1B5E20"))
    } else {
        Color(android.graphics.Color.parseColor("#D50000"))
    }

    Button(
        onClick = { mainVm.callBinanceTrade(btnName, context) },
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