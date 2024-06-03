package co.id.fadlurahmanfdev.kotlin_core_platform.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import co.id.fadlurahmanfdev.kotlin_core_platform.data.exception.CorePlatformException
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.AddressModel
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.CoordinateModel
import io.reactivex.rxjava3.core.Observable
import java.util.Locale

class CorePlatformLocationRepositoryImpl(private val context: Context) :
    CorePlatformLocationRepository {
    private var locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private fun getCoordinateModelFromLocation(location: Location): CoordinateModel {
        return CoordinateModel(
            latitude = location.latitude,
            longitude = location.longitude,
        )
    }

    private fun getAddress(latitude: Double, longitude: Double, address: Address): AddressModel {
        return AddressModel(
            country = address.countryName,
            adminArea = address.adminArea,
            subAdminArea = address.subAdminArea,
            locality = address.locality,
            subLocality = address.subLocality,
            postalCode = address.postalCode,
            latitude = latitude,
            longitude = longitude,
        )
    }

    override fun isFineLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun isCoarseLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun isLocationServiceEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            LocationManagerCompat.isLocationEnabled(locationManager)
        }
    }

    override fun getCurrentCoordinate(): Observable<CoordinateModel> {
        var locationListenerCompat: LocationListenerCompat? = null
        return Observable.create { emitter ->
            try {
                val lastKnownLocation =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastKnownLocation != null) {
                    Log.d(
                        CorePlatformLocationRepositoryImpl::class.java.simpleName,
                        "last known fetched"
                    )
                    emitter.onNext(getCoordinateModelFromLocation(lastKnownLocation))
                    emitter.onComplete()
                    return@create
                }
                Log.d(
                    CorePlatformLocationRepositoryImpl::class.java.simpleName,
                    "last known is not fetched, request a new one"
                )

                val locationRequest = LocationRequestCompat.Builder(60000)
                    .setMinUpdateIntervalMillis(30000)
                    .setQuality(LocationRequestCompat.QUALITY_HIGH_ACCURACY)
                    .setMaxUpdates(1)
                    .build()
                locationListenerCompat = LocationListenerCompat { location ->
                    emitter.onNext(getCoordinateModelFromLocation(location))
                    LocationManagerCompat.removeUpdates(locationManager, locationListenerCompat!!)
                    emitter.onComplete()
                }
                LocationManagerCompat.requestLocationUpdates(
                    locationManager,
                    LocationManager.NETWORK_PROVIDER,
                    locationRequest,
                    ContextCompat.getMainExecutor(context),
                    locationListenerCompat!!,
                )
                return@create
            } catch (e: Throwable) {
                emitter.onError(e)
                LocationManagerCompat.removeUpdates(locationManager, locationListenerCompat!!)
                emitter.onComplete()
            }
        }
    }

    override fun getCurrentAddress(): Observable<AddressModel> {
        return getCurrentCoordinate().flatMap { coordinate ->
            getAddressByCoordinate(latitude = coordinate.latitude, longitude = coordinate.longitude)
        }
    }

    override fun getAddressByCoordinate(
        latitude: Double,
        longitude: Double,
    ): Observable<AddressModel> {
        return Observable.create { emitter ->
            val geoCoder = Geocoder(context, Locale.getDefault())
            var address: Address? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geoCoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        address = addresses.first()
                    }
                }
            } else {
                val addresses = geoCoder.getFromLocation(latitude, longitude, 1) ?: listOf()
                if (addresses.isNotEmpty()) {
                    address = addresses.first()
                }
            }
            if (address != null) {
                emitter.onNext(
                    getAddress(
                        latitude = latitude,
                        longitude = longitude,
                        address = address!!
                    )
                )
                emitter.onComplete()
            } else {
                emitter.onError(
                    CorePlatformException(
                        code = "ADDRESS_EMPTY",
                        message = "Address is empty"
                    )
                )
                emitter.onComplete()
            }
        }
    }
}