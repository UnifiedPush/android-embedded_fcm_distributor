# Module embedded_fcm_distributor

Embed a FCM distributor as a fallback if user don't have another distributor. It doesn't contain any Google proprietary blobs.

This library requires Android 4.1 or higher.

## Import the library

Add the dependency to the _module_ build.gradle. Replace {VERSION} with the [latest version](https://central.sonatype.com/artifact/org.unifiedpush.android/embedded-fcm-distributor).

```groovy
dependencies {
    // ...
    implementation 'org.unifiedpush.android:embedded-fcm-distributor:{VERSION}'
```

## Usage

Google FCM servers can handle webpush requests out of the box, but they require
a [VAPID](https://www.rfc-editor.org/rfc/rfc8292) authorization.

**If your application supports VAPID, you have nothing special to do. You don't need to extend and expose [EmbeddedDistributorReceiver][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver]:**
The library already expose it.

Else, you need to use a gateway that will add VAPID authorizations to the
responses. For this, you need to extend and expose [EmbeddedDistributorReceiver][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver].

## Expose a receiver

**If your application doesn't support VAPID**, you need to expose a Receiver that extend [EmbeddedDistributorReceiver][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver]
and you must override [gateway][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver.gateway].

```kotlin
override val gateway = object : Gateway {
    override val vapid = "BJVlg_p7GZr_ZluA2ace8aWj8dXVG6hB5L19VhMX3lbVd3c8IqrziiHVY3ERNVhB9Jje5HNZQI4nUOtF_XkUIyI"

    override fun getEndpoint(token: String): String {
        return "https://fcm.example.unifiedpush.org/FCM?v3&token=$token"
    }
}
```

## Edit your manifest

The receiver has to be exposed in the `AndroidManifest.xml` in order to receive the UnifiedPush messages.

```xml
<receiver android:enabled="true"  android:name=".EmbeddedDistributor" android:exported="false">
    <intent-filter>
        <action android:name="org.unifiedpush.android.distributor.feature.BYTES_MESSAGE"/>
        <action android:name="org.unifiedpush.android.distributor.REGISTER"/>
        <action android:name="org.unifiedpush.android.distributor.UNREGISTER"/>
    </intent-filter>
</receiver>
```
