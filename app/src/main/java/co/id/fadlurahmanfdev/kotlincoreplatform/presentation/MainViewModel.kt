package co.id.fadlurahmanfdev.kotlincoreplatform.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import co.id.fadlurahmanfdev.kotlincoreplatform.domain.ExampleCryptoUseCase

class MainViewModel(
    private val exampleCryptoUseCase: ExampleCryptoUseCase
) : ViewModel() {

    fun checkIsLocationEnabled(context: Context) {
        val isEnabled = exampleCryptoUseCase.isLocationEnabled(context)
        Log.d(
            MainViewModel::class.java.simpleName,
            "IS LOCATION ENABLED: $isEnabled"
        )
    }

    fun getLastCoordinate(context: Context) {
        exampleCryptoUseCase.getCurrentLocation(
            context,
            onSuccess = { location ->
                Log.d(
                    MainViewModel::class.java.simpleName,
                    "LOCATION: ${location.latitude} & ${location.longitude}"
                )
            },
            onError = { exception ->
                Log.e(
                    MainViewModel::class.java.simpleName,
                    "failed getCurrentLocation: $exception"
                )

            },
        )
    }

    fun getAddress(context: Context) {
        exampleCryptoUseCase.getAddress(
            context,
            onSuccess = { address ->
                Log.d(MainViewModel::class.java.simpleName, "Country: ${address.country}")
                Log.d(MainViewModel::class.java.simpleName, "Province: ${address.adminArea}")
                Log.d(MainViewModel::class.java.simpleName, "City: ${address.subAdminArea}")
                Log.d(MainViewModel::class.java.simpleName, "District: ${address.locality}")
                Log.d(MainViewModel::class.java.simpleName, "SubDistrict: ${address.subLocality}")
                Log.d(MainViewModel::class.java.simpleName, "Postal Code: ${address.postalCode}")
            },
            onError = { exception ->
                Log.e(MainViewModel::class.java.simpleName, "failed getAddress: $exception")
            },
        )
    }

}