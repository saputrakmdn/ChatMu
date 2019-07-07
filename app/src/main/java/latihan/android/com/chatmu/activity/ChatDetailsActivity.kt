package latihan.android.com.chatmu.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import  latihan.android.com.chatmu.R
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat_details.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import latihan.android.com.chatmu.BottomSheet
import latihan.android.com.chatmu.StartApp
import  latihan.android.com.chatmu.adapters.ChatDetailListAdapter
import  latihan.android.com.chatmu.data.ParseFirebaseData
import  latihan.android.com.chatmu.data.SettingApi
import  latihan.android.com.chatmu.data.Tools
import  latihan.android.com.chatmu.model.ChatMessage
import  latihan.android.com.chatmu.model.Friend
import  latihan.android.com.chatmu.utilities.Const
import latihan.android.com.chatmu.utilities.Const.Companion.NODE_IMAGE
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_IS_READ
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_RECEIVER_ID
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_RECEIVER_NAME
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_RECEIVER_PHOTO
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_SENDER_ID
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_SENDER_NAME
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_SENDER_PHOTO
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_TEXT
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_TIMESTAMP
import  latihan.android.com.chatmu.utilities.CustomToast
import latihan.android.com.chatmu.widgets.CircleTransform
import java.util.ArrayList
import java.util.HashMap



class ChatDetailsActivity : AppCompatActivity() {


