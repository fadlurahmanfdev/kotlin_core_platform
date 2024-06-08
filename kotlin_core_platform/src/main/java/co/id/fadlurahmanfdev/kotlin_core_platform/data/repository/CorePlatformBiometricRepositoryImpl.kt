package co.id.fadlurahmanfdev.kotlin_core_platform.data.repository

import android.app.Activity
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.util.Base64
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import co.id.fadlurahmanfdev.kotlin_core_platform.data.callback.BiometricCallBack
import co.id.fadlurahmanfdev.kotlin_core_platform.data.callback.CryptoBiometricCallBack
import co.id.fadlurahmanfdev.kotlin_core_platform.data.type.CanAuthenticateReasonType
import co.id.fadlurahmanfdev.kotlin_core_platform.domain.plugin.CorePlatformBiometricManager
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CorePlatformBiometricRepositoryImpl : CorePlatformBiometricRepository {
    override fun canAuthenticate(activity: Activity): Boolean {
        return canAuthenticateWithReason(activity) == CanAuthenticateReasonType.SUCCESS
    }

    override fun canAuthenticateWithReason(activity: Activity): CanAuthenticateReasonType {
        val biometricManager = androidx.biometric.BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS ->
                CanAuthenticateReasonType.SUCCESS

            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                CanAuthenticateReasonType.NO_BIOMETRIC_AVAILABLE

            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                CanAuthenticateReasonType.BIOMETRIC_UNAVAILABLE

            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                CanAuthenticateReasonType.NONE_ENROLLED
            }

            else -> {
                CanAuthenticateReasonType.UNKNOWN
            }
        }
    }

    override fun promptEncrypt(
        activity: Activity,
        cancellationSignal: CancellationSignal,
        keystoreAlias: String,
        title: String,
        description: String,
        negativeText: String,
        callBack: CryptoBiometricCallBack?,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val cipher = CorePlatformBiometricManager.getCipher()
        val secretKey: SecretKey = CorePlatformBiometricManager.getOrCreateSecretKey(keystoreAlias)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                val biometricPrompt = CorePlatformBiometricManager.getCryptoBiometricPrompt(
                    activity = activity,
                    title = title,
                    description = description,
                    negativeText = negativeText,
                    executor = executor
                ) { dialog, _ ->
                    callBack?.onCancel(
                        dialogInterface = dialog,
                        cancellationSignal = cancellationSignal
                    )
                }
                biometricPrompt.authenticate(
                    BiometricPrompt.CryptoObject(cipher),
                    cancellationSignal,
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
                val promptInfo = CorePlatformBiometricManager.getAndroidXPromptInfo(
                    title = title,
                    description = description,
                    negativeText = negativeText
                )

                val biometricPrompt = CorePlatformBiometricManager.getAndroidXBiometricPrompt(
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

    override fun promptDecrypt(
        activity: Activity,
        cancellationSignal: CancellationSignal,
        keystoreAlias: String,
        encodedIvKey: String,
        title: String,
        description: String,
        negativeText: String,
        callBack: CryptoBiometricCallBack?,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val cipher = CorePlatformBiometricManager.getCipher()
        val secretKey: SecretKey = CorePlatformBiometricManager.getOrCreateSecretKey(keystoreAlias)
        val ivKey = Base64.decode(encodedIvKey, Base64.NO_WRAP)
        val ivSpec = IvParameterSpec(ivKey)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                val biometricPrompt = CorePlatformBiometricManager.getCryptoBiometricPrompt(
                    activity = activity,
                    title = title,
                    description = description,
                    negativeText = negativeText,
                    executor = executor
                ) { dialog, _ ->
                    callBack?.onCancel(
                        dialogInterface = dialog,
                        cancellationSignal = cancellationSignal
                    )
                }
                biometricPrompt.authenticate(
                    BiometricPrompt.CryptoObject(cipher),
                    cancellationSignal,
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
                val promptInfo = CorePlatformBiometricManager.getAndroidXPromptInfo(
                    title = title,
                    description = description,
                    negativeText = negativeText
                )

                val biometricPrompt = CorePlatformBiometricManager.getAndroidXBiometricPrompt(
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
    override fun prompt(
        activity: Activity,
        cancellationSignal: CancellationSignal,
        title: String,
        description: String,
        negativeText: String,
        callBack: BiometricCallBack?,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                val biometricPrompt = CorePlatformBiometricManager.getBiometricPrompt(
                    activity = activity,
                    title = title,
                    description = description,
                    negativeText = negativeText,
                    executor = executor
                ) { dialog, _ ->
                    callBack?.onCancel(
                        dialogInterface = dialog,
                        cancellationSignal = cancellationSignal
                    )
                }
                biometricPrompt.authenticate(
                    cancellationSignal,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            callBack?.onSuccessAuthenticate()
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
                val promptInfo = CorePlatformBiometricManager.getAndroidXPromptInfo(
                    title = title,
                    description = description,
                    negativeText = negativeText
                )

                val biometricPrompt = CorePlatformBiometricManager.getAndroidXBiometricPrompt(
                    fragmentActivity = activity as FragmentActivity,
                    executor = executor,
                    callBack = object :
                        androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            callBack?.onSuccessAuthenticate()
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
                biometricPrompt.authenticate(promptInfo)
            }
        }
    }
}