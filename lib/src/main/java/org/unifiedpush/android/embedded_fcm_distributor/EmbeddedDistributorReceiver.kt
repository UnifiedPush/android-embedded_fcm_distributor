package org.unifiedpush.android.embedded_fcm_distributor

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import org.unifiedpush.android.embedded_fcm_distributor.Utils.removeToken
import org.unifiedpush.android.embedded_fcm_distributor.Utils.saveToken
import org.unifiedpush.android.embedded_fcm_distributor.Utils.sendNewEndpoint
import org.unifiedpush.android.embedded_fcm_distributor.Utils.sendRegistrationFailed

private const val TAG = "UP-Embedded_distributor"

open class EmbeddedDistributorReceiver : BroadcastReceiver() {
    open fun getEndpoint(context: Context, token: String, instance: String): String {
        return ""
    }

    override fun onReceive(context: Context, intent: Intent) {
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: return
        Log.d(TAG, "New intent for $token")
        when (intent.action) {
            ACTION_REGISTER -> {
                Log.d(TAG, "Registering to the embedded distributor")
                saveGetEndpoint(context)
                saveToken(context, token)
                when (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)) {
                    ConnectionResult.SUCCESS -> Log.d(TAG, "PlayServices available")
                    ConnectionResult.SERVICE_MISSING,
                    ConnectionResult.SERVICE_DISABLED,
                    ConnectionResult.SERVICE_INVALID -> {
                        Log.w(TAG, "PlayServices Missing, disabled or invalid. Sending registration failed")
                        sendRegistrationFailed(context, token, "PlayServices not available")
                        return
                    }
                    ConnectionResult.SERVICE_UPDATING,
                    ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                        Log.w(TAG, "PlayServices updating or require an update.")
                        sendRegistrationFailed(context, token, "PlayServices temporarily not available")
                        return
                    }
                }
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val fcmToken = task.result
                        Log.d(TAG, "Token successfully received: $fcmToken")
                        sendNewEndpoint(context, fcmToken, token)
                    } else {
                        Log.w(TAG, "FCMToken registration failed: " +
                            "${task.exception?.localizedMessage}")
                        sendRegistrationFailed(context, token, "FCM (PlayServices) token not received")
                    }
                }
            }
            ACTION_UNREGISTER -> {
                Log.d(TAG, "Fake Distributor unregister")
                removeToken(context, token)
                val broadcastIntent = Intent()
                broadcastIntent.`package` = context.packageName
                broadcastIntent.action = ACTION_UNREGISTERED
                broadcastIntent.putExtra(EXTRA_TOKEN, token)
                context.sendBroadcast(broadcastIntent)
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    private fun saveGetEndpoint(context: Context) {
        val prefs = context.getSharedPreferences(PREF_MASTER, Context.MODE_PRIVATE)
        val ff = 0xff.toChar().toString()
        prefs.edit().putString(EXTRA_GET_ENDPOINT,
            getEndpoint(context, "$ff$ff.TOKEN.$ff$ff", "$ff$ff.INSTANCE.$ff$ff")
        ).commit()
    }
}
