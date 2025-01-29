package org.unifiedpush.android.embedded_fcm_distributor

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import org.unifiedpush.android.embedded_fcm_distributor.Utils.registerFCM
import org.unifiedpush.android.embedded_fcm_distributor.Utils.sendRegistrationFailed
import org.unifiedpush.android.embedded_fcm_distributor.Utils.sendUnregistered

private const val TAG = "UP-FCMD"

/**
 * UnifiedPush receiver for the Embedded distributor
 *
 * <!-- Note: This must be mirrored in Module.md -->
 *
 * Google FCM servers can handle webpush requests out of the box, but they require
 * a [VAPID](https://www.rfc-editor.org/rfc/rfc8292) authorization.
 *
 * **If your application supports VAPID, you have nothing special to do. You don't need to extend and expose [EmbeddedDistributorReceiver][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver]:**
 * The library already expose it.
 *
 * Else, you need to use a gateway that will add VAPID authorizations to the
 * responses. For this, you need to extend and expose [EmbeddedDistributorReceiver][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver].
 *
 * ## Expose a receiver
 *
 * **If your application doesn't support VAPID**, you need to expose a Receiver that extend [EmbeddedDistributorReceiver][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver]
 * and you must override [gateway][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver.gateway].
 *
 * ```kotlin
 * override val gateway = object : Gateway {
 *     override val vapid = "BJVlg_p7GZr_ZluA2ace8aWj8dXVG6hB5L19VhMX3lbVd3c8IqrziiHVY3ERNVhB9Jje5HNZQI4nUOtF_XkUIyI"
 *
 *     override fun getEndpoint(token: String): String {
 *         return "https://fcm.example.unifiedpush.org/FCM?v3&token=$token"
 *     }
 * }
 * ```
 *
 * ## Edit your manifest
 *
 * The receiver has to be exposed in the `AndroidManifest.xml` in order to receive the UnifiedPush messages.
 *
 * ```xml
 * <receiver android:enabled="true"  android:name=".EmbeddedDistributor" android:exported="false">
 *     <intent-filter>
 *         <action android:name="org.unifiedpush.android.distributor.feature.BYTES_MESSAGE"/>
 *         <action android:name="org.unifiedpush.android.distributor.REGISTER"/>
 *         <action android:name="org.unifiedpush.android.distributor.UNREGISTER"/>
 *     </intent-filter>
 * </receiver>
 * ```
 */
open class EmbeddedDistributorReceiver : BroadcastReceiver() {
    open val gateway: Gateway? = null

    override fun onReceive(context: Context, intent: Intent) {
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: return
        Log.d(TAG, "New intent for ${token.substring(0, 3)}xxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxxx")
        when (intent.action) {
            ACTION_REGISTER -> {
                if (!checkAppOrigin(context, intent)) {
                    Log.d(TAG, "Received an intent from another package. Aborting.")
                    return
                }
                Log.d(TAG, "Registering to the embedded distributor")
                var useGateway = false
                if (!isPlayServicesAvailable(context)) {
                    sendRegistrationFailed(context, token, FailedReason.ACTION_REQUIRED)
                    return
                }
                val vapid = intent.getStringExtra(EXTRA_VAPID)
                    ?: gateway?.let {
                        Utils.saveEndpoint(context, it)
                        useGateway = true
                        it.vapid
                    } ?: run {
                    Log.d(TAG, "Received registration without a VAPID key. And gateway" +
                            "is not defined: sending registration failed with VAPID_REQUIRED reason")
                    sendRegistrationFailed(context, token, FailedReason.VAPID_REQUIRED)
                    return
                }
                registerFCM(context, token, vapid, useGateway)
            }
            ACTION_UNREGISTER -> {
                Log.d(TAG, "Unregistered")
                sendUnregistered(context, token)
            }
        }
    }

    /**
     * Check if the request really come from this app.
     *
     * This should always be true, but we do the check in case the receiver
     * has been exported.
     */
    private fun checkAppOrigin(context: Context, intent: Intent): Boolean {
        return (if (Build.VERSION.SDK_INT < 17) {
            // We do not check origin for SDK 16
            context.packageName
        } else if (Build.VERSION.SDK_INT >= 34) {
            sentFromPackage
        } else {
                intent.getParcelableExtra<PendingIntent>(EXTRA_PI)?.creatorPackage
        }) == context.packageName
    }

    private fun isPlayServicesAvailable(context: Context): Boolean {
        val pm = context.packageManager
        try {
            pm.getPackageInfo(GSF_PACKAGE, PackageManager.GET_ACTIVITIES)
            return true

        } catch (e: PackageManager.NameNotFoundException) {
            Log.v(TAG, e.message!!)
        }
        return false
    }
}
