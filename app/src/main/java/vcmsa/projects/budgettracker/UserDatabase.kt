package vcmsa.projects.budgettracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import vcmsa.projects.budgettracker.model.User // Import the User model


@Database(entities = [User::class], version = 1)
abstract class UserDatabase : RoomDatabase() {

    // Reference to the DAO
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        // Singleton pattern for database instance
        fun getDatabase(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_database" // Name of the database
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
