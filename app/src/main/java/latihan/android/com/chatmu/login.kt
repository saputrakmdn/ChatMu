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
import android.util.Log
import com.facebook.*
import com.facebook.login.widget.LoginButton
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        setContentView(R.layout.login)
        mobile = findViewById(R.id.et_login)
        facebookBtn = findViewById<View>(R.id.fb_btn) as LoginButton
        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()
        facebookBtn.setReadPermissions("email")
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
        btn_login.setOnClickListener(View.OnClickListener {
                no = mobile?.getText().toString()
                validNo(no!!)
                val intent = Intent(this@login, VerifyMobile::class.java)
                intent.putExtra("mobile", no)
                startActivity(intent)
                Toast.makeText(this@login, no, Toast.LENGTH_LONG).show()
            });
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
            if (task.isSuccessful){
                Log.d(TAG, "LoginCredentialSuccess")
                val user = firebaseAuth!!.currentUser
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }else{
                Log.w(TAG, "signin credential failed", task.exception)
                Toast.makeText(this, "created Account failed", Toast.LENGTH_LONG).show()
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