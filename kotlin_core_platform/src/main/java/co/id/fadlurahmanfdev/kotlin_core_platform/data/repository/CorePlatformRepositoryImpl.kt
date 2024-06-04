package co.id.fadlurahmanfdev.kotlin_core_platform.data.repository

import android.content.Context
import android.provider.Settings.Secure

class CorePlatformRepositoryImpl : CorePlatformRepository {
    override fun getDeviceId(context: Context): String {
        return Secure.getString(context.contentResolver, Secure.ANDROID_ID)
    }
}