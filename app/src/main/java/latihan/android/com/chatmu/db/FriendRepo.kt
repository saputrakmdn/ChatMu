package latihan.android.com.chatmu.db

import android.app.Application
import android.arch.lifecycle.LiveData
import latihan.android.com.chatmu.db.db_model.FriendModel
import latihan.android.com.chatmu.db.db_only.FriendDao
import latihan.android.com.chatmu.db.db_only.FriendDb

class FriendRepo(application: Application) {
    private val friendDao : FriendDao
    init {
        val friendDb = FriendDb.getInstance(application)
        friendDao = friendDb.friendDao()
    }
    fun insertFriend(friend : FriendModel){
        friendDao.insert(friend)
    }
//    fun findFriend(id: Int): LiveData<FriendModel>{
//        return friendDao.find(id)
//    }
}