package  latihan.android.com.chatmu.data

import android.arch.lifecycle.LiveData
import android.content.Context
import com.google.firebase.database.DataSnapshot
import  latihan.android.com.chatmu.model.ChatMessage
import  latihan.android.com.chatmu.model.Friend
import  latihan.android.com.chatmu.utilities.Const
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_ID
import latihan.android.com.chatmu.utilities.Const.Companion.NODE_IMAGE
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_IS_READ
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_NAME
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_PHOTO
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_RECEIVER_ID
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_RECEIVER_NAME
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_RECEIVER_PHOTO
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_SENDER_ID
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_SENDER_NAME
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_SENDER_PHOTO
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_TEXT
import  latihan.android.com.chatmu.utilities.Const.Companion.NODE_TIMESTAMP
import java.util.ArrayList



class ParseFirebaseData {

    private var set: SettingApi

    constructor(context: Context){
        set = SettingApi(context)
    }

    fun getAllUser(dataSnapshot: DataSnapshot): ArrayList<Friend> {
        val frnds = ArrayList<Friend>()
        var name: String? = null
        var id: String? = null
        var photo: String? = null
        for (data in dataSnapshot.children) {
            name = data.child(NODE_NAME).value!!.toString()
            id = data.child(NODE_ID).value!!.toString()
            photo = data.child(NODE_PHOTO).value!!.toString()

            if (!set.readSetting(Const.PREF_MY_ID).equals(id))
                frnds.add(Friend(id, name, photo))
        }
        return frnds
    }
    fun findFriend(id: String): LiveData<Friend>{
        return findFriend(id)
    }

    fun getMessagesForSingleUser(dataSnapshot: DataSnapshot): List<ChatMessage> {
        val chats = ArrayList<ChatMessage>()
        var text: String? = null
        var msgTime: String? = null
        var senderId: String? = null
        var senderName: String? = null
        var senderPhoto: String? = null
        var receiverId: String? = null
        var receiverName: String? = null
        var receiverPhoto: String? = null
        var read: Boolean? = java.lang.Boolean.TRUE
        var imageUrl: String? = null
        for (data in dataSnapshot.children) {
            text = data.child(NODE_TEXT).value!!.toString()
            msgTime = data.child(NODE_TIMESTAMP).value!!.toString()
            senderId = data.child(NODE_SENDER_ID).value!!.toString()
            senderName = data.child(NODE_SENDER_NAME).value!!.toString()
            senderPhoto = data.child(NODE_SENDER_PHOTO).value!!.toString()
            receiverId = data.child(NODE_RECEIVER_ID).value!!.toString()
            receiverName = data.child(NODE_RECEIVER_NAME).value!!.toString()
            receiverPhoto = data.child(NODE_RECEIVER_PHOTO).value!!.toString()
            imageUrl = data.child(NODE_IMAGE).value?.toString()
            //Node isRead is added later, may be null
            read =
                data.child(NODE_IS_READ).value == null || java.lang.Boolean.parseBoolean(data.child(NODE_IS_READ).value!!.toString())

            chats.add(
                ChatMessage(
                    text,
                    msgTime,
                    receiverId,
                    receiverName,
                    receiverPhoto,
                    senderId,
                    senderName,
                    senderPhoto,
                    read,
                    imageUrl
                )
            )
        }
        return chats
    }

