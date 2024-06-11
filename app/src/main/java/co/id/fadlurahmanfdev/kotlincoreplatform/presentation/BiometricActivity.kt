package co.id.fadlurahmanfdev.kotlincoreplatform.presentation

import android.os.Bundle
import android.util.Base64
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import co.id.fadlurahmanfdev.kotlin_core_platform.data.callback.BiometricCallBack
import co.id.fadlurahmanfdev.kotlin_core_platform.data.callback.CryptoBiometricCallBack
import co.id.fadlurahmanfdev.kotlin_core_platform.data.repository.CorePlatformLocationRepositoryImpl
import co.id.fadlurahmanfdev.kotlin_core_platform.data.type.BiometricType
import co.id.fadlurahmanfdev.kotlin_core_platform.domain.plugin.CorePlatformBiometricManager
import co.id.fadlurahmanfdev.kotlincoreplatform.R
import co.id.fadlurahmanfdev.kotlincoreplatform.data.FeatureModel
import co.id.fadlurahmanfdev.kotlincoreplatform.domain.ExampleCorePlatformUseCaseImpl
import javax.crypto.Cipher

class BiometricActivity : AppCompatActivity(), ListExampleAdapter.Callback {
    lateinit var viewModel: MainViewModel
    lateinit var corePlatformBiometricManager: CorePlatformBiometricManager

    private val features: List<FeatureModel> = listOf<FeatureModel>(
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Prompt Weak Biometric",
            desc = "Prompt Weak Biometric (Fingerprint & Face Recognition)",
            enum = "PROMPT_WEAK_BIOMETRIC"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Prompt Strong Biometric",
            desc = "Prompt Strong Biometric (Fingerprint)",
            enum = "PROMPT_STRONG_BIOMETRIC"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Prompt Credential Biometric",
            desc = "Prompt Credential Biometric (Device Password)",
            enum = "PROMPT_CREDENTIAL_BIOMETRIC"
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
        corePlatformBiometricManager = CorePlatformBiometricManager()
        corePlatformBiometricManager.initialize(
            this,
            "example_core_platform_key_v2"
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

    private val plainText = "PASSW0RD"
    private lateinit var encodedEncryptedPassword: String
    private lateinit var encodedIvKey: String

    override fun onClicked(item: FeatureModel) {
        when (item.enum) {
            "PROMPT_WEAK_BIOMETRIC" -> {
                corePlatformBiometricManager.prompt(
                    activity = this,
                    type = BiometricType.WEAK,
                    title = "Authenticate Biometric",
                    description = "Authenticate Biometric",
                    negativeText = "Cancel",
                    callBack = object : BiometricCallBack {
                        override fun onSuccessAuthenticate() {
                            super.onSuccessAuthenticate()
                            println("SUCCESS AUTHENTICATE")
                        }
                    }
                )
            }

            "PROMPT_STRONG_BIOMETRIC" -> {
                corePlatformBiometricManager.prompt(
                    activity = this,
                    type = BiometricType.STRONG,
                    title = "Authenticate Biometric",
                    description = "Authenticate Biometric",
                    negativeText = "Cancel",
                    callBack = object : BiometricCallBack {
                        override fun onSuccessAuthenticate() {
                            super.onSuccessAuthenticate()
                            println("SUCCESS AUTHENTICATE")
                        }
                    }
                )
            }

            "PROMPT_CREDENTIAL_BIOMETRIC" -> {
                corePlatformBiometricManager.prompt(
                    activity = this,
                    type = BiometricType.DEVICE_CREDENTIAL,
                    title = "Authenticate Biometric",
                    description = "Authenticate Biometric",
                    negativeText = "Cancel",
                    callBack = object : BiometricCallBack {
                        override fun onSuccessAuthenticate() {
                            super.onSuccessAuthenticate()
                            println("SUCCESS AUTHENTICATE")
                        }
                    }
                )
            }

            "PROMPT_ENCRYPT_SECURE_BIOMETRIC" -> {
                corePlatformBiometricManager.promptEncrypt(
                    title = "Encrypt Biometric",
                    description = "This will encrypt your text into encrypted text",
                    negativeText = "Cancel",
                    callBack = object : CryptoBiometricCallBack {
                        override fun onSuccessAuthenticateForEncrypt(
                            cipher: Cipher,
                            encodedIvKey: String
                        ) {
                            super.onSuccessAuthenticateForEncrypt(cipher, encodedIvKey)
                            val encryptedPassword =
                                cipher.doFinal(plainText.toByteArray())
                            encodedEncryptedPassword =
                                Base64.encodeToString(encryptedPassword, Base64.NO_WRAP)
                            this@BiometricActivity.encodedIvKey = encodedIvKey
                            println("ENCODED ENCRYPTED PASSWORD: $encodedEncryptedPassword")
                            println("ENCODED IV KEY: ${this@BiometricActivity.encodedIvKey}")
                        }
                    })
            }

            "PROMPT_DECRYPT_SECURE_BIOMETRIC" -> {
                corePlatformBiometricManager.promptDecrypt(
                    title = "Decrypt Biometric",
                    description = "This will decrypt your text into encrypted text",
                    negativeText = "Cancel",
                    encodedIvKey = encodedIvKey,
                    callBack = object : CryptoBiometricCallBack {
                        override fun onSuccessAuthenticateForDecrypt(cipher: Cipher) {
                            super.onSuccessAuthenticateForDecrypt(cipher)
                            println("ENCRYPTED PASSWORD: $encodedEncryptedPassword")
                            val decodedPassword =
                                Base64.decode(encodedEncryptedPassword, Base64.NO_WRAP)
                            val plainPassword = String(cipher.doFinal(decodedPassword))
                            println("PLAIN PASSWORD: $plainPassword")
                        }
                    })
            }
        }
    }
}