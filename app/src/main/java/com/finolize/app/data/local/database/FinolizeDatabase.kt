package com.finolize.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.finolize.app.data.local.dao.ExpenseDao
import com.finolize.app.data.local.entity.ExpenseEntity

@Database(entities = [ExpenseEntity::class], version = 1)
abstract class FinolizeDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}