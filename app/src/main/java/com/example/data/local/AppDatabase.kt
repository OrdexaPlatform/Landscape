package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Client
import com.example.data.model.FinancialAccount
import com.example.data.model.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Client::class, Project::class, FinancialAccount::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun projectDao(): ProjectDao
    abstract fun financialAccountDao(): FinancialAccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "landscape_invoice_db"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    // Prepopulate with a main Cash Safe and a bank account
                    val dao = database.financialAccountDao()
                    dao.insertAccount(FinancialAccount(name = "الخزينة الرئيسية", balance = 150000.0, type = "SAFE"))
                    dao.insertAccount(FinancialAccount(name = "البنك الأهلي المصري", balance = 450000.0, type = "BANK"))
                    dao.insertAccount(FinancialAccount(name = "بنك مصر", balance = 200000.0, type = "BANK"))
                }
            }
        }
    }
}
