package co.id.fadlurahmanfdev.kotlin_core_platform.data.exception

data class CorePlatformException(
    val code: String,
    override val message: String
) : Throwable(message = message)
