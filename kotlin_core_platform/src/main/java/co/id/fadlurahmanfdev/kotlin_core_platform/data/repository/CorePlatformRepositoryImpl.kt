package co.id.fadlurahmanfdev.kotlin_core_platform.data.repository

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import androidx.core.util.Consumer
import co.id.fadlurahmanfdev.kotlin_core_platform.data.exception.CorePlatformException
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.AddressModel
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.CoordinateModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class CorePlatformRepositoryImpl : CorePlatformRepository {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private fun getFusedLocationProviderClient(context: Context): FusedLocationProviderClient {
        if (this::fusedLocationProviderClient.isInitialized) {
            return fusedLocationProviderClient
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        return fusedLocationProviderClient
    }

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

    override fun getCurrentLocation(
        context: Context,
        onSuccess: (CoordinateModel) -> Unit,
        onError: (CorePlatformException) -> Unit,
    ) {
        try {
            val locationManager = getLocationManager(context)
            val locationRequest = LocationRequestCompat.Builder(60000).build()
            val listener = LocationListenerCompat { location ->
                onSuccess(
                    CoordinateModel(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                )
            }
            LocationManagerCompat.requestLocationUpdates(
                locationManager,
                LocationManager.NETWORK_PROVIDER,
                locationRequest,
                ContextCompat.getMainExecutor(context),
                listener
            )
        } catch (e: Throwable) {
            onError(CorePlatformException(code = "GENERAL", message = e.message ?: "-"))
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

//    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
//    override fun getLastCoordinate(
//        context: Context,
//        onSuccess: (CoordinateModel) -> Unit,
//        onError: (Exception) -> Unit
//    ) {
//        val locationRequest = LocationRequest()
//        val fusedLocation = getFusedLocationProviderClient(context)
//        fusedLocation.requestLocationUpdates(
//            locationRequest,
//            object : LocationCallback() {
//                override fun onLocationResult(p0: LocationResult?) {
//                    super.onLocationResult(p0)
//                    val locations = p0?.locations ?: listOf()
//                    if (locations.isNotEmpty()) {
//                        val location = locations.first()
//                        onSuccess(
//                            CoordinateModel(
//                                latitude = location.latitude,
//                                longitude = location.longitude
//                            )
//                        )
//                    } else {
//                        onError(Exception("location is empty"))
//                    }
//                }
//            },
//            Looper.getMainLooper(),
//        )
//    }

    override fun removeLocationManager() {
    }

    interface UpdateLocationCallBack {
        fun onSuccessUpdateLocation(coordinate: CoordinateModel)
    }
}