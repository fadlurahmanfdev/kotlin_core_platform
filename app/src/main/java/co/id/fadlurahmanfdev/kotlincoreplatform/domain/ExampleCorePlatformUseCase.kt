package co.id.fadlurahmanfdev.kotlincoreplatform.domain

import android.content.Context
import co.id.fadlurahmanfdev.kotlin_core_platform.data.exception.CorePlatformException
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.AddressModel
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.CoordinateModel

interface ExampleCorePlatformUseCase {
    fun isLocationPermissionGranted(context: Context): Boolean

    fun isLocationEnabled(context: Context): Boolean

    fun getCurrentLocation(
        context: Context,
        onSuccess: (CoordinateModel) -> Unit,
        onError: (CorePlatformException) -> Unit
    )

    fun getAddress(
        context: Context,
        onSuccess: (AddressModel) -> Unit,
        onError: (CorePlatformException) -> Unit
    )
}