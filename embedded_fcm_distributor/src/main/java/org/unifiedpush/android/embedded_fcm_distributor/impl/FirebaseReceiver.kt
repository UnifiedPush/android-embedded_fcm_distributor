package org.unifiedpush.android.embedded_fcm_distributor.impl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.unifiedpush.android.embedded_fcm_distributor.*
import org.unifiedpush.android.embedded_fcm_distributor.Utils.sendMessage
import org.unifiedpush.android.embedded_fcm_distributor.Utils.sendNewEndpoint

/**
 * This receivers interacts with Google Services and receives FCM message. It is exposed by the library.
 */
class FirebaseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_FCM_REGISTRATION -> {
                if (intent.hasExtra(EXTRA_REGISTRATION_ID)) {
                    val registration = intent.getStringExtra(EXTRA_REGISTRATION_ID)?.split(":", limit = 3) ?: return
                    if (registration.size != 3) {
                        Log.d(TAG, "Cannot retrieve registration info, aborting.")
                        return
                    }
                    val useGateway = registration[0] == "1"
                    val connectionToken = registration[1]
                    val fcmToken = registration[2]
                    sendNewEndpoint(context, connectionToken, fcmToken, useGateway)
                    Log.i(
                        TAG,
                        "Successfully registered for FCM"
                    )
                } else {
                    Log.e(
                        TAG,
                        "FCM registration intent did not contain registration_id: $intent"
                    )
                    val extras = intent.extras
                    for (key in extras!!.keySet()) {
                        Log.i(
                            TAG,
                            key + " -> " + extras[key]
                        )
                    }
                }
            }
             ACTION_FCM_RECEIVE -> {
                 val token = intent.getStringExtra(EXTRA_SUBTYPE)?.let {
                     if (it.startsWith("wp:")) {
                         it.substring(3)
                     } else {
                         Log.d(TAG, "Received message for unknown subtype. Starting with ${it.substring(0,3)}")
                         null
                     }
                 } ?: return
                 val message = intent.getByteArrayExtra(EXTRA_RAW_DATA) ?: return
                 val messageId = intent.getStringExtra(EXTRA_GOOGLE_MSG_ID)
                 sendMessage(context, token, message, messageId)
            }
        }
    }

    private companion object {
        private const val TAG = "FirebaseReceiver"
    }
}
