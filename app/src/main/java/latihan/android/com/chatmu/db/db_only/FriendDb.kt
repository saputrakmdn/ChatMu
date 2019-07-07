package latihan.android.com.chatmu.db.db_only

import android.app.Application
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.os.AsyncTask
import latihan.android.com.chatmu.db.db_model.FriendModel
import latihan.android.com.chatmu.db.db_net.user

@Database(entities = [FriendModel::class], version = 1)
abstract class FriendDb:RoomDatabase() {
    abstract fun friendDao() : FriendDao
    companion object{
        private val lock = Any()
        private val DB_NAME = "friend.db"
        private var INSTANCE: FriendDb? = null
        fun getInstance(application: Application): FriendDb{
            synchronized(FriendDb.lock){
                if (FriendDb.INSTANCE == null){
                    FriendDb.INSTANCE =
                        Room.databaseBuilder(application,
                            FriendDb::class.java, FriendDb.DB_NAME)
                            .allowMainThreadQueries()
                            .addCallback(
                                object : RoomDatabase.Callback(){
                                    override fun onCreate(db: SupportSQLiteDatabase) {
                                        super.onCreate(db)
                                        FriendDb.INSTANCE?.let{
                                            FriendDb.prePopulated(it, user.user)
                                        }
                                    }
                                }
                            ).build()
                }
                return FriendDb.INSTANCE!!
            }
        }
        fun prePopulated(database: FriendDb, peopleList : List<FriendModel>){
            for(people in peopleList) {
                AsyncTask.execute { database.friendDao().insert(people) }
            }
        }
    }
}