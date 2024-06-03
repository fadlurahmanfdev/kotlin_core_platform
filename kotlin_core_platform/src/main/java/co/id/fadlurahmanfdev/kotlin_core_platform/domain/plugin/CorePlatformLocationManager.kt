package co.id.fadlurahmanfdev.kotlin_core_platform.domain.plugin

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

class CorePlatformLocationManager(context: Context) {
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun requestLocationService(
        activity: Activity,
        callback: RequestLocationServiceCallback,
    ) {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000)
                .build()

        val settingRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val task =
            LocationServices.getSettingsClient(activity)
                .checkLocationSettings(settingRequest.build())
        task.addOnSuccessListener { response ->
            callback.onLocationServiceEnabled(enabled = (response.locationSettingsStates?.isLocationPresent == true) && (response.locationSettingsStates?.isLocationUsable == true))
        }.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                callback.onShouldShowPromptServiceDialog(
                    IntentSenderRequest.Builder(exception.resolution).build()
                )
            } else {
                callback.onFailure(exception)
            }
        }.addOnCompleteListener {
            Log.d(
                CorePlatformLocationManager::class.java.simpleName,
                "on complete requestLocationService"
            )
        }
    }

    interface RequestLocationServiceCallback {
        fun onLocationServiceEnabled(enabled: Boolean)
        fun onShouldShowPromptServiceDialog(intentSenderRequest: IntentSenderRequest)
        fun onFailure(exception: Exception)
    }
}