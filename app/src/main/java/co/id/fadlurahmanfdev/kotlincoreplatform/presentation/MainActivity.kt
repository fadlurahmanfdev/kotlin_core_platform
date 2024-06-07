package co.id.fadlurahmanfdev.kotlincoreplatform.presentation

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import co.id.fadlurahmanfdev.kotlin_core_platform.data.callback.CorePlatformBiometricCallBack
import co.id.fadlurahmanfdev.kotlin_core_platform.data.repository.CorePlatformLocationRepositoryImpl
import co.id.fadlurahmanfdev.kotlin_core_platform.domain.plugin.CorePlatformBiometricManager
import co.id.fadlurahmanfdev.kotlin_core_platform.domain.plugin.CorePlatformLocationManager
import co.id.fadlurahmanfdev.kotlincoreplatform.R
import co.id.fadlurahmanfdev.kotlincoreplatform.data.FeatureModel
import co.id.fadlurahmanfdev.kotlincoreplatform.domain.ExampleCorePlatformUseCaseImpl
import javax.crypto.Cipher

class MainActivity : AppCompatActivity(), ListExampleAdapter.Callback {
    lateinit var viewModel: MainViewModel
    lateinit var corePlatformLocationManager: CorePlatformLocationManager
    lateinit var corePlatformBiometricManager: CorePlatformBiometricManager

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
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Prompt Biometric",
            desc = "Prompt Encrypt Secure Biometric",
            enum = "PROMPT_ENCRYPT_SECURE_BIOMETRIC"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Prompt Decrypt Biometric",
            desc = "Prompt Decrypt Secure Biometric",
            enum = "PROMPT_DECRYPT_SECURE_BIOMETRIC"
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
        corePlatformBiometricManager = CorePlatformBiometricManager()
        corePlatformBiometricManager.initialize(
            this,
            "example_core_platform_key"
        )

        viewModel = MainViewModel(
            exampleCorePlatformUseCase = ExampleCorePlatformUseCaseImpl(
                platformRepository = CorePlatformLocationRepositoryImpl(
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

    private val plainText = "PASSW0RD"
    private lateinit var encodedEncryptedPassword: String
    private lateinit var encodedIvKey: String

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

            "PROMPT_ENCRYPT_SECURE_BIOMETRIC" -> {
                corePlatformBiometricManager.promptEncrypt(
                    title = "TES TITLE ENCRYPT",
                    description = "DESC ENCRYPT",
                    negativeText = "NEGATIVE",
                    callBack = object : CorePlatformBiometricCallBack {
                        override fun onSuccessAuthenticateForEncrypt(
                            cipher: Cipher,
                            encodedIvKey: String
                        ) {
                            super.onSuccessAuthenticateForEncrypt(cipher, encodedIvKey)
                            val encryptedPassword =
                                cipher.doFinal(plainText.toByteArray())
                            encodedEncryptedPassword =
                                Base64.encodeToString(encryptedPassword, Base64.NO_WRAP)
                            this@MainActivity.encodedIvKey = encodedIvKey
                            println("MASUK ENCODED ENCRYPTED PASSWORD: $encodedEncryptedPassword")
                        }
                    })
            }

            "PROMPT_DECRYPT_SECURE_BIOMETRIC" -> {
                corePlatformBiometricManager.promptDecrypt(
                    title = "TES TITLE DECRYPT",
                    description = "DESC DECRYPT",
                    negativeText = "NEGATIVE",
                    encodedIvKey = encodedIvKey,
                    callBack = object : CorePlatformBiometricCallBack {
                        override fun onSuccessAuthenticateForDecrypt(cipher: Cipher) {
                            super.onSuccessAuthenticateForDecrypt(cipher)
                            val decodedPassword =
                                Base64.decode(encodedEncryptedPassword, Base64.NO_WRAP)
                            val plainPassword = String(cipher.doFinal(decodedPassword))
                            println("MASUK PLAIN PASSWORD: $plainPassword")
                        }
                    })
            }
        }
    }
}