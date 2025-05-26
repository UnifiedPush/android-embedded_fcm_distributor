# Module embedded_fcm_distributor

Embed a FCM distributor as a fallback if user don't have another distributor. It doesn't contain any Google proprietary blobs.

This library requires Android 4.1 or higher.

## Usage

Google FCM servers can handle webpush requests out of the box, but they require
a [VAPID](https://www.rfc-editor.org/rfc/rfc8292) authorization.

**If your application supports VAPID, you have nothing special to do. You don't need to extend and expose [EmbeddedDistributorReceiver][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver]:**
The library already expose it.

Else, you need to use a gateway that will add VAPID authorizations to the
responses. For this, you need to extend and expose [EmbeddedDistributorReceiver][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver].

### Your server sends push WITH VAPID authorizations

Import the library and you're done!

#### Import the library

Add the dependency to the _module_ build.gradle. Replace {VERSION} with the [latest version](https://central.sonatype.com/artifact/org.unifiedpush.android/embedded-fcm-distributor).

```groovy
dependencies {
    // ...
    implementation 'org.unifiedpush.android:embedded-fcm-distributor:{VERSION}'
```

### Your server sends push WITHOUT VAPID authorizations

First, import the library.

Then you will need a gateway that will add VAPID authorizations to the push requests. You will need to expose a Receiver with the information (URL and public key) of your gateway.

The library provides a [DefaultGateway][org.unifiedpush.android.embedded_fcm_distributor.DefaultGateway] you can use for your tests, or if you have a small userbase. Please host your [own gateway](https://codeberg.org/UnifiedPush/common-proxies) if possible.

#### 1. Import the library

Add the dependency to the _module_ build.gradle. Replace {VERSION} with the [latest version](https://central.sonatype.com/artifact/org.unifiedpush.android/embedded-fcm-distributor).

```groovy
dependencies {
    // ...
    implementation 'org.unifiedpush.android:embedded-fcm-distributor:{VERSION}'
```

#### 2. Expose a receiver

You need to expose a Receiver that extend [EmbeddedDistributorReceiver][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver]
and you must override [gateway][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver.gateway].

[vapid][org.unifiedpush.android.embedded_fcm_distributor.Gateway.vapid] contains the public key of your gateway VAPID key and [getEndpoint][org.unifiedpush.android.embedded_fcm_distributor.Gateway.getEndpoint] returns your gateway URL.

```kotlin
override val gateway = object : Gateway {
    override val vapid = "BJVlg_p7GZr_ZluA2ace8aWj8dXVG6hB5L19VhMX3lbVd3c8IqrziiHVY3ERNVhB9Jje5HNZQI4nUOtF_XkUIyI"

    override fun getEndpoint(token: String): String {
        return "https://fcm.example.unifiedpush.org/FCM?v3&token=$token"
    }
}
```

#### 3. Edit your manifest

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

## Troubleshooting

### How do I know if my server supports VAPID ?

Your application server is supposed to send push notification following web push specifications. Web push is defined by 3 RFC: [RFC8030](https://www.rfc-editor.org/rfc/rfc8030) defines the content of the http request used to push a message, [RFC8291](https://www.rfc-editor.org/rfc/rfc8291) defines the (required) encryption of the push messages, and [RFC8292](https://www.rfc-editor.org/rfc/rfc8292) defines the authorization used to control the sender of push messages, this authorization is known as VAPID and is optional with most distributors, but **required by FCM**.

Most applications that properly support Web Push also support VAPID. There is usually an API to get the public key of a server. For example, [mastodon sends the public key with the server information](https://docs.joinmastodon.org/methods/instance/#v2) (Note: `=` at the end must be removed, because mastodon sends the key with padding).

There are some applications that follow a non-adopted draft of the protocol, and send invalid VAPID authorizations. In that case, the `Authorization` headers doesn't start with `vapid [...]`.

Some applications don't send push messages following the web push specifications, like Matrix. Therefore, they need to use a [gateway](https://unifiedpush.org/users/gateway/). In that case, you won't check if the application supports VAPID, but if the gateway supports it. For example, if the matrix request is converted to web push with or without VAPID.
