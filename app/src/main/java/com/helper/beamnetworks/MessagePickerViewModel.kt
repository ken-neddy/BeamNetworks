package com.helper.beamnetworks

import android.content.ContentResolver
import android.provider.Telephony
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SmsThread(val sender: String, val lastMessage: String, val threadId: Long)
data class Message(val id: Long, val body: String)

class MessagePickerViewModel : ViewModel() {

    private val _threads = MutableStateFlow<List<SmsThread>>(emptyList())
    val threads = _threads.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    fun fetchThreads(contentResolver: ContentResolver) {
        val threadsMap = mutableMapOf<Long, SmsThread>()
        val projection = arrayOf(Telephony.Sms.THREAD_ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE)
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            val threadIdColumn = it.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            val addressColumn = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyColumn = it.getColumnIndexOrThrow(Telephony.Sms.BODY)

            while (it.moveToNext()) {
                val threadId = it.getLong(threadIdColumn)
                if (!threadsMap.containsKey(threadId)) {
                    val address = it.getString(addressColumn) ?: "Unknown"
                    val body = it.getString(bodyColumn) ?: ""
                    threadsMap[threadId] = SmsThread(address, body, threadId)
                }
            }
        }
        _threads.value = threadsMap.values.toList()
    }

    fun fetchMessagesForThread(contentResolver: ContentResolver, threadId: Long) {
        val messagesList = mutableListOf<Message>()
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms._ID, Telephony.Sms.BODY),
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(Telephony.Sms._ID)
            val bodyColumn = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val body = it.getString(bodyColumn)
                messagesList.add(Message(id, body))
            }
        }
        _messages.value = messagesList
    }
}
