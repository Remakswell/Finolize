package com.finolize.app.core.utils

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

fun Long.toFormattedDate(): String {
    val date = Date(this)
    return when {
        DateUtils.isToday(this) -> "Today, " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(date)
    }
}

fun Long.toGroupHeader(): String {
    return when {
        DateUtils.isToday(this) -> "Today"
        DateUtils.isToday(this + DateUtils.DAY_IN_MILLIS) -> "Yesterday"
        else -> SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(this)) }
}