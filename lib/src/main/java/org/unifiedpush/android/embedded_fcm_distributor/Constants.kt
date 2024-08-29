package org.unifiedpush.android.embedded_fcm_distributor

/**
 * Constants as defined on the specs
 * https://unifiedpush.org/developers/spec/android/
 */

const val PREF_MASTER = "UP-embedded_fcm"
const val PREF_MASTER_TOKEN = "$PREF_MASTER:token"
const val PREF_MASTER_TOKENS = "$PREF_MASTER:tokens"

const val ACTION_NEW_ENDPOINT = "org.unifiedpush.android.connector.NEW_ENDPOINT"
const val ACTION_REGISTRATION_FAILED = "org.unifiedpush.android.connector.REGISTRATION_FAILED"
const val ACTION_REGISTRATION_REFUSED = "org.unifiedpush.android.connector.REGISTRATION_REFUSED"
const val ACTION_UNREGISTERED = "org.unifiedpush.android.connector.UNREGISTERED"
const val ACTION_MESSAGE = "org.unifiedpush.android.connector.MESSAGE"

const val ACTION_REGISTER = "org.unifiedpush.android.distributor.REGISTER"
const val ACTION_UNREGISTER = "org.unifiedpush.android.distributor.UNREGISTER"
const val ACTION_MESSAGE_ACK = "org.unifiedpush.android.distributor.MESSAGE_ACK"

const val EXTRA_APPLICATION = "application"
const val EXTRA_TOKEN = "token"
const val EXTRA_ENDPOINT = "endpoint"
const val EXTRA_MESSAGE = "message"
const val EXTRA_BYTES_MESSAGE = "bytesMessage"
const val EXTRA_MESSAGE_ID = "id"
const val EXTRA_FCM_TOKEN = "FCMToken"
const val EXTRA_GET_ENDPOINT = "getEndpoint"

const val INSTANCE_DEFAULT = "default"
