package latihan.android.com.chatmu

import android.app.Application
import latihan.android.com.chatmu.db.FriendRepo

class StartApp: Application() {
    fun getPeopleRepo() = FriendRepo(this)
}