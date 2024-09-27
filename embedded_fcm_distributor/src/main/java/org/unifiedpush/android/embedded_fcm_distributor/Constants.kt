package org.unifiedpush.android.embedded_fcm_distributor

/**
 * Constants as defined on the specs
 * https://unifiedpush.org/developers/spec/android/
 */

internal const val PREF_MASTER = "UP-embedded_fcm"
internal const val PREF_MASTER_TOKEN = "$PREF_MASTER:token"
internal const val PREF_MASTER_TOKENS = "$PREF_MASTER:tokens"

internal const val ACTION_NEW_ENDPOINT = "org.unifiedpush.android.connector.NEW_ENDPOINT"
internal const val ACTION_REGISTRATION_FAILED = "org.unifiedpush.android.connector.REGISTRATION_FAILED"
internal const val ACTION_REGISTRATION_REFUSED = "org.unifiedpush.android.connector.REGISTRATION_REFUSED"
internal const val ACTION_UNREGISTERED = "org.unifiedpush.android.connector.UNREGISTERED"
internal const val ACTION_MESSAGE = "org.unifiedpush.android.connector.MESSAGE"

internal const val ACTION_REGISTER = "org.unifiedpush.android.distributor.REGISTER"
internal const val ACTION_UNREGISTER = "org.unifiedpush.android.distributor.UNREGISTER"
internal const val ACTION_MESSAGE_ACK = "org.unifiedpush.android.distributor.MESSAGE_ACK"

internal const val EXTRA_APPLICATION = "application"
internal const val EXTRA_TOKEN = "token"
internal const val EXTRA_ENDPOINT = "endpoint"
internal const val EXTRA_MESSAGE = "message"
internal const val EXTRA_BYTES_MESSAGE = "bytesMessage"
internal const val EXTRA_MESSAGE_ID = "id"
internal const val EXTRA_FCM_TOKEN = "FCMToken"
internal const val EXTRA_GET_ENDPOINT = "getEndpoint"
