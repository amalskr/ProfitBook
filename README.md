# Profit Book

An Android app for placing one-tap **Binance USDⓈ-M Futures** trades on `ADAUSDT`, with automatic take-profit and stop-loss orders and trade-status notifications.

Built with Kotlin and Jetpack Compose, using the official [binance-futures-connector-java](https://github.com/binance/binance-futures-connector-java) client.

> ⚠️ **This app places real orders on a live Binance Futures account.** Futures trading is high risk. Use it at your own risk, and test with small amounts first.

## Features

- **One-tap BUY / SELL.** Places a `LIMIT` entry order at the current mark price, together with a `TAKE_PROFIT_MARKET` and a `STOP_MARKET` order on the opposite side.
- **Automatic trade monitoring.** Polls open orders and positions every 2 seconds. When a trade finishes, or is left in an invalid state, it cancels the remaining orders.
- **Notifications.** Shows a system notification (with sound and vibration) when a trade closes in profit or loss, or is cancelled.
- **Account overview.** Shows the available USDT futures balance and the live ADA-USDT mark price.
- **Settings:**
  - Take-profit and stop-loss distances
  - Order size in USDT
  - Keep the screen on while the app is open
- **Quick link** to open the Binance app.

## How orders are calculated

| Setting    | Default | Meaning |
|------------|---------|---------|
| TakeProfit | `4`     | TP distance from entry, in units of 0.0001 USDT (`4` → 0.0004) |
| StopLoss   | `20`    | SL distance from entry, in units of 0.0001 USDT (`20` → 0.0020) |
| USDT       | `0`     | Order size. Quantity = `USDT × 40` ADA; `0` places the minimum of 10 ADA |

For a **BUY**, TP = entry + TP distance and SL = entry − SL distance. For a **SELL**, the directions are reversed.

The traded symbol is hard-coded as `ADAUSDT` in `view/MainActivity.kt`.

## Tech stack

- Kotlin 1.8.10, Jetpack Compose (Material 3, BOM 2023.03.00), Navigation Compose
- Android Gradle Plugin 8.1.3, Gradle 8.0
- Gson
- Binance Futures Connector Java 3.0.3
- minSdk 26, target/compile SDK 34

## Requirements

- Android Studio (Giraffe or newer)
- **JDK 17.** Gradle 8.0 supports Java 8–19 and AGP 8.x needs 17, so a newer JDK such as 21 or 25 will fail with *"Incompatible Gradle JVM version"*.
- A Binance account with Futures enabled, and an API key that has **Futures** permission

## Setup

1. **Clone**
   ```bash
   git clone https://github.com/amalskr/ProfitBook.git
   ```

2. **Add your Binance API keys** to `local.properties` in the project root. Git ignores this file, so the keys stay on your machine:
   ```properties
   BINANCE_API_KEY=your_api_key
   BINANCE_SECRET_KEY=your_secret_key
   ```
   At build time they go into `BuildConfig`, and `util/PrivateConfig.kt` reads them from there. If they're missing, the app still builds, but API calls fail. On Binance, turn on IP restrictions for the key and leave withdrawals disabled.

3. **Set the Gradle JDK to 17.** In Android Studio, go to *Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK*, then sync the project.

4. **Build and run**
   ```bash
   ./gradlew assembleDebug        # Windows: gradlew.bat assembleDebug
   ./gradlew installDebug         # install on a connected device
   ```

On Android 13+, allow the notification permission so trade alerts can appear.

## Project structure

```
app/src/main/java/com/ceylonapz/profitbook/
├── model/          # Gson models: AccountInfo, MarketInfo, Order, TradingPosition
├── ui/theme/       # Compose theme
├── util/
│   ├── Enums.kt              # OrderType, OrderStatus, OrderFields
│   ├── NotificationHelper.kt # Trade result notifications
│   ├── Preference.kt         # SharedPreferences (TP/SL/USDT, screen-on)
│   └── PrivateConfig.kt      # Binance API credentials (from BuildConfig)
├── view/
│   ├── MainActivity.kt       # Entry point, navigation, symbol constants
│   └── screen/               # MainScreen, SettingsScreen
└── viewmodel/
    └── MainViewModel.kt      # Binance calls, order placement, trade monitoring
```

## Disclaimer

This project is for personal and educational use. It is not financial advice. The author is not responsible for any trading losses.
