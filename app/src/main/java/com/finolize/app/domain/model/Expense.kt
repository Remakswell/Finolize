package com.finolize.app.domain.model

import java.util.Date

data class Expense(
    val id: Long? = null,
    val amount: Double,
    val category: String,
    val date: Date,
    val description: String
)
