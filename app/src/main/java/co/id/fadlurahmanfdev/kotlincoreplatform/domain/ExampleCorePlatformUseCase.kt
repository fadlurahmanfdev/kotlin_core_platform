package co.id.fadlurahmanfdev.kotlincoreplatform.domain

import android.content.Context
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.AddressModel
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.CoordinateModel
import io.reactivex.rxjava3.core.Observable

interface ExampleCorePlatformUseCase {
    fun isLocationPermissionGranted(context: Context): Boolean

    fun isLocationEnabled(context: Context): Boolean

    fun getCurrentLocation():Observable<CoordinateModel>

    fun getAddress():Observable<AddressModel>
}