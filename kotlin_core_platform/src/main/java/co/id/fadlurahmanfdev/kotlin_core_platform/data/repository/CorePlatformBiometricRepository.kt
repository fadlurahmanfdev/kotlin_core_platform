package co.id.fadlurahmanfdev.kotlin_core_platform.data.repository

import android.app.Activity
import co.id.fadlurahmanfdev.kotlin_core_platform.data.type.CanAuthenticateReasonType

interface CorePlatformBiometricRepository {
    fun canAuthenticate(activity: Activity): Boolean
    fun canAuthenticateWithReason(activity: Activity): CanAuthenticateReasonType
}