package com.extrainch.ngao.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Telephony
import android.telephony.SmsMessage
import android.widget.EditText

class ReceiveSMS : BroadcastReceiver() {
    var preferences: SharedPreferences? = null
    var SHARED_PREF_NAME = "ngao_pref"
    val OTP = "OTP"
    var editor: SharedPreferences.Editor? = null

    fun setEditText(editText: EditText?) {
        Companion.editText = editText
    }

    // OnReceive will keep trace when sms is been received in mobile
    override fun onReceive(context: Context, intent: Intent) {
        //message will be holding complete sms that is received
        val messages: Array<SmsMessage?> = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (sms in messages) {
            val sms_from = messages[0]!!.displayOriginatingAddress
            if (sms_from.equals(SMS_SENDER, ignoreCase = true)) {
                StringBuilder()
                val msg = sms!!.messageBody

                // here we are splitting the sms using " : " symbol
                if (msg.contains("<#>")) {
                    val first6char = msg.substring(0, 7)
                    val first4char = first6char.replace("<#>", "")

                    preferences = context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)

                    editor = preferences!!.edit()
                    editor!!.putString(OTP, first4char)
                    editor!!.apply()

                    editText!!.setText(first4char)
                }
            }
        }
    }

    companion object {
        private var editText: EditText? = null
        private const val SMS_SENDER = "NGAO CREDIT"
    }
}