package  latihan.android.com.chatmu.activity

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import  latihan.android.com.chatmu.R
import latihan.android.com.chatmu.StartApp
import latihan.android.com.chatmu.data.SettingApi
import  latihan.android.com.chatmu.data.Tools
import latihan.android.com.chatmu.db.db_model.FriendModel
import  latihan.android.com.chatmu.fragment.ChatsFragment
import latihan.android.com.chatmu.login
import  latihan.android.com.chatmu.services.NotificationService
import latihan.android.com.chatmu.utilities.Const
import  latihan.android.com.chatmu.utilities.CustomToast

class MainActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    lateinit var fab: FloatingActionButton
    internal lateinit var mJobScheduler: JobScheduler
    internal lateinit var set: SettingApi
    internal lateinit var start: StartApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        set = SettingApi(this)
        start = StartApp()
        toolbar = findViewById(R.id.toolbar) as Toolbar
        fab = findViewById(R.id.add) as FloatingActionButton

        prepareActionBar(toolbar)
        initComponent()


        fab.setOnClickListener {
            val i = Intent(this@MainActivity, SelectFriendActivity::class.java)
            startActivity(i)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // for system bar in lollipop
            Tools.systemBarLolipop(this)
            //Create the scheduler
            mJobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val builder = JobInfo.Builder(1, ComponentName(packageName, NotificationService::class.java!!.getName()))
            builder.setPeriodic(900000)
            //If it needs to continue even after boot, persisted needs to be true
            //builder.setPersisted(true);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            mJobScheduler.schedule(builder.build())
        }
    }

    private fun initComponent() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val ctf = ChatsFragment()
        //icf.setRetainInstance(true);
        fragmentTransaction.add(R.id.main_containe, ctf, "Chat History")
        fragmentTransaction.commit()

    }

    private fun prepareActionBar(toolbar: Toolbar?) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(false)
        actionBar.setHomeButtonEnabled(false)
        profile.setOnClickListener {
            savePeopleInfo()
            Toast.makeText(this, "tes", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_logout -> {
                val logoutIntent = Intent(this, login::class.java)
                logoutIntent.putExtra("mode", "logout")
                startActivity(logoutIntent)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private var exitTime: Long = 0

    fun doExitApp() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            CustomToast(this).showInfo(getString(R.string.press_again_exit_app))
            exitTime = System.currentTimeMillis()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        doExitApp()
    }
    private fun savePeopleInfo(){
        val name = set.readSetting(Const.PREF_MY_NAME)
        val photo = set.readSetting(Const.PREF_MY_DP)
        val friend = FriendModel(
            "test",
            "test"
        )
        Log.d("tag", "test$photo")
        start.getPeopleRepo().insertFriend(friend)
    }
}
