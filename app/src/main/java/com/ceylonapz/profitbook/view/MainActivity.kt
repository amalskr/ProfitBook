package com.ceylonapz.profitbook.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ceylonapz.profitbook.ui.theme.ProfitBookTheme
import com.ceylonapz.profitbook.view.screen.MainScreen
import com.ceylonapz.profitbook.view.screen.SettingsScreen

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
                AppNavController()
            }
        }
    }

    @Composable
    private fun AppNavController() {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable("main") {
                MainScreen(navController = navController)
            }

            composable("settings") {
                SettingsScreen(navController = navController)
            }
        }
    }
}

