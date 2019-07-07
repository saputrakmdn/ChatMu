package latihan.android.com.chatmu

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.R.string.no
import android.content.Intent
import android.R.attr.button
import android.view.View
import kotlinx.android.synthetic.main.login.*
import android.R.string.no
import android.os.Handler
import android.util.Log
import android.util.Log.e
import com.facebook.*
import com.facebook.login.widget.LoginButton
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.database.*
import com.google.firebase.internal.FirebaseAppHelper.getUid

import com.google.firebase.auth.UserInfo
import com.google.firebase.auth.FirebaseUser
import latihan.android.com.chatmu.activity.ChatDetailsActivity
import latihan.android.com.chatmu.activity.MainActivity
import latihan.android.com.chatmu.data.SettingApi
import latihan.android.com.chatmu.db.db_model.FriendModel
import latihan.android.com.chatmu.utilities.Const
import latihan.android.com.chatmu.utilities.Const.Companion.NODE_ID
import latihan.android.com.chatmu.utilities.Const.Companion.NODE_NAME
import latihan.android.com.chatmu.utilities.Const.Companion.NODE_PHOTO


class login: AppCompatActivity() {
    lateinit var facebookBtn : LoginButton
    val TAG = "Create Account"
    val FB_RC_SIGN = 2
    var googleApiClient: GoogleApiClient? = null
    // Firebase Auth Object.
    var firebaseAuth: FirebaseAuth? = null
    var callbackManager: CallbackManager? = null
    var mobile: EditText? = null
    var button: Button? = null
    var no: String? = null
    internal lateinit var set: SettingApi
     lateinit var ref: DatabaseReference
    val USERS_CHILD = "users"
    var facebookUserId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        setContentView(R.layout.login)
        set = SettingApi(this)

        facebookBtn = findViewById<View>(R.id.fb_btn) as LoginButton
        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()
        ref = FirebaseDatabase.getInstance().getReference(USERS_CHILD)
        facebookBtn.setReadPermissions("public_profile", "email")

        facebookBtn.registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)

            }

            override fun onCancel() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onError(error: FacebookException?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
        if (firebaseAuth!!.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken) {
        Log.d(TAG, "facebookaccesToken"+accessToken)
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        firebaseAuth!!.signInWithCredential(credential).addOnCompleteListener(this) {
            task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }else{
                Log.d(TAG, "LoginCredentialSuccess")

                val user = firebaseAuth!!.currentUser
                for (profile in user!!.providerData) {
                    // check if the provider id matches "facebook.com"
                    if (FacebookAuthProvider.PROVIDER_ID == profile.providerId) {
                        facebookUserId = profile.uid
                    }
                }


                    ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                         val usrName = user.displayName
                         val userId =  user.uid


                        val photoUrl = "https://graph.facebook.com/$facebookUserId/picture?height=500";
                            e("TAGERR", "$photoUrl")
                        set.addUpdateSettings(Const.PREF_MY_ID, userId!!)
                        set.addUpdateSettings(Const.PREF_MY_NAME, usrName!!)
                        set.addUpdateSettings(Const.PREF_MY_DP, photoUrl)

                        if (!p0 .hasChild(userId!!)) {
                            ref.child("$userId/$NODE_NAME").setValue(usrName)
                            ref.child("$userId/$NODE_PHOTO").setValue(photoUrl)
                            ref.child("$userId/$NODE_ID").setValue(userId)
                        }

                        val intent = Intent(this@login, MainActivity::class.java)
                        startActivity(intent)


                    }

                })


            }
        }

    }



    private fun validNo(no: String) {
        if(no.isEmpty() || no.length < 10){
            mobile?.setError("Enter a valid mobile")
            mobile?.requestFocus()
            return
        }
    }
}