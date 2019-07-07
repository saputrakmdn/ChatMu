package latihan.android.com.chatmu.db.db_net

import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import latihan.android.com.chatmu.db.db_model.FriendModel

class user {
    companion object{
        var firebaseAuth: FirebaseAuth? = null
        var user = initPeopleList()
        var facebookUserId =""

        private fun initPeopleList(): MutableList<FriendModel> {
            firebaseAuth = FirebaseAuth.getInstance()
            val user = firebaseAuth!!.currentUser
            val uName = user!!.displayName
            for (profile in user!!.providerData) {
                // check if the provider id matches "facebook.com"
                if (FacebookAuthProvider.PROVIDER_ID == profile.providerId) {
                    facebookUserId = profile.uid
                }
            }
            var user_po = mutableListOf<FriendModel>()
            user_po.add(
                FriendModel(
                    "${uName}",
                    "https://graph.facebook.com/$facebookUserId/picture?height=500"

            )
            )
            return user_po

        }

    }
}