package com.example.myspendyapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myspendyapp.data.db.dao.CategoryDao
import com.example.myspendyapp.data.db.dao.TransactionDao
import com.example.myspendyapp.data.db.dao.UserDao
import com.example.myspendyapp.data.db.entity.Category
import com.example.myspendyapp.data.db.entity.Transaction
import com.example.myspendyapp.data.db.entity.User

@Database(entities = [User::class, Category::class, Transaction::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spendy_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
