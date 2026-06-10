package com.finolize.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.finolize.app.data.local.dao.CategoryDao
import com.finolize.app.data.local.dao.ExpenseDao
import com.finolize.app.data.local.entity.CategoryEntity
import com.finolize.app.data.local.entity.ExpenseEntity

@Database(entities = [ExpenseEntity::class, CategoryEntity::class], version = 3)
abstract class FinolizeDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
}