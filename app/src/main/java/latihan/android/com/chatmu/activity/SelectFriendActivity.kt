package  latihan.android.com.chatmu.activity

import android.os.Build
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import  latihan.android.com.chatmu.R
import  latihan.android.com.chatmu.adapters.FriendListAdapter
import  latihan.android.com.chatmu.data.ParseFirebaseData
import  latihan.android.com.chatmu.data.Tools
import  latihan.android.com.chatmu.model.Friend
import  latihan.android.com.chatmu.utilities.CustomToast
import  latihan.android.com.chatmu.widgets.DividerItemDecoration
import java.util.ArrayList



class SelectFriendActivity: AppCompatActivity() {
    private var actionBar: ActionBar? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: FriendListAdapter? = null
    internal lateinit var friendList: List<Friend>

    val USERS_CHILD = "users"
    internal lateinit var pfbd: ParseFirebaseData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_chat)
        initToolbar()
        initComponent()
        friendList = ArrayList<Friend>()
        pfbd = ParseFirebaseData(this)

        val ref = FirebaseDatabase.getInstance().getReference(USERS_CHILD)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // TODO: 25-05-2017 if number of items is 0 then show something else
                mAdapter = FriendListAdapter(this@SelectFriendActivity, pfbd.getAllUser(dataSnapshot))
                recyclerView!!.adapter = mAdapter

                mAdapter!!.setOnItemClickListener(object : FriendListAdapter.OnItemClickListener{
                    override fun onItemClick(view: View, obj: Friend, position: Int) {
                        ChatDetailsActivity.navigate(this@SelectFriendActivity, findViewById(R.id.lyt_parent), obj)
                    }
                })


                bindView()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                CustomToast(this@SelectFriendActivity).showError(getString(R.string.error_could_not_connect))
            }
        })

        // for system bar in lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Tools.systemBarLolipop(this)
        }
    }

    private fun initComponent() {
        recyclerView = findViewById(R.id.recyclerView) as RecyclerView

        // use a linear layout manager
        val mLayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST))
    }

    fun initToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        //        actionBar.setSubtitle(Constant.getFriendsData(this).size()+" friends");
    }

    fun bindView() {
        try {
            mAdapter!!.notifyDataSetChanged()
        } catch (e: Exception) {
        }

    }
}