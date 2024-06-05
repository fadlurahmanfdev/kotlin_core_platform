package co.id.fadlurahmanfdev.kotlin_core_platform.domain.plugin

import android.app.Activity
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.ContextCompat
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CorePlatformBiometricManager {
    private fun generateKeyGenParameterSpec(): KeyGenParameterSpec {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyGenParameterSpec.Builder(
                "KEY_NAME",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).apply {
                setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                setUserAuthenticationRequired(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setInvalidatedByBiometricEnrollment(true)
                }
            }.build()
        } else {
            throw IllegalAccessException("ILLEGAL ACCESS EXCEPTION")
        }
    }

    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )
            keyGenerator.init(keyGenParameterSpec)
            val key = keyGenerator.generateKey()
            println("MASUK GENERATE SECRET KEY: ${key.algorithm}")
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        return keyStore.getKey("KEY_NAME", null) as SecretKey
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
    }

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt

    private var cancellationSignal: CancellationSignal? = null

    fun initialize() {
        generateSecretKey(generateKeyGenParameterSpec())
    }

    fun promptEncrypt(
        activity: Activity,
        title: String,
        description: String,
        negativeText: String,
        callBack: CallBack
    ) {
        cancellationSignal = CancellationSignal()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            executor = ContextCompat.getMainExecutor(activity)
            biometricPrompt =
                BiometricPrompt.Builder(activity).setTitle(title).setDescription(description)
                    .setNegativeButton(negativeText, executor) { dialog, _ ->
                        dialog.cancel()
                        callBack.onCancel(cancellationSignal)
                    }
                    .build()
            val cipher = getCipher()
            val secretKey = getSecretKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            biometricPrompt.authenticate(
                BiometricPrompt.CryptoObject(cipher),
                cancellationSignal!!,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        val currentCipher = result!!.cryptoObject!!.cipher!!
//                        val password = "PASSW0RD"
//                        val encryptedPassword = currentCipher.doFinal(
//                            password.toByteArray(
//                                Charset.defaultCharset()
//                            )
//                        )
//                        println("MASUK SINI ENCRYPTED PASSWORD: $encryptedPassword")
//                        var encodedEncryptedPassword: String? = null
//                        if (encryptedPassword != null) {
//                            encodedEncryptedPassword =
//                                Base64.encodeToString(encryptedPassword, Base64.NO_WRAP)
//                            println("MASUK SINI ENCODED ENCRYPTED PASSWORD: $encodedEncryptedPassword")
//                        }

                        val encodedIvKey = Base64.encodeToString(currentCipher.iv, Base64.NO_WRAP)
                        callBack.onEncrypted(
                            cipher = cipher,
                            encodedIvKey = encodedIvKey,
                            ivKey = currentCipher.iv
                        )
                    }
                })
        }
    }

    fun promptDecrypt(
        activity: Activity,
        title: String,
        description: String,
        negativeText: String,
        encodedIvKey: String,
        ivKey: ByteArray,
        callBack: CallBack
    ) {
        cancellationSignal = CancellationSignal()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            executor = ContextCompat.getMainExecutor(activity)
            biometricPrompt =
                BiometricPrompt.Builder(activity).setTitle(title).setDescription(description)
                    .setNegativeButton(negativeText, executor) { dialog, _ ->
                        dialog.cancel()
                        callBack.onCancel(cancellationSignal)
                    }
                    .build()
            val cipher = getCipher()
            val secretKey = getSecretKey()
            val ivSpec = IvParameterSpec(ivKey)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            biometricPrompt.authenticate(
                BiometricPrompt.CryptoObject(cipher),
                cancellationSignal!!,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        val currentCipher = result!!.cryptoObject!!.cipher!!
                        callBack.onDecrypted(currentCipher)
                    }
                })
        }
    }

    interface CallBack {
        fun onCancel(cancellationSignal: CancellationSignal?) {
            cancellationSignal?.cancel()
        }

        fun onEncrypted(cipher: Cipher, encodedIvKey: String, ivKey: ByteArray) {}
        fun onDecrypted(cipher: Cipher) {}
    }
}