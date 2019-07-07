package latihan.android.com.chatmu.db.db_only

import android.app.Application
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import latihan.android.com.chatmu.db.db_model.FriendModel

@Database(entities = [FriendModel::class], version = 1)
abstract class FriendDb:RoomDatabase() {
    abstract fun friendDao() : FriendDao
    companion object{
        private val lock = Any()
        private val DB_NAME = "friend.db"
        private var INSTANCE: FriendDb? = null
        fun getInstance(application: Application): FriendDb{
            synchronized(lock){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(
                        application, FriendDb::class.java, DB_NAME)
                        .allowMainThreadQueries()
                        .build()
                }
                return INSTANCE!!
            }
        }
    }
}