package co.id.fadlurahmanfdev.kotlin_core_platform.data.repository

import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.AddressModel
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.CoordinateModel
import io.reactivex.rxjava3.core.Observable

interface CorePlatformRepository {

    /**
     * Checks if the [android.Manifest.permission.ACCESS_FINE_LOCATION] permission is granted.
     * For more precise location data, consider using isFineLocationEnabled.
     * @see isCoarseLocationEnabled
     */
    fun isFineLocationEnabled(): Boolean

    /**
     * Checks if the [android.Manifest.permission.ACCESS_COARSE_LOCATION] permission is granted.
     * For more precise location data, consider using isFineLocationEnabled.
     * @see isFineLocationEnabled
     */
    fun isCoarseLocationEnabled(): Boolean

    fun isLocationEnabled(): Boolean

    /**
     * Determine current coordinate using network provider.
     * @return [CoordinateModel] which contain [CoordinateModel.latitude] & [CoordinateModel.longitude]
     */
    fun requestCurrentCoordinate(): Observable<CoordinateModel>

    /**
     * Determine detail address from coordinate
     * @return [AddressModel]
     * @see requestCurrentCoordinate
     */
    fun getAddress(
        latitude: Double,
        longitude: Double,
    ): Observable<AddressModel>
}