    companion object {
        var KEY_FRIEND = "FRIEND"

        fun navigate(activity: AppCompatActivity, transitionImage: View, obj: Friend) {
            val intent = Intent(activity, ChatDetailsActivity::class.java)
            intent.putExtra(KEY_FRIEND, obj)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionImage, KEY_FRIEND)
            ActivityCompat.startActivity(activity, intent, options.toBundle())
        }
        private const val LOCATION_PERMISSION = 1
    }

    // give preparation animation activity transition


    private var btn_send: Button? = null
    private var et_content: EditText? = null
    lateinit var mAdapter: ChatDetailListAdapter

    private var listview: ListView? = null
    private var actionBar: ActionBar? = null
    private var friend: Friend? = null
    private val items = ArrayList<ChatMessage>()
    private var parent_view: View? = null
    internal lateinit var pfbd: ParseFirebaseData
    internal lateinit var set: SettingApi


    internal lateinit var chatNode: String
    internal lateinit var chatNode_1:String
    internal lateinit var chatNode_2:String

    internal lateinit var ref: DatabaseReference
    internal lateinit var valueEventListener: ValueEventListener
    private val REQUEST_IMAGE = 2
    private val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation : Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_details)
        parent_view = findViewById(android.R.id.content)
        pfbd = ParseFirebaseData(this)
        set = SettingApi(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sheet_bottom.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE)
        }
        location.setOnClickListener {
            if(ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION)
            }
            fusedLocationClient.lastLocation.addOnSuccessListener {
                location ->
                if (location != null){
                    val lat = location.latitude
                    val long = location.longitude

                    et_content!!.setText("https://www.google.com/maps/@${lat},${long},20z")


                }


            }

        }
        // animation transition
        ViewCompat.setTransitionName(parent_view!!, KEY_FRIEND)

        // initialize conversation data
        val intent = intent
        friend = intent.extras!!.getSerializable(KEY_FRIEND) as Friend
        initToolbar()

        iniComponen()
        chatNode_1 = set.readSetting(Const.PREF_MY_ID) + "-" + friend!!.id
        chatNode_2 = friend!!.id + "-" + set.readSetting(Const.PREF_MY_ID)

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(Const.LOG_TAG, "Data changed from activity")
                if (dataSnapshot.hasChild(chatNode_1)) {
                    chatNode = chatNode_1
                } else if (dataSnapshot.hasChild(chatNode_2)) {
                    chatNode = chatNode_2
                } else {
                    chatNode = chatNode_1
                }
                items.clear()
                items.addAll(pfbd.getMessagesForSingleUser(dataSnapshot.child(chatNode)))

                //Here we are traversing all the messages and mark all received messages read

                for (data in dataSnapshot.child(chatNode).children) {
                    if (data.child(NODE_RECEIVER_ID).value!!.toString() == set.readSetting(Const.PREF_MY_ID)) {
                        data.child(NODE_IS_READ).ref.runTransaction(object : Transaction.Handler {
                            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                mutableData.value = true
                                return Transaction.success(mutableData)
                            }

                            override fun onComplete(
                                databaseError: DatabaseError?,
                                b: Boolean,
                                dataSnapshot: DataSnapshot?
                            ) {

                            }
                        })
                    }
                }

                // TODO: 12/09/18 Change it to recyclerview
                mAdapter = ChatDetailListAdapter(this@ChatDetailsActivity, items)
                listview!!.adapter = mAdapter
                listview!!.requestFocus()
                registerForContextMenu(listview)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                CustomToast(this@ChatDetailsActivity).showError(getString(R.string.error_could_not_connect))
            }
        }

        ref = FirebaseDatabase.getInstance().getReference(Const.MESSAGE_CHILD)
        ref.addValueEventListener(valueEventListener)

        // for system bar in lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Tools.systemBarLolipop(this)
        }
    }

    fun initToolbar() {
        val toolbar = findViewById(R.id.toolbar1) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(false)
        actionBar!!.setTitle("")
        titlebar.text = friend?.name
        Picasso.with(this@ChatDetailsActivity).load(friend?.photo).resize(100, 100).transform(CircleTransform()).into(usr_img)






    }

    fun iniComponen() {
        listview = findViewById(R.id.listview) as ListView
        btn_send = findViewById(R.id.btn_send) as Button
        et_content = findViewById(R.id.text_content) as EditText
        btn_send!!.setOnClickListener {
            //                ChatMessage im=new ChatMessage(et_content.getText().toString(), String.valueOf(System.currentTimeMillis()),friend.getId(),friend.getName(),friend.getPhoto());

            val hm = HashMap<String, Any>()
            hm.put(NODE_TEXT, et_content!!.text.toString())
            hm.put(NODE_TIMESTAMP, System.currentTimeMillis().toString())
            hm.put(NODE_RECEIVER_ID, friend!!.id)
            hm.put(NODE_RECEIVER_NAME, friend!!.name)
            hm.put(NODE_RECEIVER_PHOTO, friend!!.photo)
            hm.put(NODE_SENDER_ID, set.readSetting(Const.PREF_MY_ID))
            hm.put(NODE_SENDER_NAME, set.readSetting(Const.PREF_MY_NAME))
            hm.put(NODE_SENDER_PHOTO, set.readSetting(Const.PREF_MY_DP))
            hm.put(NODE_IS_READ, false)
            hm.put(NODE_IMAGE, "")

            ref.child(chatNode).push().setValue(hm)
            et_content!!.setText("")
            hideKeyboard()
        }
        et_content!!.addTextChangedListener(contentWatcher)
        if (et_content!!.length() == 0) {
            btn_send!!.visibility = View.GONE
        }
        hideKeyboard()
    }


    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private val contentWatcher = object : TextWatcher {
        override fun afterTextChanged(etd: Editable) {
            if (etd.toString().trim { it <= ' ' }.length == 0) {
                btn_send!!.visibility = View.GONE
            } else {
                btn_send!!.visibility = View.VISIBLE
            }
            //draft.setContent(etd.toString());
        }

        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}

        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        //Remove the listener, otherwise it will continue listening in the background
        //We have service to run in the background
        ref.removeEventListener(valueEventListener)
        super.onDestroy()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("tag", "onActivity Result"+ "requestcode=$requestCode"+ "result code = $resultCode")
        if (requestCode == REQUEST_IMAGE){
            if (resultCode == Activity.RESULT_OK){
                if(data != null){
                    val uri = data.data
                    Log.d("tag", "uri"+ uri!!.toString())
                    val hm = HashMap<String, Any>()
                    hm.put(NODE_TEXT, "Gambar")
                    hm.put(NODE_TIMESTAMP, System.currentTimeMillis().toString())
                    hm.put(NODE_RECEIVER_ID, friend!!.id)
                    hm.put(NODE_RECEIVER_NAME, friend!!.name)
                    hm.put(NODE_RECEIVER_PHOTO, friend!!.photo)
                    hm.put(NODE_SENDER_ID, set.readSetting(Const.PREF_MY_ID))
                    hm.put(NODE_SENDER_NAME, set.readSetting(Const.PREF_MY_NAME))
                    hm.put(NODE_SENDER_PHOTO, set.readSetting(Const.PREF_MY_DP))
                    hm.put(NODE_IS_READ, false)
                    hm.put(NODE_IMAGE, LOADING_IMAGE_URL)
                    val storageReference = FirebaseStorage.getInstance().getReference(set.readSetting(Const.PREF_MY_ID)).child(uri.lastPathSegment!!)
                    putImageInStorage(storageReference, uri)
//                    ref!!.child(chatNode).push().setValue(hm, object : DatabaseReference.CompletionListener{
//                        override fun onComplete(p0: DatabaseError?, p1: DatabaseReference) {
//                            if (p0 == null){
//                                val key = p1.key
//                                val storageReference = FirebaseStorage.getInstance().getReference(set.readSetting(Const.PREF_MY_ID)).child(key!!).child(uri.lastPathSegment!!)
//                                putImageInStorage(storageReference, uri, key)
//                            }else{
//                                Log.w("tag", "unable to write nessage"+"message to database", p0!!.toException())
//                            }
//                        }
//
//                    })
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }
    private fun putImageInStorage(storageReference:
                                  StorageReference, uri: Uri?) {
        storageReference.putFile(uri!!)
            .addOnCompleteListener(this@ChatDetailsActivity,
                object: OnCompleteListener<UploadTask.TaskSnapshot> {
                    override fun onComplete(task: Task<UploadTask
                    .TaskSnapshot>
                    ) {
                        if (task.isSuccessful) {
                            task.result!!.metadata!!
                                .reference!!.downloadUrl
                                .addOnCompleteListener(this@ChatDetailsActivity,
                                    object: OnCompleteListener<Uri> {
                                        override fun onComplete(task: Task<Uri>) {
                                            if (task.isSuccessful) {
                                                val hm = HashMap<String, Any>()
                                                hm.put(NODE_TEXT, "Gambar")
                                                hm.put(NODE_TIMESTAMP, System.currentTimeMillis().toString())
                                                hm.put(NODE_RECEIVER_ID, friend!!.id)
                                                hm.put(NODE_RECEIVER_NAME, friend!!.name)
                                                hm.put(NODE_RECEIVER_PHOTO, friend!!.photo)
                                                hm.put(NODE_SENDER_ID, set.readSetting(Const.PREF_MY_ID))
                                                hm.put(NODE_SENDER_NAME, set.readSetting(Const.PREF_MY_NAME))
                                                hm.put(NODE_SENDER_PHOTO, set.readSetting(Const.PREF_MY_DP))
                                                hm.put(NODE_IS_READ, false)
                                                hm.put(NODE_IMAGE, task.result!!.toString())
                                               ref!!.child(chatNode).push()
                                                    .setValue(hm)
                                            }
                                        }
                                    })
                        }else{
                            Log.w("tag", "Image upload" +
                                    " task was not successful.",
                                task.exception)
                        }
                    }
                })
    }

}