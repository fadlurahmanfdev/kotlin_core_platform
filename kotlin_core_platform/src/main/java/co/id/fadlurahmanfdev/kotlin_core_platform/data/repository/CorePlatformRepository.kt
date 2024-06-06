package co.id.fadlurahmanfdev.kotlin_core_platform.data.repository

import android.content.Context

interface CorePlatformRepository {

    /**
     * return unique identifier of deviceID
     */
    fun getDeviceId(context: Context): String
}