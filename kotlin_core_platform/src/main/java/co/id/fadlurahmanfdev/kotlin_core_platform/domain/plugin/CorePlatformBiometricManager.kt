package co.id.fadlurahmanfdev.kotlin_core_platform.domain.plugin

import android.app.Activity
import android.content.DialogInterface.OnClickListener
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import co.id.fadlurahmanfdev.kotlin_core_platform.data.repository.CorePlatformBiometricRepository
import co.id.fadlurahmanfdev.kotlin_core_platform.data.repository.CorePlatformBiometricRepositoryImpl
import co.id.fadlurahmanfdev.kotlin_core_platform.data.type.CanAuthenticateReasonType
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CorePlatformBiometricManager {
    private val corePlatformBiometricRepository: CorePlatformBiometricRepository =
        CorePlatformBiometricRepositoryImpl()
    private lateinit var executor: Executor
    private var cancellationSignal: CancellationSignal? = null
    private lateinit var keyStoreAlias: String
    private lateinit var secretKey: SecretKey
    private lateinit var activity: Activity

    companion object {

        fun getCipher(): Cipher {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7
                )
            } else {
                Cipher.getInstance("AES/CBC/PKCS7Padding")
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun generateKeyGenParameterSpec(keyStoreAlias: String): KeyGenParameterSpec {
            return KeyGenParameterSpec.Builder(
                keyStoreAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).apply {
                setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                setUserAuthenticationRequired(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setInvalidatedByBiometricEnrollment(true)
                }
            }.build()
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec): SecretKey {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )
            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        }

        fun generateSecretKey(): SecretKey {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256)
            return keyGenerator.generateKey()
        }

        fun getOrCreateSecretKey(keyStoreAlias: String): SecretKey {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val existingSecretKey = keyStore.getKey(keyStoreAlias, null) as SecretKey?
            existingSecretKey?.let {
                Log.d(
                    CorePlatformBiometricManager::class.java.simpleName,
                    "fetched existing secret key $keyStoreAlias"
                )
                return it
            }

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val key = generateSecretKey(generateKeyGenParameterSpec(keyStoreAlias))
                Log.d(
                    CorePlatformBiometricManager::class.java.simpleName,
                    "successfully generated secret key"
                )
                key
            } else {
                val key = generateSecretKey()
                Log.d(
                    CorePlatformBiometricManager::class.java.simpleName,
                    "successfully generated secret key"
                )
                key
            }
        }

        @RequiresApi(Build.VERSION_CODES.P)
        fun getBiometricPrompt(
            activity: Activity,
            title: String,
            description: String,
            negativeText: String,
            executor: Executor,
            listener: OnClickListener,
        ): BiometricPrompt {
            return BiometricPrompt.Builder(activity).setTitle(title).setDescription(description)
                .setNegativeButton(negativeText, executor, listener)
                .build()
        }

        fun getAndroidXPromptInfo(
            title: String,
            description: String,
            negativeText: String,
        ): androidx.biometric.BiometricPrompt.PromptInfo {
            return androidx.biometric.BiometricPrompt.PromptInfo.Builder().setTitle(title)
                .setDescription(description).setNegativeButtonText(negativeText)
                .build()
        }

        fun getAndroidXBiometricPrompt(
            fragmentActivity: FragmentActivity,
            executor: Executor,
            callBack: androidx.biometric.BiometricPrompt.AuthenticationCallback,
        ): androidx.biometric.BiometricPrompt {
            return androidx.biometric.BiometricPrompt(
                fragmentActivity,
                executor,
                callBack,
            )
        }

        fun encrypt(): String? {
            return ""
        }

        fun decrypt(): String? {
            return ""
        }
    }

    fun initialize(activity: Activity, keyStoreAlias: String) {
        this.keyStoreAlias = keyStoreAlias
        this.activity = activity
        executor = ContextCompat.getMainExecutor(this.activity)
        secretKey = getOrCreateSecretKey(keyStoreAlias)
    }

    fun canAuthenticate(): Boolean {
        return corePlatformBiometricRepository.canAuthenticate(activity)
    }

    fun canAuthenticateWithReason(): CanAuthenticateReasonType {
        return corePlatformBiometricRepository.canAuthenticateWithReason(activity)
    }

    fun promptEncrypt(
        title: String,
        description: String,
        negativeText: String,
    ) {
        return promptEncrypt(
            title = title,
            description = description,
            negativeText = negativeText,
            callBack = null,
        )
    }

    fun promptEncrypt(
        title: String,
        description: String,
        negativeText: String,
        callBack: CallBack? = null,
    ) {
        cancellationSignal = CancellationSignal()
        val cipher = getCipher()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                val biometricPrompt = getBiometricPrompt(
                    activity = activity,
                    title = title,
                    description = description,
                    negativeText = negativeText,
                    executor = executor
                ) { dialog, _ ->
                    dialog?.cancel()
                    callBack?.onCancel(cancellationSignal)
                }
                biometricPrompt.authenticate(
                    BiometricPrompt.CryptoObject(cipher),
                    cancellationSignal!!,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            val currentCipher = result!!.cryptoObject!!.cipher!!
                            val encodedIvKey =
                                Base64.encodeToString(currentCipher.iv, Base64.NO_WRAP)
                            callBack?.onSuccessAuthenticateForEncrypt(
                                cipher = cipher,
                                encodedIvKey = encodedIvKey
                            )
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            callBack?.onFailedAuthenticate()
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence?
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            callBack?.onErrorAuthenticate(errorCode, errString)
                        }
                    })
            }

            else -> {
                val promptInfo = getAndroidXPromptInfo(
                    title = title,
                    description = description,
                    negativeText = negativeText
                )

                val biometricPrompt = getAndroidXBiometricPrompt(
                    fragmentActivity = activity as FragmentActivity,
                    executor = executor,
                    callBack = object :
                        androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            val currentCipher = result.cryptoObject!!.cipher!!
                            val encodedIvKey =
                                Base64.encodeToString(currentCipher.iv, Base64.NO_WRAP)
                            callBack?.onSuccessAuthenticateForEncrypt(
                                cipher = cipher,
                                encodedIvKey = encodedIvKey
                            )
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            callBack?.onFailedAuthenticate()
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            callBack?.onErrorAuthenticate(errorCode, errString)
                        }
                    }
                )
                biometricPrompt.authenticate(
                    promptInfo,
                    androidx.biometric.BiometricPrompt.CryptoObject(cipher)
                )
            }
        }
    }

    fun promptDecrypt(
        title: String,
        description: String,
        negativeText: String,
        encodedIvKey: String,
    ) {
        return promptDecrypt(
            title = title,
            description = description,
            negativeText = negativeText,
            encodedIvKey = encodedIvKey,
            callBack = null,
        )
    }

    fun promptDecrypt(
        title: String,
        description: String,
        negativeText: String,
        encodedIvKey: String,
        callBack: CallBack?
    ) {
        cancellationSignal = CancellationSignal()
        val ivKey = Base64.decode(encodedIvKey, Base64.NO_WRAP)
        val ivSpec = IvParameterSpec(ivKey)
        val cipher = getCipher()
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                val biometricPrompt = getBiometricPrompt(
                    activity = activity,
                    title = title,
                    description = description,
                    negativeText = negativeText,
                    executor = executor
                ) { dialog, _ ->
                    dialog?.cancel()
                    callBack?.onCancel(cancellationSignal)
                }
                biometricPrompt.authenticate(
                    BiometricPrompt.CryptoObject(cipher),
                    cancellationSignal!!,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            val currentCipher = result!!.cryptoObject!!.cipher!!
                            callBack?.onSuccessAuthenticateForDecrypt(currentCipher)
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            callBack?.onFailedAuthenticate()
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence?
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            callBack?.onErrorAuthenticate(errorCode, errString)
                        }
                    },
                )
            }

            else -> {
                val promptInfo = getAndroidXPromptInfo(
                    title = title,
                    description = description,
                    negativeText = negativeText
                )

                val biometricPrompt = getAndroidXBiometricPrompt(
                    fragmentActivity = activity as FragmentActivity,
                    executor = executor,
                    callBack = object :
                        androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            val currentCipher = result.cryptoObject!!.cipher!!
                            callBack?.onSuccessAuthenticateForDecrypt(currentCipher)
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            callBack?.onFailedAuthenticate()
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            callBack?.onErrorAuthenticate(errorCode, errString)
                        }
                    }
                )
                biometricPrompt.authenticate(
                    promptInfo,
                    androidx.biometric.BiometricPrompt.CryptoObject(cipher)
                )
            }
        }
    }

    interface CallBack {
        fun onCancel(cancellationSignal: CancellationSignal?) {
            cancellationSignal?.cancel()
        }

        fun onSuccessAuthenticateForEncrypt(cipher: Cipher, encodedIvKey: String) {}
        fun onSuccessAuthenticateForDecrypt(cipher: Cipher) {}
        fun onFailedAuthenticate() {}

        fun onErrorAuthenticate(errorCode: Int, errString: CharSequence?) {}
    }
}