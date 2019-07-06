package latihan.android.com.chatmu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import latihan.android.com.chatmu.model.ChatMessage
import latihan.android.com.chatmu.model.Friend


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BottomSheet: BottomSheetDialogFragment() {
    private val REQUEST_IMAGE = 2
    private val TAG = "Image"
    private val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    val ANONYMOUS= "anonymous"
    private var friend: Friend? = null
    private var friendId : String? = null
    var friendName: String? = null
    var friendPhoto: String? = null
    var senderId: String? =null
    var senderName: String? = null
    var senderPhoto: String? = null
    var isRead: Boolean? = null
    var imageUrl: String? = null
    var value = 0.0
    private var mFirebaseDatabaseReference: DatabaseReference? = null
    private var mBottomSheetListener: BottomSheetListener?=null
    private var mFirebaseUser: FirebaseUser? = null
    private var storage : StorageReference? = null
    lateinit var filepath : Uri
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.bottom_sheet, container, false)
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        v.send_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE)
            dismiss()
        }
        v.send_location.setOnClickListener {
            Toast.makeText(context, "send", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        return v
    }
    interface BottomSheetListener{
        fun onOptionClick(text: String)
    }
    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            mBottomSheetListener = context as BottomSheetListener?
        }
        catch (e: ClassCastException){
            throw ClassCastException(context!!.toString())
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE){
            if (resultCode == Activity.RESULT_OK){
                if (data != null){
                    val uri = data.data
                    Log.d(TAG, "uri"+uri!!.toString())
                    val tempMessage = ChatMessage(null, null, friendId!!, friendName!!, friendPhoto!!, senderId!!, senderName!!, senderPhoto!!, isRead!!, LOADING_IMAGE_URL)
                    mFirebaseDatabaseReference!!.child("messages").push().setValue(tempMessage, object : DatabaseReference.CompletionListener{
                        override fun onComplete(p0: DatabaseError?, p1: DatabaseReference) {
                            if (p0 == null){
                                val key = p1.key

                                val storageReference = FirebaseStorage.getInstance().getReference().child(key!!).child(uri.lastPathSegment!!)
                                putImageInStorage(storageReference, uri, key)
                            }
                            else{
                                Log.w(TAG, "unable to write nessage"+"message to database", p0!!.toException())
                            }

                        }

                    })

                }
            }
        }
    }
    private fun putImageInStorage(storageReference:
                                  StorageReference, uri: Uri?, key:String?) {
        storageReference.putFile(uri!!)
            .addOnCompleteListener(
                Activity(),
                object: OnCompleteListener<UploadTask.TaskSnapshot> {
                    override fun onComplete(task: Task<UploadTask
                    .TaskSnapshot>) {
                        if (task.isSuccessful) {
                            task.result!!.metadata!!
                                .reference!!.downloadUrl
                                .addOnCompleteListener(
                                    Activity(),
                                    object: OnCompleteListener<Uri> {
                                        override fun onComplete(task: Task<Uri>) {
                                            if (task.isSuccessful) {
                                                val friendlyMessage = ChatMessage(null, null, friendId!!, friendName!!, friendPhoto!!, senderId!!, senderName!!, senderPhoto!!, isRead!!, task.result!!.toString())
                                                mFirebaseDatabaseReference!!
                                                    .child("messages").child(key!!)
                                                    .setValue(friendlyMessage)
                                            }
                                        }
                                    })
                        }else{
                            Log.w(TAG, "Image upload" +
                                    " task was not successful.",
                                task.exception)
                        }
                    }
                })
    }
}