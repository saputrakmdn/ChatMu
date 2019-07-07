package latihan.android.com.chatmu.db.db_only

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import latihan.android.com.chatmu.db.db_model.FriendModel

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(friend: FriendModel)
//    @Query("Select * from friend where id = :id")
//    fun find (id: Int): LiveData<FriendModel>
}