package co.id.fadlurahmanfdev.kotlincoreplatform.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import co.id.fadlurahmanfdev.kotlin_core_platform.data.repository.CorePlatformRepositoryImpl
import co.id.fadlurahmanfdev.kotlin_core_platform.domain.plugin.CorePlatformLocationManager
import co.id.fadlurahmanfdev.kotlincoreplatform.R
import co.id.fadlurahmanfdev.kotlincoreplatform.data.FeatureModel
import co.id.fadlurahmanfdev.kotlincoreplatform.domain.ExampleCorePlatformUseCaseImpl

class MainActivity : AppCompatActivity(), ListExampleAdapter.Callback {
    lateinit var viewModel: MainViewModel
    lateinit var corePlatformLocationManager: CorePlatformLocationManager

    private val features: List<FeatureModel> = listOf<FeatureModel>(
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Request Location Permission",
            desc = "Request Location Permission",
            enum = "REQUEST_LOCATION_PERMISSION"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Request Location Service",
            desc = "Request Location Service",
            enum = "REQUEST_LOCATION_SERVICE"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Is Location Enabled",
            desc = "Check whether location enabled",
            enum = "CHECK_LOCATION_ENABLED"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Get Coordinate",
            desc = "Get Coordinate Latitude Longitude",
            enum = "GET_COORDINATE"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Get Address",
            desc = "Get Address",
            enum = "GET_ADDRESS"
        ),
    )

    private lateinit var rv: RecyclerView

    private lateinit var adapter: ListExampleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        rv = findViewById<RecyclerView>(R.id.rv)
        corePlatformLocationManager = CorePlatformLocationManager(this)

        viewModel = MainViewModel(
            exampleCorePlatformUseCase = ExampleCorePlatformUseCaseImpl(
                platformRepository = CorePlatformRepositoryImpl(
                    applicationContext,
                )
            )
        )

        rv.setItemViewCacheSize(features.size)
        rv.setHasFixedSize(true)

        adapter = ListExampleAdapter()
        adapter.setCallback(this)
        adapter.setList(features)
        adapter.setHasStableIds(true)
        rv.adapter = adapter
    }

    val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Log.d(MainActivity::class.java.simpleName, "IS LOCATION PERMISSION GRANTED: $it")
        }

    private var locationRequestLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {

        }

    override fun onClicked(item: FeatureModel) {
        when (item.enum) {
            "REQUEST_LOCATION_PERMISSION" -> {
                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            }

            "REQUEST_LOCATION_SERVICE" -> {
                corePlatformLocationManager.requestLocationService(
                    this,
                    object : CorePlatformLocationManager.RequestLocationServiceCallback {
                        override fun onLocationServiceEnabled(enabled: Boolean) {

                        }

                        override fun onShouldShowPromptServiceDialog(intentSenderRequest: IntentSenderRequest) {
                            locationRequestLauncher.launch(intentSenderRequest)
                        }

                        override fun onFailure(exception: Exception) {

                        }
                    },
                )

//                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, 1000)
//
//                    .build()
//
//                val builder = LocationSettingsRequest.Builder()
//                    .addLocationRequest(locationRequest)
//                    .setAlwaysShow(true)
//
//                val result =
//                    LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
//                result.addOnSuccessListener { response ->
//                    println("MASUK RESPONSE: ${response.locationSettingsStates?.isLocationPresent}")
//                    println("MASUK RESPONSE: ${response.locationSettingsStates?.isLocationUsable}")
//                }.addOnFailureListener { exception ->
//                    if(exception is ResolvableApiException){
//                        println("MASUK EXCEPTION: ${exception.javaClass.simpleName}")
//                        println("MASUK EXCEPTION: ${exception.message}")
//                        val intentSenderRequest =
//                            IntentSenderRequest.Builder(exception.resolution).build()
//                        locationRequestLauncher.launch(intentSenderRequest)
//                    }
//                }
            }

            "CHECK_LOCATION_ENABLED" -> {
                viewModel.checkIsLocationEnabled(this)
            }

            "GET_COORDINATE" -> {
                viewModel.getCurrentLocation()
            }

            "GET_ADDRESS" -> {
                viewModel.getAddress()
            }
        }
    }
}