    fun getAllLastMessages(dataSnapshot: DataSnapshot): ArrayList<ChatMessage> {
        // TODO: 11/09/18 Return only last messages of every conversation current user is involved in
        val lastChats = ArrayList<ChatMessage>()
        var tempMsgList: ArrayList<ChatMessage>
        var lastTimeStamp: Long
        var text: String? = null
        var msgTime: String? = null
        var senderId: String? = null
        var senderName: String? = null
        var senderPhoto: String? = null
        var receiverId: String? = null
        var receiverName: String? = null
        var receiverPhoto: String? = null
        var read: Boolean? = java.lang.Boolean.TRUE
        var imageUrl: String? = null
        for (wholeChatData in dataSnapshot.children) {

            tempMsgList = ArrayList<ChatMessage>()
            lastTimeStamp = 0

            for (data in wholeChatData.children) {
                msgTime = data.child(NODE_TIMESTAMP).value!!.toString()
                if (java.lang.Long.parseLong(msgTime) > lastTimeStamp)
                    lastTimeStamp = java.lang.Long.parseLong(msgTime)
                text = data.child(NODE_TEXT).value!!.toString()
                senderId = data.child(NODE_SENDER_ID).value!!.toString()
                senderName = data.child(NODE_SENDER_NAME).value!!.toString()
                senderPhoto = data.child(NODE_SENDER_PHOTO).value!!.toString()
                receiverId = data.child(NODE_RECEIVER_ID).value!!.toString()
                receiverName = data.child(NODE_RECEIVER_NAME).value!!.toString()
                receiverPhoto = data.child(NODE_RECEIVER_PHOTO).value!!.toString()
                imageUrl = data.child(NODE_IMAGE).value?.toString()
                //Node isRead is added later, may be null
                read =
                    data.child(NODE_IS_READ).value == null || java.lang.Boolean.parseBoolean(data.child(NODE_IS_READ).value!!.toString())

                tempMsgList.add(
                    ChatMessage(
                        text,
                        msgTime,
                        receiverId,
                        receiverName,
                        receiverPhoto,
                        senderId,
                        senderName,
                        senderPhoto,
                        read,
                        imageUrl
                    )
                )
            }

            for (oneTemp in tempMsgList) {
                if (set.readSetting(Const.PREF_MY_ID).equals(oneTemp.receiver.id) || set.readSetting("myid").equals(
                        oneTemp.senderId
                    )
                ) {
                    if (oneTemp.timestamp.equals(lastTimeStamp.toString())) {
                        lastChats.add(oneTemp)
                    }
                }
            }
        }
        return lastChats
    }

    fun getAllUnreadReceivedMessages(dataSnapshot: DataSnapshot): ArrayList<ChatMessage> {
        val lastChats = ArrayList<ChatMessage>()
        var tempMsgList: ArrayList<ChatMessage>
        var lastTimeStamp: Long
        var text: String? = null
        var msgTime: String? = null
        var senderId: String? = null
        var senderName: String? = null
        var senderPhoto: String? = null
        var receiverId: String? = null
        var receiverName: String? = null
        var receiverPhoto: String? = null
        var read: Boolean? = java.lang.Boolean.TRUE
        var imageUrl: String? = null
        for (wholeChatData in dataSnapshot.children) {

            tempMsgList = ArrayList<ChatMessage>()
            lastTimeStamp = 0

            for (data in wholeChatData.children) {
                msgTime = data.child(NODE_TIMESTAMP).value!!.toString()
                if (java.lang.Long.parseLong(msgTime) > lastTimeStamp)
                    lastTimeStamp = java.lang.Long.parseLong(msgTime)
                text = data.child(NODE_TEXT).value!!.toString()
                senderId = data.child(NODE_SENDER_ID).value!!.toString()
                senderName = data.child(NODE_SENDER_NAME).value!!.toString()
                senderPhoto = data.child(NODE_SENDER_PHOTO).value!!.toString()
                receiverId = data.child(NODE_RECEIVER_ID).value!!.toString()
                receiverName = data.child(NODE_RECEIVER_NAME).value!!.toString()
                receiverPhoto = data.child(NODE_RECEIVER_PHOTO).value!!.toString()
                imageUrl = data.child(NODE_IMAGE).value?.toString()
                //Node isRead is added later, may be null
                read =
                    data.child(NODE_IS_READ).value == null || java.lang.Boolean.parseBoolean(data.child(NODE_IS_READ).value!!.toString())

                tempMsgList.add(
                    ChatMessage(
                        text,
                        msgTime,
                        receiverId,
                        receiverName,
                        receiverPhoto,
                        senderId,
                        senderName,
                        senderPhoto,
                        read,
                        imageUrl
                    )
                )
            }

            for (oneTemp in tempMsgList) {
                if (set.readSetting(Const.PREF_MY_ID).equals(oneTemp.receiver.id)) {
                    if (oneTemp.timestamp.equals(lastTimeStamp.toString()) && !oneTemp.isRead!!) {
                        lastChats.add(oneTemp)
                    }
                }
            }
        }
        return lastChats
    }

    private fun encodeText(msg: String): String {
        return msg.replace(",", "#comma#").replace("{", "#braceopen#").replace("}", "#braceclose#")
            .replace("=", "#equals#")
    }

    private fun decodeText(msg: String): String {
        return msg.replace("#comma#", ",").replace("#braceopen#", "{").replace("#braceclose#", "}")
            .replace("#equals#", "=")
    }
}