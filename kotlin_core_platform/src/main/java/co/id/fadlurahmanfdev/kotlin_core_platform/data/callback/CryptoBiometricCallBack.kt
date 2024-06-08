package co.id.fadlurahmanfdev.kotlin_core_platform.data.callback

import android.content.DialogInterface
import android.os.CancellationSignal
import javax.crypto.Cipher

interface BaseCorePlatformBiometricCallBack {
    fun onCancel(dialogInterface: DialogInterface, cancellationSignal: CancellationSignal) {
        cancellationSignal.cancel()
        dialogInterface.cancel()
    }

    fun onFailedAuthenticate() {}

    fun onErrorAuthenticate(errorCode: Int, errString: CharSequence?) {}
}

interface CryptoBiometricCallBack : BaseCorePlatformBiometricCallBack {

    fun onSuccessAuthenticateForEncrypt(cipher: Cipher, encodedIvKey: String) {}
    fun onSuccessAuthenticateForDecrypt(cipher: Cipher) {}
}

interface BiometricCallBack : BaseCorePlatformBiometricCallBack {
    fun onSuccessAuthenticate() {}
}