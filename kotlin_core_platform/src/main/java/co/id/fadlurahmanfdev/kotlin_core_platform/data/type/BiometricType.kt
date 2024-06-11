package co.id.fadlurahmanfdev.kotlin_core_platform.data.type

enum class BiometricType {
    /**
     * Represents biometric authentication using fingerprint recognition or face recognition.
     */
    WEAK,
    /**
     * Represents biometric authentication using fingerprint recognition.
     */
    STRONG,
    /**
     * Represents biometric authentication using device credential (PIN, PASSWORD, or PATTERN).
     */
    DEVICE_CREDENTIAL
}