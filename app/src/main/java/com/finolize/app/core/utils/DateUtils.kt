package com.finolize.app.core.utils

import android.content.Context
import android.text.format.DateUtils
import com.finolize.app.R
import java.text.SimpleDateFormat
import java.util.*

fun Long.toFormattedDate(context: Context): String {
    val date = Date(this)
    return when {
        DateUtils.isToday(this) -> {
            val today = context.getString(R.string.today)
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            "$today, $time"
        }
        else -> SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(date)
    }
}

fun Long.toFormattedDateOnly(context: Context): String {
    return when {
        DateUtils.isToday(this) -> context.getString(R.string.today)
        DateUtils.isToday(this + DateUtils.DAY_IN_MILLIS) -> context.getString(R.string.yesterday)
        else -> SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(this))
    }
}

fun Long.toFormattedTimeOnly(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))
}