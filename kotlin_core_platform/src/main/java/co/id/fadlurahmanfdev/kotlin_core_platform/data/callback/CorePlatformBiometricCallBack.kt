package co.id.fadlurahmanfdev.kotlin_core_platform.data.callback

import android.content.DialogInterface
import android.os.CancellationSignal
import javax.crypto.Cipher

interface CorePlatformBiometricCallBack {
    fun onCancel(dialogInterface: DialogInterface, cancellationSignal: CancellationSignal) {
        cancellationSignal.cancel()
        dialogInterface.cancel()
    }

    fun onSuccessAuthenticateForEncrypt(cipher: Cipher, encodedIvKey: String) {}
    fun onSuccessAuthenticateForDecrypt(cipher: Cipher) {}
    fun onFailedAuthenticate() {}

    fun onErrorAuthenticate(errorCode: Int, errString: CharSequence?) {}
}