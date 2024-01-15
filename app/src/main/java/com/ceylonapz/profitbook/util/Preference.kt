package com.ceylonapz.profitbook.util

import android.content.Context
import android.widget.Toast

fun saveLimitOrderValues(context: Context, valuesList: LinkedHashMap<String, Int>) {
    val sharedPrefs = context.getSharedPreferences("pb_prefs", Context.MODE_PRIVATE)

    with(sharedPrefs.edit()) {
        valuesList[OrderFields.TP.name]?.let { putInt(OrderFields.TP.name, it) }
        valuesList[OrderFields.SL.name]?.let { putInt(OrderFields.SL.name, it) }
        valuesList[OrderFields.USDT.name]?.let { putInt(OrderFields.USDT.name, it) }
        commit()
    }

    Toast.makeText(context, "Saved...!", Toast.LENGTH_SHORT).show()
}

fun getLimitOrderValues(context: Context): LinkedHashMap<String, Int> {
    val sharedPrefs = context.getSharedPreferences("pb_prefs", Context.MODE_PRIVATE)
    val markParams = LinkedHashMap<String, Int>()
    markParams[OrderFields.TP.name] = sharedPrefs.getInt(OrderFields.TP.name, 4)
    markParams[OrderFields.SL.name] = sharedPrefs.getInt(OrderFields.SL.name, 20)
    markParams[OrderFields.USDT.name] = sharedPrefs.getInt(OrderFields.USDT.name, 0)
    return markParams
}