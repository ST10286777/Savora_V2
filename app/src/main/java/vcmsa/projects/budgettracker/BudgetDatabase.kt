package vcmsa.projects.budgettracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import vcmsa.projects.budgettracker.model.Budget
import vcmsa.projects.budgettracker.model.Category
import vcmsa.projects.budgettracker.model.Expense
import vcmsa.projects.budgettracker.model.User
import vcmsa.projects.budgettracker.model.UserProfile
import vcmsa.projects.budgettracker.util.Converters
import vcmsa.projects.budgettracker.dao.UserProfileDao

/*********
Title: <The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android>
Author: <Philipp Lackner>
Date: <2023>
Code version <version from video published in Mar 15, 2023>
Availability:<https://www.youtube.com/watch?v=bOd3wO0uFr8&t=1784s>
 Explaination: Video helped provide a basic setup for the Room Database and entities
 **********/
@Database(
    entities = [User::class, Category::class, Expense::class, Budget::class,UserProfile::class],
    version = 12
)
@TypeConverters(Converters::class)
abstract class BudgetDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun userProfileDao(): UserProfileDao


    companion object {
        @Volatile
        private var INSTANCE: BudgetDatabase? = null

        fun getDatabase(context: Context): BudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    "budget_database"
                )
                    .fallbackToDestructiveMigration()  // drop & recreate on schema change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
