package latihan.android.com.chatmu.db.db_model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class FriendModel(
    var name : String = "",
    var photo : String = "",
    @PrimaryKey(autoGenerate = true)var id: Int = 0
)