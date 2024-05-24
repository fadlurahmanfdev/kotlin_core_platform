package co.id.fadlurahmanfdev.kotlincoreplatform.domain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import co.id.fadlurahmanfdev.kotlin_core_platform.data.exception.CorePlatformException
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.AddressModel
import co.id.fadlurahmanfdev.kotlin_core_platform.data.model.CoordinateModel
import co.id.fadlurahmanfdev.kotlin_core_platform.data.repository.CorePlatformRepository

class ExampleCryptoUseCaseImpl(
    private val platformRepository: CorePlatformRepository,
) : ExampleCryptoUseCase {
    override fun isLocationPermissionGranted(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun isLocationEnabled(context: Context): Boolean {
        return isLocationPermissionGranted(context) && platformRepository.isLocationEnabled(context)
    }

    override fun getCurrentLocation(
        context: Context,
        onSuccess: (CoordinateModel) -> Unit,
        onError: (CorePlatformException) -> Unit
    ) {
        return platformRepository.getCurrentLocation(context, onSuccess, onError = onError)
    }


    override fun getAddress(
        context: Context,
        onSuccess: (AddressModel) -> Unit,
        onError: (CorePlatformException) -> Unit
    ) {
        platformRepository.getCurrentLocation(
            context,
            onSuccess = { coordinate ->
                platformRepository.getAddress(
                    context,
                    latitude = coordinate.latitude,
                    longitude = coordinate.longitude,
                    onSuccess = onSuccess,
                    onError = onError,
                )
            },
            onError = onError,
        )
    }


}