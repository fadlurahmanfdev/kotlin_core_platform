package co.id.fadlurahmanfdev.kotlin_core_platform.data.repository

import android.app.Activity
import android.os.CancellationSignal
import co.id.fadlurahmanfdev.kotlin_core_platform.data.callback.BiometricCallBack
import co.id.fadlurahmanfdev.kotlin_core_platform.data.callback.CryptoBiometricCallBack
import co.id.fadlurahmanfdev.kotlin_core_platform.data.type.CanAuthenticateReasonType

interface CorePlatformBiometricRepository {
    fun canAuthenticate(activity: Activity): Boolean
    fun canAuthenticateWithReason(activity: Activity): CanAuthenticateReasonType

    fun promptEncrypt(
        activity: Activity,
        cancellationSignal: CancellationSignal,
        keystoreAlias: String,
        title: String,
        description: String,
        negativeText: String,
        callBack: CryptoBiometricCallBack? = null,
    )

    fun promptDecrypt(
        activity: Activity,
        cancellationSignal: CancellationSignal,
        keystoreAlias: String,
        encodedIvKey:String,
        title: String,
        description: String,
        negativeText: String,
        callBack: CryptoBiometricCallBack? = null,
    )

    fun prompt(
        activity: Activity,
        cancellationSignal: CancellationSignal,
        title: String,
        description: String,
        negativeText: String,
        callBack: BiometricCallBack?,
    )
}