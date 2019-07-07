package  latihan.android.com.chatmu.adapters

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import latihan.android.com.chatmu.BigImageDialog
import  latihan.android.com.chatmu.R
import  latihan.android.com.chatmu.data.SettingApi
import  latihan.android.com.chatmu.model.ChatMessage



class ChatDetailListAdapter(private val mContext: Context, private val mMessages: MutableList<ChatMessage>) : BaseAdapter() {
    internal var set: SettingApi

    init {
        set = SettingApi(mContext)
    }

    override fun getCount(): Int {
        return mMessages.size
    }

    override fun getItem(position: Int): Any {
        return mMessages[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val msg = getItem(position) as ChatMessage
        val holder: ViewHolder
        if (convertView == null) {
            holder = ViewHolder()
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row_chat_details, parent, false)
            holder.time = convertView!!.findViewById<View>(R.id.text_time) as TextView
            holder.message = convertView.findViewById<View>(R.id.text_content) as TextView
            holder.lyt_thread = convertView.findViewById<View>(R.id.lyt_thread) as CardView
            holder.lyt_parent = convertView.findViewById<View>(R.id.lyt_parent) as LinearLayout
            holder.image_status = convertView.findViewById<View>(R.id.image_status) as ImageView
            holder.imageMessage = convertView.findViewById<View>(R.id.messageImageView) as ImageView
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        holder.imageMessage!!.setOnClickListener {
            BigImageDialog.newInstance(msg.imageUrl!!).show((mContext as Activity).fragmentManager, "")
        }
        if (msg.imageUrl != null){
            val imageUrl = msg.imageUrl
            if (imageUrl!!.startsWith("gs://")){
                val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                storageReference.downloadUrl.addOnCompleteListener{
                    task->
                    if (task.isSuccessful){
                        val downloadUrl = task.result!!.toString()
                        Glide.with(holder.imageMessage!!.context).load(downloadUrl).into(holder.imageMessage!!)
                    }else{
                        Log.w("tag", "GETTING DOWNLOAD"+"url was not succesfull.", task.exception)
                    }
                }
            }else{
                Glide.with(holder.imageMessage!!.context).load(msg.imageUrl).into(holder.imageMessage!!)
            }

        }

        holder.message!!.text = msg.text
        holder.time!!.text = msg.readableTime
        if (msg.receiver.id == set.readSetting("myid")) {
            holder.lyt_parent!!.setPadding(15, 10, 100, 10)
            holder.lyt_parent!!.gravity = Gravity.LEFT
            holder.lyt_thread!!.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            holder.image_status!!.setImageResource(android.R.color.transparent)
        } else {
            holder.lyt_parent!!.setPadding(100, 10, 15, 10)
            holder.lyt_parent!!.gravity = Gravity.RIGHT
            holder.lyt_thread!!.setCardBackgroundColor(mContext.resources.getColor(R.color.me_chat_bg))
            holder.image_status!!.setImageResource(R.drawable.baseline_done_24)
            holder.image_status!!.setColorFilter(ContextCompat.getColor(mContext, android.R.color.darker_gray), android.graphics.PorterDuff.Mode.MULTIPLY)

            if (msg.isRead!!) {
                holder.image_status!!.setImageResource(R.drawable.baseline_done_all_24)
                holder.image_status!!.setColorFilter(ContextCompat.getColor(mContext, android.R.color.holo_blue_dark), android.graphics.PorterDuff.Mode.MULTIPLY)
            }
        }

        return convertView
    }

    /**
     * remove data item from messageAdapter
     */
    fun remove(position: Int) {
        mMessages.removeAt(position)
    }

    /**
     * add data item to messageAdapter
     */
    fun add(msg: ChatMessage) {
        mMessages.add(msg)
    }

    private class ViewHolder {
        internal var time: TextView? = null
        internal var message: TextView? = null
        internal var lyt_parent: LinearLayout? = null
        internal var lyt_thread: CardView? = null
        internal var image_status: ImageView? = null
        internal var imageMessage: ImageView? = null
    }
}