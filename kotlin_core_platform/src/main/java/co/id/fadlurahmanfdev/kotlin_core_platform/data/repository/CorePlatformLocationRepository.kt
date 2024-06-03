package co.id.fadlurahmanfdev.kotlin_core_platform.data.repository

import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.AddressModel
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.CoordinateModel
import io.reactivex.rxjava3.core.Observable

interface CorePlatformLocationRepository {

    /**
     * Checks if the [android.Manifest.permission.ACCESS_FINE_LOCATION] permission is granted.
     * For more precise location data, consider using isFineLocationEnabled.
     * @see isCoarseLocationPermissionGranted
     */
    fun isFineLocationPermissionGranted(): Boolean

    /**
     * Checks if the [android.Manifest.permission.ACCESS_COARSE_LOCATION] permission is granted.
     * For more precise location data, consider using isFineLocationEnabled.
     * @see isFineLocationPermissionGranted
     */
    fun isCoarseLocationPermissionGranted(): Boolean

    fun isLocationServiceEnabled(): Boolean

    /**
     * Determine current coordinate using network provider.
     * @return [CoordinateModel] which contain [CoordinateModel.latitude] & [CoordinateModel.longitude]
     */
    fun getCurrentCoordinate(): Observable<CoordinateModel>

    /**
     * Determine detail address from coordinate
     * @return [AddressModel]
     * @see getCurrentCoordinate
     */
    fun getCurrentAddress(): Observable<AddressModel>

    /**
     * Determine detail address from coordinate
     * @return [AddressModel]
     * @see getCurrentCoordinate
     */
    fun getAddressByCoordinate(
        latitude: Double,
        longitude: Double,
    ): Observable<AddressModel>
}