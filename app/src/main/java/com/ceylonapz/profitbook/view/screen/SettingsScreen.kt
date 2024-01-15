@file:OptIn(ExperimentalMaterial3Api::class)

package com.ceylonapz.profitbook.view.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ceylonapz.profitbook.util.saveLimitOrderValues
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
