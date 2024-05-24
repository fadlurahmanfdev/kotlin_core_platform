package co.id.fadlurahmanfdev.kotlin_core_platform.data.repository

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import co.id.fadlurahmanfdev.kotlin_core_platform.data.exception.CorePlatformException
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.AddressModel
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.CoordinateModel
import java.util.Locale

class CorePlatformRepositoryImpl : CorePlatformRepository {
    private lateinit var locationManager: LocationManager
    private fun getLocationManager(context: Context): LocationManager {
        if (this::locationManager.isInitialized) {
            return locationManager
        }
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager
    }

    override fun isLocationEnabled(context: Context): Boolean {
        val locationManager = getLocationManager(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            LocationManagerCompat.isLocationEnabled(locationManager)
        }
    }

    private var requestAndForgetLocationListener: LocationListenerCompat? = null
    override fun requestAndForgetLocation(
        context: Context,
        onSuccess: (CoordinateModel) -> Unit,
        onError: (CorePlatformException) -> Unit,
    ) {
        try {
            val locationManager = getLocationManager(context)
            val locationRequest = LocationRequestCompat.Builder(60000).build()
            requestAndForgetLocationListener = LocationListenerCompat { location ->
                onSuccess(
                    CoordinateModel(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                )
                LocationManagerCompat.removeUpdates(
                    locationManager,
                    requestAndForgetLocationListener!!
                )
            }
            LocationManagerCompat.requestLocationUpdates(
                locationManager,
                LocationManager.NETWORK_PROVIDER,
                locationRequest,
                ContextCompat.getMainExecutor(context),
                requestAndForgetLocationListener!!,
            )
        } catch (e: Throwable) {
            if (requestAndForgetLocationListener != null) {
                LocationManagerCompat.removeUpdates(
                    locationManager,
                    requestAndForgetLocationListener!!
                )
            }
            requestAndForgetLocationListener = null;
            onError(CorePlatformException(code = "REQUEST_LOCATION_00", message = e.message ?: "-"))
        }
    }

    override fun getAddress(
        context: Context,
        latitude: Double,
        longitude: Double,
        onSuccess: (AddressModel) -> Unit,
        onError: (CorePlatformException) -> Unit,
    ) {
        val geoCoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geoCoder.getFromLocation(
                latitude, longitude, 1,
            ) { addresses ->
                if (addresses.isNotEmpty()) {
                    val address = addresses.first()
                    onSuccess(
                        AddressModel(
                            country = address.countryName,
                            adminArea = address.adminArea,
                            subAdminArea = address.subAdminArea,
                            locality = address.locality,
                            subLocality = address.subLocality,
                            postalCode = address.postalCode,
                            latitude = latitude,
                            longitude = longitude,
                        )
                    )
                } else {
                    onError(
                        CorePlatformException(
                            code = "ADDRESS_EMPTY",
                            message = "Address is empty"
                        )
                    )
                }
            }

        } else {
            val addresses =
                geoCoder.getFromLocation(latitude, longitude, 1) ?: listOf()
            if (addresses.isNotEmpty()) {
                val address = addresses.first()
                onSuccess(
                    AddressModel(
                        country = address.countryName,
                        adminArea = address.adminArea,
                        subAdminArea = address.subAdminArea,
                        locality = address.locality,
                        subLocality = address.subLocality,
                        postalCode = address.postalCode,
                        latitude = latitude,
                        longitude = longitude,
                    )
                )
            } else {
                onError(CorePlatformException(code = "ADDRESS_EMPTY", message = "Address is empty"))
            }
        }

    }
}