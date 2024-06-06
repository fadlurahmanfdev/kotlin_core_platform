package co.id.fadlurahmanfdev.kotlin_core_platform.data.type

enum class CanAuthenticateReason {
    SUCCESS,
    NO_BIOMETRIC_AVAILABLE,
    BIOMETRIC_UNAVAILABLE,
    NONE_ENROLLED,
    UNKNOWN,
}