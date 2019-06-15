package latihan.android.com.chatmu

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.verifotp.*
import com.google.android.gms.tasks.TaskExecutors
import javax.xml.datatype.DatatypeConstants.SECONDS
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import android.widget.Toast

import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

import android.content.Intent

import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import android.util.Log.w
import com.google.android.gms.tasks.OnCompleteListener






class VerifyMobile: AppCompatActivity() {
    var otp: EditText? = null
    var login: Button? = null
    var no: String? = null
    private lateinit var mAuth: FirebaseAuth
    private var mVerificationId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verifotp)
        otp = findViewById(R.id.otp)
        this.mAuth = FirebaseAuth.getInstance()


        no = getIntent().getStringExtra("mobile");

        sendVerificationCode(no!!);
     
        loginotp.setOnClickListener(View.OnClickListener {
            val code = otp?.getText().toString().trim()
            if (code.isEmpty() || code.length < 6) {
                otp?.setError("Enter valid code")
                otp?.requestFocus()
                return@OnClickListener
            }

            //verifying the code entered manually
            verifyVerificationCode(code)
        })

    }
    private fun sendVerificationCode(no: String) {
        val phoneAuthProvider = PhoneAuthProvider.getInstance()
        phoneAuthProvider.verifyPhoneNumber(
            "+62$no",
            60,
            TimeUnit.SECONDS,
            this,
            mCallbacks
        )
    }
    private val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {

            //Getting the code sent by SMS
            val code = phoneAuthCredential.smsCode

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                otp?.setText(code)
                //verifying the code
                verifyVerificationCode(code)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@VerifyMobile, e.message, Toast.LENGTH_LONG).show()
            w("TAG Error", e.message)
        }

        override fun onCodeSent(s: String?, forceResendingToken: PhoneAuthProvider.ForceResendingToken?) {
            super.onCodeSent(s, forceResendingToken)

            //storing the verification id that is sent to the user
            mVerificationId = s
        }
    }


    private fun verifyVerificationCode(code: String) {
        //creating the credential
        val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)

        //signing the user
        signInWithPhoneAuthCredential(credential)
    }
     private fun signInWithPhoneAuthCredential(credential:PhoneAuthCredential) {
mAuth.signInWithCredential(credential)
.addOnCompleteListener(this@VerifyMobile
) { task ->
    if (task.isSuccessful) {
        //verification successful we will start the profile activity
        val intent = Intent(this@VerifyMobile, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

    } else {

        //verification unsuccessful.. display an error message

        var message = "Somthing is wrong, we will fix it soon..."

        if (task.exception is FirebaseAuthInvalidCredentialsException) {
            message = "Invalid code entered..."
        }


    }
}
     }
}