package org.unifiedpush.android.embedded_fcm_distributor

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build

internal object Utils {

    // UnifiedPush intents

    /** Send new UnifiedPush endpoint */
    fun sendNewEndpoint(
        context: Context,
        connectionToken: String,
        fcmToken: String,
        useGateway: Boolean
    ) {
        val intent = Intent().apply {
            `package` = context.packageName
            action = ACTION_NEW_ENDPOINT
            val endpoint = if (useGateway) {
                getEndpoint(context, fcmToken)
            } else {
                ENDPOINT.format(fcmToken)
            }
            putExtra(EXTRA_ENDPOINT, endpoint)
            putExtra(EXTRA_TOKEN, connectionToken)
        }
        context.sendBroadcast(intent)
    }

    /** Send UnifiedPush unregistration */
    fun sendUnregistered(context: Context, token: String) {
        val intent = Intent().apply {
            `package` = context.packageName
            action = ACTION_UNREGISTERED
            putExtra(EXTRA_TOKEN, token)
        }
        context.sendBroadcast(intent)
    }

    /** Send UnifiedPush registration failure */
    fun sendRegistrationFailed(
        context: Context,
        connectionToken: String,
        reason: FailedReason,
    ) {
        val intent = Intent().apply {
            `package` = context.packageName
            action = ACTION_REGISTRATION_FAILED
            putExtra(EXTRA_TOKEN, connectionToken)
            putExtra(EXTRA_FAILED_REASON, reason.name)
        }
        context.sendBroadcast(intent)
    }

    /** Send UnifiedPush Message */
    fun sendMessage(context: Context,
                    token: String,
                    message: ByteArray,
                    messageId: String?) {
        val intent = Intent().apply {
            action = ACTION_MESSAGE
            setPackage(context.packageName)
            putExtra(EXTRA_BYTES_MESSAGE, message)
            putExtra(EXTRA_TOKEN, token)
            messageId?.let {
                putExtra(EXTRA_MESSAGE_ID, messageId)
            }
        }
        context.sendBroadcast(intent)
    }

    // FCM intents

    /** Register to Google services */
    fun registerFCM(context: Context, channelId: String, vapid: String, useGateway: Boolean) {
        val subtype = "wp:$channelId"
        // kid is the registration id, it is return when we receive a new
        // token. The extra contains "KID:TOKEN"
        val kid = if (useGateway) "1:$channelId" else "0:$channelId"
        val intent = Intent(ACTION_FCM_TOKEN_REQUEST).apply {
            `package` = GSF_PACKAGE
            putExtra(EXTRA_SCOPE, "GCM")
            putExtra(EXTRA_SENDER, vapid)
            putExtra(EXTRA_SUB, vapid)
            putExtra(EXTRA_SUB_X, vapid)
            putExtra(EXTRA_SUBTYPE, subtype)
            putExtra(EXTRA_SUBTYPE_X, subtype)
            // When we receive a new token, the extra contains
            // "KID:TOKEN"
            putExtra(EXTRA_KID, kid)
            putExtra(
                EXTRA_APPLICATION_PENDING_INTENT,
                PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent(),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
        context.sendBroadcast(intent)
    }

    // Shared Prefs

    fun saveEndpoint(context: Context, gateway: Gateway) {
        context.getSharedPreferences(PREF_MASTER, Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_ENDPOINT, gateway.getEndpoint("%s"))
            .apply()
    }

    private fun getEndpoint(context: Context, token: String): String? {
        return context.getSharedPreferences(PREF_MASTER, Context.MODE_PRIVATE)
            .getString(PREF_ENDPOINT, null)
            ?.format(token)
    }

    // Other

    fun getResolveInfo(
        context: Context,
        action: String
    ): List<ResolveInfo> {
        val intent =
            Intent(action).apply {
                `package` = context.packageName
            }
        return (
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.queryBroadcastReceivers(
                        intent,
                        PackageManager.ResolveInfoFlags.of(
                            PackageManager.GET_META_DATA.toLong() +
                                    PackageManager.GET_RESOLVED_FILTER.toLong(),
                        ),
                    )
                } else {
                    context.packageManager.queryBroadcastReceivers(
                        Intent(ACTION_REGISTER),
                        PackageManager.GET_RESOLVED_FILTER,
                    )
                }
                )
    }
}
