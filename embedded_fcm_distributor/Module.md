# Module embedded_fcm_distributor

Embed a FCM distributor as a fallback if user don't have another distributor. Uses Google proprietary blobs.

This library requires Android 5.0 or higher.

## Import the library

Add the dependency to the _module_ build.gradle. Replace {VERSION} with the [latest version](https://central.sonatype.com/artifact/org.unifiedpush.android/embedded-fcm-distributor).

```groovy
dependencies {
    // ...
    implementation 'org.unifiedpush.android:embedded-fcm-distributor:{VERSION}' {
        exclude group: 'com.google.firebase', module: 'firebase-core'
        exclude group: 'com.google.firebase', module: 'firebase-analytics'
        exclude group: 'com.google.firebase', module: 'firebase-measurement-connector'
    }
}
```

## Setup Google Services

Add google-services to the build dependencies, in the _root_ build.gradle. Replace {VERSION} with the [latest version](https://mvnrepository.com/artifact/com.google.gms/google-services).

```groovy
classpath 'com.google.gms:google-services:{VERSION}'
```

Apply google-services plugin for your fcm flavor in your _module_ build.gradle. (You may need to edit the pattern)

```groovy
def getCurrentFlavor() {
    Gradle gradle = getGradle()
    String  tskReqStr = gradle.getStartParameter().getTaskRequests().toString()
    String flavor

    Pattern pattern

    if( tskReqStr.contains( "assemble" ) )
        pattern = Pattern.compile("assemble(\\w+)")
    else
        pattern = Pattern.compile("generate(\\w+)")

    Matcher matcher = pattern.matcher( tskReqStr )

    if( matcher.find() ) {
        flavor = matcher.group(1).toLowerCase()
    }
    else
    {
        println "NO MATCH FOUND"
        return ""
    }

    pattern = Pattern.compile("^fcm.*");
    matcher = pattern.matcher(flavor);

    if( matcher.matches() ) {
        return "fcm"
    } else {
        return "main"
    }
}

println("Flavor: ${getCurrentFlavor()}")
if ( getCurrentFlavor() == "fcm" ){
    apply plugin: 'com.google.gms.google-services'
}
```

Download `google-services.json` from the [firebase console](https://console.firebase.google.com/project/_/settings/serviceaccounts/adminsdk), and add it to the _module_ directory.

## Expose a receiver

You need to expose a Receiver that extend [EmbeddedDistributorReceiver][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver]
and you must override [getEndpoint][org.unifiedpush.android.embedded_fcm_distributor.EmbeddedDistributorReceiver.getEndpoint] to return the address of your FCM rewrite-proxy.

```kotlin
class EmbeddedDistributor: EmbeddedDistributorReceiver() {
    override fun getEndpoint(context: Context, fcmToken: String, instance: String): String {
        // This returns the endpoint of your FCM Rewrite-Proxy
        return "https://<your.domain.tld>/FCM?v2&instance=$instance&token=$token"
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
