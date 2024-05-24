package co.id.fadlurahmanfdev.kotlin_core_platform.data.repository

import android.content.Context
import android.location.Location
import co.id.fadlurahmanfdev.kotlin_core_platform.data.exception.CorePlatformException
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.AddressModel
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.CoordinateModel

interface CorePlatformRepository {
    fun isLocationPermissionEnabled(context: Context): Boolean
    fun isLocationEnabled(context: Context): Boolean
    fun requestAndForgetLocation(
        context: Context,
        onSuccess: (CoordinateModel) -> Unit,
        onError: (CorePlatformException) -> Unit
    )

    fun getAddress(
        context: Context,
        latitude: Double,
        longitude: Double,
        onSuccess: (AddressModel) -> Unit,
        onError: (CorePlatformException) -> Unit,
    )
}