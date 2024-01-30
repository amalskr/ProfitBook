@file:OptIn(ExperimentalMaterial3Api::class)

package com.ceylonapz.profitbook.view.screen

import android.content.Context
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ceylonapz.profitbook.util.OrderFields
import com.ceylonapz.profitbook.util.getLimitOrderValues
import com.ceylonapz.profitbook.util.isScreenLightOn
import com.ceylonapz.profitbook.util.saveLimitOrderValues
import com.ceylonapz.profitbook.util.saveScreenLight
import com.ceylonapz.profitbook.viewmodel.MainViewModel

@Composable
fun SettingsScreen(navController: NavHostController) {

    val mainVM: MainViewModel = viewModel()

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FormContent()
            GeneralContent()
        }
    }
}

@Composable
fun GeneralContent() {

    val context = LocalContext.current
    var keepScreenOn by remember { mutableStateOf(isScreenLightOn(context)) }
    saveScreenLight(context, keepScreenOn)

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "General",
            style = MaterialTheme.typography.displaySmall
        )

        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Switch(
                checked = keepScreenOn,
                onCheckedChange = { newValue ->
                    keepScreenOn = newValue
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = if (keepScreenOn) "Keep Screen On" else "Keep Screen Off",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun FormContent() {

    val context = LocalContext.current
    val orderValues = getLimitOrderValues(context)

    var takeProfitValue by remember { mutableStateOf(orderValues[OrderFields.TP.name].toString()) }
    var stopLossValue by remember { mutableStateOf(orderValues[OrderFields.SL.name].toString()) }
    var usdtValue by remember { mutableStateOf(orderValues[OrderFields.USDT.name].toString()) }


    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Trade",
            style = MaterialTheme.typography.displaySmall
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = takeProfitValue,
            onValueChange = { newVal -> takeProfitValue = newVal },
            label = { Text("TakeProfit") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = stopLossValue,
            onValueChange = { newVal -> stopLossValue = newVal },
            label = { Text("StopLoss") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = usdtValue,
            onValueChange = { newVal -> usdtValue = newVal },
            label = { Text("USDT") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = {
                val tp = takeProfitValue.toInt()
                val sl = stopLossValue.toInt()
                val usdt = usdtValue.toInt()

                saveOrderValues(context, tp, sl, usdt)
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Save")
        }
    }
}

fun saveOrderValues(context: Context, tp: Int, sl: Int, usdt: Int) {
    val markParams = LinkedHashMap<String, Int>()
    markParams[OrderFields.TP.name] = tp
    markParams[OrderFields.SL.name] = sl
    markParams[OrderFields.USDT.name] = usdt

    saveLimitOrderValues(context, markParams)
}

@Composable
fun KeepScreenOnOff(keepScreenOn: Boolean) {
    val activity = LocalContext.current as? ComponentActivity

    // Set the FLAG_KEEP_SCREEN_ON flag when the switch is toggled
    DisposableEffect(keepScreenOn) {
        val window = activity?.window
        if (keepScreenOn && window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            // Remove the FLAG_KEEP_SCREEN_ON flag when the composable is disposed
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
