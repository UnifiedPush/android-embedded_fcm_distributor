package org.unifiedpush.android.embedded_fcm_distributor.fcm

import android.content.Intent
import android.util.Base64
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import org.unifiedpush.android.embedded_fcm_distributor.*
import org.unifiedpush.android.embedded_fcm_distributor.Utils.getTokens
import org.unifiedpush.android.embedded_fcm_distributor.Utils.sendNewEndpoint
import java.util.Timer
import kotlin.concurrent.schedule

/**
 * This service interacts with Google Services and receives FCM message. It is exposed by the library.
 */
class FirebaseForwardingService : FirebaseMessagingService() {

    private companion object {
        private const val TAG = "FirebaseForwarding"
        private val pendingMessages = mutableMapOf<String, ByteArray>()
    }

    override fun onNewToken(fcmToken: String) {
        Log.d(TAG, "New FCM token: $fcmToken")
        getTokens(baseContext).forEach {
            sendNewEndpoint(baseContext, fcmToken, it)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "New Firebase message ${remoteMessage.messageId}")
        // Empty token can be used by app not using an UnifiedPush gateway.
        val token = remoteMessage.data["i"] ?: getTokens(applicationContext).lastOrNull() ?: return
        getMessage(remoteMessage.data)?.let { message ->
            forwardMessage(token, message, remoteMessage.messageId ?: "")
        }
    }

    private fun forwardMessage(token: String, message: ByteArray, messageId: String) {
        val intent = Intent()
        intent.action = ACTION_MESSAGE
        intent.setPackage(baseContext.packageName)
        intent.putExtra(EXTRA_MESSAGE, String(message))
        intent.putExtra(EXTRA_BYTES_MESSAGE, message)
        intent.putExtra(EXTRA_MESSAGE_ID, messageId)
        intent.putExtra(EXTRA_TOKEN, token)
        baseContext.sendBroadcast(intent)
    }

    private fun getMessage(data: MutableMap<String, String>): ByteArray? {
        var message: ByteArray? = null
        data["b"]?.let { b64 ->
            if (!Regex("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$")
                    .matches(b64)) {
                // The map can be used to allow applications keeping using their old gateway for FCM
                return JSONObject(data as Map<*, *>).toString().toByteArray()
            }
            data["m"]?.let { mId ->
                data["s"]?.let { splitId ->
                    if (pendingMessages.containsKey(mId)) {
                        Log.d(TAG, "Found pending message")
                        message = when (splitId) {
                            "1" -> {
                                Base64.decode(b64, Base64.DEFAULT) +
                                        (pendingMessages[mId] ?: ByteArray(0))
                            }
                            "2" -> {
                                (pendingMessages[mId] ?: ByteArray(0)) +
                                        Base64.decode(b64, Base64.DEFAULT)
                            }
                            else -> ByteArray(0)
                        }
                        pendingMessages.remove(mId)
                    } else {
                        pendingMessages[mId] = Base64.decode(b64, Base64.DEFAULT)
                        Timer().schedule(3000) {
                            pendingMessages.remove(mId)
                        }
                    }
                }
            } ?: run {
                return Base64.decode(b64, Base64.DEFAULT)
            }
        } ?: run {
            // The map can be used to allow applications keeping using their old gateway for FCM
            return JSONObject(data as Map<*, *>).toString().toByteArray()
        }
        return message
    }
}
